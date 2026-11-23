package com.skycasino.network;

import com.skycasino.client.screen.BlackjackScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BlackjackStatePacket {
    public final List<Integer> playerHand;
    public final List<Integer> dealerHand;
    public final int phase;
    public final String statusKey;
    public final long currentBet;
    public final long payout;
    public final long newBalance;

    public BlackjackStatePacket(List<Integer> playerHand, List<Integer> dealerHand, int phase,
                                String statusKey, long currentBet, long payout, long newBalance) {
        this.playerHand = playerHand;
        this.dealerHand = dealerHand;
        this.phase = phase;
        this.statusKey = statusKey;
        this.currentBet = currentBet;
        this.payout = payout;
        this.newBalance = newBalance;
    }

    public static void encode(BlackjackStatePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.playerHand.size());
        for (int c : msg.playerHand) buf.writeInt(c);
        buf.writeInt(msg.dealerHand.size());
        for (int c : msg.dealerHand) buf.writeInt(c);
        buf.writeInt(msg.phase);
        buf.writeUtf(msg.statusKey);
        buf.writeLong(msg.currentBet);
        buf.writeLong(msg.payout);
        buf.writeLong(msg.newBalance);
    }

    public static BlackjackStatePacket decode(FriendlyByteBuf buf) {
        int pSize = buf.readInt();
        List<Integer> pHand = new ArrayList<>(pSize);
        for (int i = 0; i < pSize; i++) pHand.add(buf.readInt());
        int dSize = buf.readInt();
        List<Integer> dHand = new ArrayList<>(dSize);
        for (int i = 0; i < dSize; i++) dHand.add(buf.readInt());
        return new BlackjackStatePacket(pHand, dHand, buf.readInt(), buf.readUtf(), buf.readLong(), buf.readLong(), buf.readLong());
    }

    public static void handle(BlackjackStatePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (Minecraft.getInstance().screen instanceof BlackjackScreen screen) {
                screen.onStateUpdate(msg);
            }
        });
    }
}
