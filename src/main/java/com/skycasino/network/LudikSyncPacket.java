package com.skycasino.network;

import com.skycasino.client.screen.LudikPanelScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LudikSyncPacket {
    public final int addiction;
    public final int winStreak;
    public final int lossStreak;
    public final long debt;
    public final int debtSecsLeft;
    public final int withdrawalStage;
    public final long pawnValue;

    public LudikSyncPacket(int addiction, int winStreak, int lossStreak, long debt,
                           int debtSecsLeft, int withdrawalStage, long pawnValue) {
        this.addiction = addiction;
        this.winStreak = winStreak;
        this.lossStreak = lossStreak;
        this.debt = debt;
        this.debtSecsLeft = debtSecsLeft;
        this.withdrawalStage = withdrawalStage;
        this.pawnValue = pawnValue;
    }

    public static void encode(LudikSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.addiction);
        buf.writeInt(msg.winStreak);
        buf.writeInt(msg.lossStreak);
        buf.writeLong(msg.debt);
        buf.writeInt(msg.debtSecsLeft);
        buf.writeInt(msg.withdrawalStage);
        buf.writeLong(msg.pawnValue);
    }

    public static LudikSyncPacket decode(FriendlyByteBuf buf) {
        return new LudikSyncPacket(
                buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readLong(), buf.readInt(), buf.readInt(), buf.readLong()
        );
    }

    public static void handle(LudikSyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (Minecraft.getInstance().screen instanceof LudikPanelScreen screen) {
                screen.onSync(msg);
            }
        });
    }
}
