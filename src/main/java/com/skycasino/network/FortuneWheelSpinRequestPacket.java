package com.skycasino.network;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.ludik.LudikManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

public class FortuneWheelSpinRequestPacket {
    // 12 sectors around the wheel
    public static final double[] MULTIPLIERS = {
            0.0, 2.0, 0.5, 3.0, 0.0, 5.0,
            0.5, 1.5, 0.0, 10.0, 0.5, 25.0
    };
    private static final int[] WEIGHTS = {
            16, 14, 14, 9, 15, 6,
            10, 8, 5, 2, 0, 1 // wait let's give sector 10 weight too!
    };
    private static final int[] SECTOR_WEIGHTS = {
            14, 13, 12, 8, 14, 5,
            11, 10, 8, 2, 2, 1
    }; // sum = 100

    private final long bet;
    private final BlockPos pos;

    public FortuneWheelSpinRequestPacket(long bet, BlockPos pos) {
        this.bet = bet;
        this.pos = pos;
    }

    public static void encode(FortuneWheelSpinRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.bet);
        buf.writeBlockPos(msg.pos);
    }

    public static FortuneWheelSpinRequestPacket decode(FriendlyByteBuf buf) {
        return new FortuneWheelSpinRequestPacket(buf.readLong(), buf.readBlockPos());
    }

    public static void handle(FortuneWheelSpinRequestPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer player = ctxSupplier.get().getSender();
        if (player == null) return;
        if (msg.bet < 1 || msg.bet > 10_000_000) return;

        if (!ChipEconomy.deductChips(player, msg.bet)) return;
        LudikManager.onBetPlaced(player, msg.bet);

        int r = new Random().nextInt(100);
        int sum = 0;
        int sector = 0;
        for (int i = 0; i < SECTOR_WEIGHTS.length; i++) {
            sum += SECTOR_WEIGHTS[i];
            if (r < sum) {
                sector = i;
                break;
            }
        }

        double mult = MULTIPLIERS[sector];
        long rawPayout = Math.round(msg.bet * mult);
        long payout = LudikManager.processOutcomeAndCredit(player, msg.bet, rawPayout, msg.pos);
        if (rawPayout > 0) {
            CasinoEffects.playWinEffect(player, msg.pos, mult >= 10.0);
        } else {
            CasinoEffects.playLoseEffect(player, msg.pos);
        }

        long balance = ChipEconomy.getTotalChips(player);
        ModNetwork.sendToPlayer(new FortuneWheelSpinResultPacket(sector, mult, payout, balance), player);
    }
}
