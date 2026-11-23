package com.skycasino.network;

import com.skycasino.client.screen.FortuneWheelScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FortuneWheelSpinResultPacket {
    public final int sectorIndex;
    public final double multiplier;
    public final long payout;
    public final long newBalance;

    public FortuneWheelSpinResultPacket(int sectorIndex, double multiplier, long payout, long newBalance) {
        this.sectorIndex = sectorIndex;
        this.multiplier = multiplier;
        this.payout = payout;
        this.newBalance = newBalance;
    }

    public static void encode(FortuneWheelSpinResultPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.sectorIndex);
        buf.writeDouble(msg.multiplier);
        buf.writeLong(msg.payout);
        buf.writeLong(msg.newBalance);
    }

    public static FortuneWheelSpinResultPacket decode(FriendlyByteBuf buf) {
        return new FortuneWheelSpinResultPacket(buf.readInt(), buf.readDouble(), buf.readLong(), buf.readLong());
    }

    public static void handle(FortuneWheelSpinResultPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (Minecraft.getInstance().screen instanceof FortuneWheelScreen screen) {
                screen.onSpinResult(msg);
            }
        });
    }
}
