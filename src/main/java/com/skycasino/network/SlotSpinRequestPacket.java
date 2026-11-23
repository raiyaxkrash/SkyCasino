package com.skycasino.network;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.ludik.LudikManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

public class SlotSpinRequestPacket {
    // Symbols: 0=Cherry, 1=Lemon, 2=Bell, 3=Emerald, 4=Diamond, 5=Star, 6=Lucky Seven
    private static final int[] WEIGHTS = {26, 24, 18, 13, 9, 6, 4};
    private static final int TOTAL_WEIGHT = 100;
    private static final double[] LINE_MULT_3 = {3.0, 4.0, 6.0, 10.0, 16.0, 25.0, 50.0};

    private final long bet;
    private final BlockPos pos;

    public SlotSpinRequestPacket(long bet, BlockPos pos) {
        this.bet = bet;
        this.pos = pos;
    }

    public static void encode(SlotSpinRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.bet);
        buf.writeBlockPos(msg.pos);
    }

    public static SlotSpinRequestPacket decode(FriendlyByteBuf buf) {
        return new SlotSpinRequestPacket(buf.readLong(), buf.readBlockPos());
    }

    private static int rollSymbol(Random random) {
        int r = random.nextInt(TOTAL_WEIGHT);
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i];
            if (r < sum) return i;
        }
        return 0;
    }

    public static void handle(SlotSpinRequestPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer player = ctxSupplier.get().getSender();
        if (player == null) return;
        if (msg.bet < 1 || msg.bet > 10_000_000) return;

        if (!ChipEconomy.deductChips(player, msg.bet)) {
            return;
        }
        LudikManager.onBetPlaced(player, msg.bet);

        Random random = new Random();
        int[] grid = new int[9]; // 3 rows x 3 cols: index = row * 3 + col
        for (int i = 0; i < 9; i++) {
            grid[i] = rollSymbol(random);
        }

        // Check 5 paylines: 3 horizontal rows + 2 diagonals
        int[][] lines = {
                {0, 1, 2}, // Top row
                {3, 4, 5}, // Middle row (main)
                {6, 7, 8}, // Bottom row
                {0, 4, 8}, // Diag TL-BR
                {6, 4, 2}  // Diag BL-TR
        };

        int winMask = 0;
        double totalMultiplier = 0.0;
        boolean jackpot = false;

        for (int l = 0; l < lines.length; l++) {
            int a = grid[lines[l][0]];
            int b = grid[lines[l][1]];
            int c = grid[lines[l][2]];
            if (a == b && b == c) {
                winMask |= (1 << l);
                double mult = LINE_MULT_3[a];
                if (l == 1) {
                    // Middle line pays full, other lines pay 60%
                    totalMultiplier += mult;
                } else {
                    totalMultiplier += mult * 0.6;
                }
                if (a == 6) {
                    jackpot = true;
                }
            } else if (l == 1 && (a == b || b == c || a == c)) {
                // Pair on the center line gives a small return
                int matched = (a == b) ? a : c;
                if (matched >= 3) {
                    winMask |= (1 << 5); // center pair bit
                    totalMultiplier += (matched == 6) ? 2.0 : 1.0;
                }
            }
        }

        long rawPayout = Math.round(msg.bet * totalMultiplier);
        long payout = LudikManager.processOutcomeAndCredit(player, msg.bet, rawPayout, msg.pos);
        if (rawPayout > 0) {
            CasinoEffects.playWinEffect(player, msg.pos, jackpot || rawPayout >= msg.bet * 10);
        } else {
            CasinoEffects.playLoseEffect(player, msg.pos);
        }

        long newBalance = ChipEconomy.getTotalChips(player);
        ModNetwork.sendToPlayer(new SlotSpinResultPacket(grid, payout, winMask, jackpot, newBalance), player);
    }
}
