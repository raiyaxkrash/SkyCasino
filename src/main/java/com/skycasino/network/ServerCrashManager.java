package com.skycasino.network;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.ludik.LudikManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerCrashManager {
    public static final int PHASE_IDLE = 0;
    public static final int PHASE_RUNNING = 1;
    public static final int PHASE_CASHED_OUT = 2;
    public static final int PHASE_CRASHED = 3;

    public static final double GROWTH_RATE = 0.24; // exp(0.24 * seconds)

    public static class Session {
        public long bet = 0;
        public double crashPoint = 1.0;
        public long startTimeMs = 0;
        public int phase = PHASE_IDLE;
        public BlockPos pos = BlockPos.ZERO;
    }

    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();

    public static double currentMultiplier(long startTimeMs) {
        double elapsedSec = Math.max(0.0, (System.currentTimeMillis() - startTimeMs) / 1000.0);
        return Math.max(1.0, Math.exp(elapsedSec * GROWTH_RATE));
    }

    private static double rollCrashPoint() {
        double r = RANDOM.nextDouble();
        if (r < 0.04) return 1.00; // 4% instant crash
        double raw = 0.96 / (1.0 - r);
        double clamped = Math.min(50.0, Math.max(1.05, raw));
        return Math.round(clamped * 100.0) / 100.0;
    }

    public static void handleAction(ServerPlayer player, int action, long bet, BlockPos pos) {
        Session s = SESSIONS.computeIfAbsent(player.getUUID(), id -> new Session());
        s.pos = pos;

        if (action == CrashActionPacket.ACTION_START) {
            if (s.phase == PHASE_RUNNING) return;
            if (bet < 1 || bet > 10_000_000) return;
            if (!ChipEconomy.deductChips(player, bet)) return;
            LudikManager.onBetPlaced(player, bet);

            s.bet = bet;
            s.crashPoint = rollCrashPoint();
            s.startTimeMs = System.currentTimeMillis();
            s.phase = PHASE_RUNNING;

            ModNetwork.sendToPlayer(new CrashStatePacket(
                    PHASE_RUNNING, 1.0, 0.0, s.bet, 0, ChipEconomy.getTotalChips(player)
            ), player);
        } else if (action == CrashActionPacket.ACTION_POLL) {
            if (s.phase != PHASE_RUNNING) return;
            double mult = currentMultiplier(s.startTimeMs);
            if (mult >= s.crashPoint) {
                s.phase = PHASE_CRASHED;
                LudikManager.processOutcomeAndCredit(player, s.bet, 0, s.pos);
                CasinoEffects.playLoseEffect(player, s.pos);
                ModNetwork.sendToPlayer(new CrashStatePacket(
                        PHASE_CRASHED, s.crashPoint, s.crashPoint, s.bet, 0, ChipEconomy.getTotalChips(player)
                ), player);
            } else {
                ModNetwork.sendToPlayer(new CrashStatePacket(
                        PHASE_RUNNING, mult, 0.0, s.bet, 0, ChipEconomy.getTotalChips(player)
                ), player);
            }
        } else if (action == CrashActionPacket.ACTION_CASHOUT) {
            if (s.phase != PHASE_RUNNING) return;
            double mult = Math.round(currentMultiplier(s.startTimeMs) * 100.0) / 100.0;
            if (mult >= s.crashPoint) {
                s.phase = PHASE_CRASHED;
                LudikManager.processOutcomeAndCredit(player, s.bet, 0, s.pos);
                CasinoEffects.playLoseEffect(player, s.pos);
                ModNetwork.sendToPlayer(new CrashStatePacket(
                        PHASE_CRASHED, s.crashPoint, s.crashPoint, s.bet, 0, ChipEconomy.getTotalChips(player)
                ), player);
            } else {
                s.phase = PHASE_CASHED_OUT;
                long payout = LudikManager.processOutcomeAndCredit(player, s.bet, Math.round(s.bet * mult), s.pos);
                CasinoEffects.playWinEffect(player, s.pos, mult >= 5.0);
                ModNetwork.sendToPlayer(new CrashStatePacket(
                        PHASE_CASHED_OUT, mult, s.crashPoint, s.bet, payout, ChipEconomy.getTotalChips(player)
                ), player);
            }
        }
    }
}
