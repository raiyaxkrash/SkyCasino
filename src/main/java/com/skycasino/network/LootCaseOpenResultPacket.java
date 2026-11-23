package com.skycasino.network;

import com.skycasino.client.screen.LootCaseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LootCaseOpenResultPacket {
    public final boolean vip;
    public final int winIndex;

    public LootCaseOpenResultPacket(boolean vip, int winIndex) {
        this.vip = vip;
        this.winIndex = winIndex;
    }

    public static void encode(LootCaseOpenResultPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.vip);
        buf.writeInt(msg.winIndex);
    }

    public static LootCaseOpenResultPacket decode(FriendlyByteBuf buf) {
        return new LootCaseOpenResultPacket(buf.readBoolean(), buf.readInt());
    }

    public static void handle(LootCaseOpenResultPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (Minecraft.getInstance().screen instanceof LootCaseScreen screen) {
                screen.onCaseResult(msg);
            }
        });
    }
}
