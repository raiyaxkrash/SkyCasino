package com.skycasino.network;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.ludik.LudikManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMinesManager {
    public static final int PHASE_IDLE = 0;
    public static final int PHASE_PLAYING = 1;
    public static final int PHASE_WON = 2;
    public static final int PHASE_LOST = 3;

    public static class Session {
        public long bet = 0;
        public int mineCount = 5;
        public int mineMask = 0;
        public int revealedMask = 0;
        public int safeCount = 0;
        public int phase = PHASE_IDLE;
        public BlockPos pos = BlockPos.ZERO;
    }

    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    public static double computeMultiplier(int mineCount, int safeClicks) {
        if (safeClicks <= 0) return 1.0;
        double mult = 0.97;
        for (int k = 0; k < safeClicks; k++) {
            mult *= (25.0 - k) / (25.0 - mineCount - k);
        }
        return Math.round(mult * 100.0) / 100.0;
    }

    public static void handleAction(ServerPlayer player, int action, int param, long bet, BlockPos pos) {
        Session s = SESSIONS.computeIfAbsent(player.getUUID(), id -> new Session());
        s.pos = pos;

        if (action == MinesActionPacket.ACTION_START) {
            if (s.phase == PHASE_PLAYING) return;
            int mines = Math.max(1, Math.min(20, param));
            if (bet < 1 || bet > 10_000_000) return;
            if (!ChipEconomy.deductChips(player, bet)) return;
            LudikManager.onBetPlaced(player, bet);

            s.bet = bet;
            s.mineCount = mines;
            s.revealedMask = 0;
            s.safeCount = 0;
            s.phase = PHASE_PLAYING;

            // Place random mines
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < 25; i++) indices.add(i);
            Collections.shuffle(indices, new Random());
            int mask = 0;
            for (int i = 0; i < mines; i++) mask |= (1 << indices.get(i));
            s.mineMask = mask;

            sendState(player, s, 0, false);
        } else if (action == MinesActionPacket.ACTION_CLICK) {
            if (s.phase != PHASE_PLAYING) return;
            int tile = param;
            if (tile < 0 || tile >= 25) return;
            if ((s.revealedMask & (1 << tile)) != 0) return;

            s.revealedMask |= (1 << tile);
            if ((s.mineMask & (1 << tile)) != 0) {
                s.phase = PHASE_LOST;
                LudikManager.processOutcomeAndCredit(player, s.bet, 0, s.pos);
                CasinoEffects.playLoseEffect(player, s.pos);
                sendState(player, s, 0, true);
            } else {
                s.safeCount++;
                if (s.safeCount >= 25 - s.mineCount) {
                    // Cleared the entire board!
                    doCashout(player, s);
                } else {
                    sendState(player, s, 0, false);
                }
            }
        } else if (action == MinesActionPacket.ACTION_CASHOUT) {
            if (s.phase != PHASE_PLAYING || s.safeCount <= 0) return;
            doCashout(player, s);
        }
    }

    private static void doCashout(ServerPlayer player, Session s) {
        s.phase = PHASE_WON;
        double mult = computeMultiplier(s.mineCount, s.safeCount);
        long payout = LudikManager.processOutcomeAndCredit(player, s.bet, Math.round(s.bet * mult), s.pos);
        CasinoEffects.playWinEffect(player, s.pos, mult >= 5.0);
        sendState(player, s, payout, true);
    }

    private static void sendState(ServerPlayer player, Session s, long payout, boolean revealMines) {
        double mult = computeMultiplier(s.mineCount, s.safeCount);
        double nextMult = (s.phase == PHASE_PLAYING && s.safeCount < 25 - s.mineCount)
                ? computeMultiplier(s.mineCount, s.safeCount + 1)
                : mult;
        ModNetwork.sendToPlayer(new MinesStatePacket(
                s.phase,
                s.mineCount,
                s.revealedMask,
                revealMines ? s.mineMask : 0,
                mult,
                nextMult,
                s.bet,
                payout,
                ChipEconomy.getTotalChips(player)
        ), player);
    }
}
