package com.skycasino.network;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.ludik.LudikManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerBlackjackManager {
    public static final int PHASE_IDLE = 0;
    public static final int PHASE_PLAYER_TURN = 1;
    public static final int PHASE_FINISHED = 2;

    // Card encoding: 0..51 -> suit = card / 13 (0=Spades,1=Hearts,2=Diamonds,3=Clubs), rank = card % 13 (0=A, 1=2..9=10, 10=J, 11=Q, 12=K)
    public static class Session {
        public final List<Integer> deck = new ArrayList<>();
        public final List<Integer> playerHand = new ArrayList<>();
        public final List<Integer> dealerHand = new ArrayList<>();
        public long bet = 0;
        public int phase = PHASE_IDLE;
        public BlockPos pos = BlockPos.ZERO;

        public void resetDeck() {
            deck.clear();
            for (int i = 0; i < 52; i++) deck.add(i);
            Collections.shuffle(deck, new Random());
        }

        public int draw() {
            if (deck.isEmpty()) resetDeck();
            return deck.remove(deck.size() - 1);
        }
    }

    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    public static int handValue(List<Integer> hand) {
        int total = 0;
        int aces = 0;
        for (int card : hand) {
            int rank = card % 13;
            if (rank == 0) {
                aces++;
                total += 11;
            } else if (rank >= 10) {
                total += 10;
            } else {
                total += (rank + 1);
            }
        }
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }

    public static void handleAction(ServerPlayer player, int action, long bet, BlockPos pos) {
        Session session = SESSIONS.computeIfAbsent(player.getUUID(), id -> new Session());
        session.pos = pos;

        switch (action) {
            case BlackjackActionPacket.ACTION_DEAL -> doDeal(player, session, bet);
            case BlackjackActionPacket.ACTION_HIT -> doHit(player, session);
            case BlackjackActionPacket.ACTION_STAND -> doStand(player, session);
            case BlackjackActionPacket.ACTION_DOUBLE -> doDouble(player, session);
        }
    }

    private static void doDeal(ServerPlayer player, Session s, long bet) {
        if (bet < 1 || bet > 10_000_000) return;
        if (s.phase == PHASE_PLAYER_TURN) return;
        if (!ChipEconomy.deductChips(player, bet)) return;
        LudikManager.onBetPlaced(player, bet);

        s.resetDeck();
        s.playerHand.clear();
        s.dealerHand.clear();
        s.bet = bet;
        s.phase = PHASE_PLAYER_TURN;

        s.playerHand.add(s.draw());
        s.dealerHand.add(s.draw());
        s.playerHand.add(s.draw());
        s.dealerHand.add(s.draw());

        int pVal = handValue(s.playerHand);
        int dVal = handValue(s.dealerHand);

        if (pVal == 21) {
            s.phase = PHASE_FINISHED;
            if (dVal == 21) {
                // Push
                ChipEconomy.giveChips(player, s.bet);
                sendState(player, s, false, "push", s.bet);
            } else {
                // Natural Blackjack pays 2.5x!
                long payout = LudikManager.processOutcomeAndCredit(player, s.bet, Math.round(s.bet * 2.5), s.pos);
                CasinoEffects.playWinEffect(player, s.pos, true);
                sendState(player, s, false, "blackjack", payout);
            }
        } else {
            sendState(player, s, true, "your_turn", 0);
        }
    }

    private static void doHit(ServerPlayer player, Session s) {
        if (s.phase != PHASE_PLAYER_TURN) return;
        s.playerHand.add(s.draw());
        int pVal = handValue(s.playerHand);
        if (pVal > 21) {
            s.phase = PHASE_FINISHED;
            LudikManager.processOutcomeAndCredit(player, s.bet, 0, s.pos);
            CasinoEffects.playLoseEffect(player, s.pos);
            sendState(player, s, false, "bust", 0);
        } else if (pVal == 21) {
            doStand(player, s);
        } else {
            sendState(player, s, true, "your_turn", 0);
        }
    }

    private static void doDouble(ServerPlayer player, Session s) {
        if (s.phase != PHASE_PLAYER_TURN || s.playerHand.size() != 2) return;
        if (!ChipEconomy.deductChips(player, s.bet)) return;
        s.bet *= 2;
        s.playerHand.add(s.draw());
        int pVal = handValue(s.playerHand);
        if (pVal > 21) {
            s.phase = PHASE_FINISHED;
            LudikManager.processOutcomeAndCredit(player, s.bet, 0, s.pos);
            CasinoEffects.playLoseEffect(player, s.pos);
            sendState(player, s, false, "bust", 0);
        } else {
            doStand(player, s);
        }
    }

    private static void doStand(ServerPlayer player, Session s) {
        if (s.phase != PHASE_PLAYER_TURN) return;
        s.phase = PHASE_FINISHED;

        while (handValue(s.dealerHand) < 17) {
            s.dealerHand.add(s.draw());
        }

        int pVal = handValue(s.playerHand);
        int dVal = handValue(s.dealerHand);

        if (dVal > 21 || pVal > dVal) {
            long payout = LudikManager.processOutcomeAndCredit(player, s.bet, s.bet * 2, s.pos);
            CasinoEffects.playWinEffect(player, s.pos, payout >= 1000);
            sendState(player, s, false, dVal > 21 ? "dealer_bust" : "win", payout);
        } else if (pVal == dVal) {
            ChipEconomy.giveChips(player, s.bet);
            sendState(player, s, false, "push", s.bet);
        } else {
            LudikManager.processOutcomeAndCredit(player, s.bet, 0, s.pos);
            CasinoEffects.playLoseEffect(player, s.pos);
            sendState(player, s, false, "lose", 0);
        }
    }

    private static void sendState(ServerPlayer player, Session s, boolean hideDealerHole, String statusKey, long payout) {
        List<Integer> visibleDealer = new ArrayList<>();
        if (hideDealerHole && s.dealerHand.size() >= 2) {
            visibleDealer.add(s.dealerHand.get(0));
            visibleDealer.add(-1); // Hidden hole card
        } else {
            visibleDealer.addAll(s.dealerHand);
        }
        long balance = ChipEconomy.getTotalChips(player);
        ModNetwork.sendToPlayer(new BlackjackStatePacket(
                new ArrayList<>(s.playerHand),
                visibleDealer,
                s.phase,
                statusKey,
                s.bet,
                payout,
                balance
        ), player);
    }
}
