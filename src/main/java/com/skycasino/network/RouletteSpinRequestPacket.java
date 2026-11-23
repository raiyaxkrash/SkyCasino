package com.skycasino.network;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.ludik.LudikManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

public class RouletteSpinRequestPacket {
    public static final int BET_RED = 0;
    public static final int BET_BLACK = 1;
    public static final int BET_EVEN = 2;
    public static final int BET_ODD = 3;
    public static final int BET_LOW = 4;    // 1-18
    public static final int BET_HIGH = 5;   // 19-36
    public static final int BET_DOZEN1 = 6; // 1-12
    public static final int BET_DOZEN2 = 7; // 13-24
    public static final int BET_DOZEN3 = 8; // 25-36
    public static final int BET_STRAIGHT = 9; // exact number 0..36

    public static final Set<Integer> RED_NUMBERS = Set.of(
            1, 3, 5, 7, 9, 12, 14, 16, 18,
            19, 21, 23, 25, 27, 30, 32, 34, 36
    );

    private final int betType;
    private final int betNumber;
    private final long betAmount;
    private final BlockPos pos;

    public RouletteSpinRequestPacket(int betType, int betNumber, long betAmount, BlockPos pos) {
        this.betType = betType;
        this.betNumber = betNumber;
        this.betAmount = betAmount;
        this.pos = pos;
    }

    public static void encode(RouletteSpinRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.betType);
        buf.writeInt(msg.betNumber);
        buf.writeLong(msg.betAmount);
        buf.writeBlockPos(msg.pos);
    }

    public static RouletteSpinRequestPacket decode(FriendlyByteBuf buf) {
        return new RouletteSpinRequestPacket(buf.readInt(), buf.readInt(), buf.readLong(), buf.readBlockPos());
    }

    public static void handle(RouletteSpinRequestPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer player = ctxSupplier.get().getSender();
        if (player == null) return;
        if (msg.betAmount < 1 || msg.betAmount > 10_000_000) return;

        if (!ChipEconomy.deductChips(player, msg.betAmount)) return;
        LudikManager.onBetPlaced(player, msg.betAmount);

        int number = new Random().nextInt(37); // 0..36
        boolean isRed = RED_NUMBERS.contains(number);
        boolean isBlack = number != 0 && !isRed;

        long multiplier = 0;
        switch (msg.betType) {
            case BET_RED -> { if (isRed) multiplier = 2; }
            case BET_BLACK -> { if (isBlack) multiplier = 2; }
            case BET_EVEN -> { if (number != 0 && number % 2 == 0) multiplier = 2; }
            case BET_ODD -> { if (number != 0 && number % 2 == 1) multiplier = 2; }
            case BET_LOW -> { if (number >= 1 && number <= 18) multiplier = 2; }
            case BET_HIGH -> { if (number >= 19 && number <= 36) multiplier = 2; }
            case BET_DOZEN1 -> { if (number >= 1 && number <= 12) multiplier = 3; }
            case BET_DOZEN2 -> { if (number >= 13 && number <= 24) multiplier = 3; }
            case BET_DOZEN3 -> { if (number >= 25 && number <= 36) multiplier = 3; }
            case BET_STRAIGHT -> { if (number == msg.betNumber) multiplier = 36; }
        }

        long rawPayout = msg.betAmount * multiplier;
        long payout = LudikManager.processOutcomeAndCredit(player, msg.betAmount, rawPayout, msg.pos);
        if (rawPayout > 0) {
            CasinoEffects.playWinEffect(player, msg.pos, multiplier >= 36);
        } else {
            CasinoEffects.playLoseEffect(player, msg.pos);
        }

        long balance = ChipEconomy.getTotalChips(player);
        ModNetwork.sendToPlayer(new RouletteSpinResultPacket(number, payout, balance), player);
    }
}
