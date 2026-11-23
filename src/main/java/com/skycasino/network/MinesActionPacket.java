package com.skycasino.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MinesActionPacket {
    public static final int ACTION_START = 0;
    public static final int ACTION_CLICK = 1;
    public static final int ACTION_CASHOUT = 2;

    private final int action;
    private final int param; // mineCount on START, tileIndex on CLICK
    private final long bet;
    private final BlockPos pos;

    public MinesActionPacket(int action, int param, long bet, BlockPos pos) {
        this.action = action;
        this.param = param;
        this.bet = bet;
        this.pos = pos;
    }

    public static void encode(MinesActionPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.action);
        buf.writeInt(msg.param);
        buf.writeLong(msg.bet);
        buf.writeBlockPos(msg.pos);
    }

    public static MinesActionPacket decode(FriendlyByteBuf buf) {
        return new MinesActionPacket(buf.readInt(), buf.readInt(), buf.readLong(), buf.readBlockPos());
    }

    public static void handle(MinesActionPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer player = ctxSupplier.get().getSender();
        if (player == null) return;
        ServerMinesManager.handleAction(player, msg.action, msg.param, msg.bet, msg.pos);
    }
}
