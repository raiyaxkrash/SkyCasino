package com.skycasino.network;

import com.skycasino.client.screen.MinesScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MinesStatePacket {
    public final int phase;
    public final int mineCount;
    public final int revealedMask;
    public final int mineMask;
    public final double multiplier;
    public final double nextMultiplier;
    public final long bet;
    public final long payout;
    public final long balance;

    public MinesStatePacket(int phase, int mineCount, int revealedMask, int mineMask,
                            double multiplier, double nextMultiplier, long bet, long payout, long balance) {
        this.phase = phase;
        this.mineCount = mineCount;
        this.revealedMask = revealedMask;
        this.mineMask = mineMask;
        this.multiplier = multiplier;
        this.nextMultiplier = nextMultiplier;
        this.bet = bet;
        this.payout = payout;
        this.balance = balance;
    }

    public static void encode(MinesStatePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.phase);
        buf.writeInt(msg.mineCount);
        buf.writeInt(msg.revealedMask);
        buf.writeInt(msg.mineMask);
        buf.writeDouble(msg.multiplier);
        buf.writeDouble(msg.nextMultiplier);
        buf.writeLong(msg.bet);
        buf.writeLong(msg.payout);
        buf.writeLong(msg.balance);
    }

    public static MinesStatePacket decode(FriendlyByteBuf buf) {
        return new MinesStatePacket(
                buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readDouble(), buf.readDouble(), buf.readLong(), buf.readLong(), buf.readLong()
        );
    }

    public static void handle(MinesStatePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (Minecraft.getInstance().screen instanceof MinesScreen screen) {
                screen.onStateUpdate(msg);
            }
        });
    }
}
