package com.skycasino.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CrashActionPacket {
    public static final int ACTION_START = 0;
    public static final int ACTION_CASHOUT = 1;
    public static final int ACTION_POLL = 2;

    private final int action;
    private final long bet;
    private final BlockPos pos;

    public CrashActionPacket(int action, long bet, BlockPos pos) {
        this.action = action;
        this.bet = bet;
        this.pos = pos;
    }

    public static void encode(CrashActionPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.action);
        buf.writeLong(msg.bet);
        buf.writeBlockPos(msg.pos);
    }

    public static CrashActionPacket decode(FriendlyByteBuf buf) {
        return new CrashActionPacket(buf.readInt(), buf.readLong(), buf.readBlockPos());
    }

    public static void handle(CrashActionPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer player = ctxSupplier.get().getSender();
        if (player == null) return;
        ServerCrashManager.handleAction(player, msg.action, msg.bet, msg.pos);
    }
}
