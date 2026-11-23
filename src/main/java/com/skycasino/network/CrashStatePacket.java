package com.skycasino.network;

import com.skycasino.client.screen.CrashScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CrashStatePacket {
    public final int phase;
    public final double currentMultiplier;
    public final double crashPoint;
    public final long bet;
    public final long payout;
    public final long balance;

    public CrashStatePacket(int phase, double currentMultiplier, double crashPoint, long bet, long payout, long balance) {
        this.phase = phase;
        this.currentMultiplier = currentMultiplier;
        this.crashPoint = crashPoint;
        this.bet = bet;
        this.payout = payout;
        this.balance = balance;
    }

    public static void encode(CrashStatePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.phase);
        buf.writeDouble(msg.currentMultiplier);
        buf.writeDouble(msg.crashPoint);
        buf.writeLong(msg.bet);
        buf.writeLong(msg.payout);
        buf.writeLong(msg.balance);
    }

    public static CrashStatePacket decode(FriendlyByteBuf buf) {
        return new CrashStatePacket(buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readLong(), buf.readLong(), buf.readLong());
    }

    public static void handle(CrashStatePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (Minecraft.getInstance().screen instanceof CrashScreen screen) {
                screen.onStateUpdate(msg);
            }
        });
    }
}
