package com.skycasino.network;

import com.skycasino.client.screen.SlotMachineScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SlotSpinResultPacket {
    public final int[] grid;
    public final long payout;
    public final int winMask;
    public final boolean jackpot;
    public final long newBalance;

    public SlotSpinResultPacket(int[] grid, long payout, int winMask, boolean jackpot, long newBalance) {
        this.grid = grid;
        this.payout = payout;
        this.winMask = winMask;
        this.jackpot = jackpot;
        this.newBalance = newBalance;
    }

    public static void encode(SlotSpinResultPacket msg, FriendlyByteBuf buf) {
        for (int i = 0; i < 9; i++) {
            buf.writeInt(msg.grid[i]);
        }
        buf.writeLong(msg.payout);
        buf.writeInt(msg.winMask);
        buf.writeBoolean(msg.jackpot);
        buf.writeLong(msg.newBalance);
    }

    public static SlotSpinResultPacket decode(FriendlyByteBuf buf) {
        int[] grid = new int[9];
        for (int i = 0; i < 9; i++) {
            grid[i] = buf.readInt();
        }
        return new SlotSpinResultPacket(grid, buf.readLong(), buf.readInt(), buf.readBoolean(), buf.readLong());
    }

    public static void handle(SlotSpinResultPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (Minecraft.getInstance().screen instanceof SlotMachineScreen screen) {
                screen.onSpinResult(msg);
            }
        });
    }
}
