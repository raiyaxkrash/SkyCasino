package com.skycasino.network;

import com.skycasino.ludik.LudikManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LudikActionPacket {
    public static final int ACTION_SYNC = 0;
    public static final int ACTION_LOAN = 1;
    public static final int ACTION_REPAY = 2;
    public static final int ACTION_PAWN = 3;
    public static final int ACTION_DODEP = 4;

    private final int action;
    private final long amount;

    public LudikActionPacket(int action, long amount) {
        this.action = action;
        this.amount = amount;
    }

    public static void encode(LudikActionPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.action);
        buf.writeLong(msg.amount);
    }

    public static LudikActionPacket decode(FriendlyByteBuf buf) {
        return new LudikActionPacket(buf.readInt(), buf.readLong());
    }

    public static void handle(LudikActionPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer player = ctxSupplier.get().getSender();
        if (player == null) return;

        switch (msg.action) {
            case ACTION_SYNC -> LudikManager.syncToClient(player);
            case ACTION_LOAN -> LudikManager.takeLoan(player, msg.amount);
            case ACTION_REPAY -> LudikManager.repayDebt(player);
            case ACTION_PAWN -> LudikManager.pawnHeldItem(player);
            case ACTION_DODEP -> LudikManager.claimDodep(player);
        }
    }
}
