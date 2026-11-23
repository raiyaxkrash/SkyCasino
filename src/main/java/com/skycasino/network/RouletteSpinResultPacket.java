package com.skycasino.network;

import com.skycasino.client.screen.RouletteScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RouletteSpinResultPacket {
    public final int winningNumber;
    public final long payout;
    public final long newBalance;

    public RouletteSpinResultPacket(int winningNumber, long payout, long newBalance) {
        this.winningNumber = winningNumber;
        this.payout = payout;
        this.newBalance = newBalance;
    }

    public static void encode(RouletteSpinResultPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.winningNumber);
        buf.writeLong(msg.payout);
        buf.writeLong(msg.newBalance);
    }

    public static RouletteSpinResultPacket decode(FriendlyByteBuf buf) {
        return new RouletteSpinResultPacket(buf.readInt(), buf.readLong(), buf.readLong());
    }

    public static void handle(RouletteSpinResultPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (Minecraft.getInstance().screen instanceof RouletteScreen screen) {
                screen.onSpinResult(msg);
            }
        });
    }
}
