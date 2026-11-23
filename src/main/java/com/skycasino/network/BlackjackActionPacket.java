package com.skycasino.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BlackjackActionPacket {
    public static final int ACTION_DEAL = 0;
    public static final int ACTION_HIT = 1;
    public static final int ACTION_STAND = 2;
    public static final int ACTION_DOUBLE = 3;

    private final int action;
    private final long bet;
    private final BlockPos pos;

    public BlackjackActionPacket(int action, long bet, BlockPos pos) {
        this.action = action;
        this.bet = bet;
        this.pos = pos;
    }

    public static void encode(BlackjackActionPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.action);
        buf.writeLong(msg.bet);
        buf.writeBlockPos(msg.pos);
    }

    public static BlackjackActionPacket decode(FriendlyByteBuf buf) {
        return new BlackjackActionPacket(buf.readInt(), buf.readLong(), buf.readBlockPos());
    }

    public static void handle(BlackjackActionPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer player = ctxSupplier.get().getSender();
        if (player == null) return;
        ServerBlackjackManager.handleAction(player, msg.action, msg.bet, msg.pos);
    }
}
