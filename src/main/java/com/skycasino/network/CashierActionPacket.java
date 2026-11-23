package com.skycasino.network;

import com.skycasino.economy.ChipEconomy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class CashierActionPacket {
    public static final int ACTION_BUY = 0;
    public static final int ACTION_SELL = 1;
    public static final int ACTION_CONSOLIDATE = 2;

    private final int action;
    private final int rateIndex;
    private final int count; // 1, 10, or -1 for ALL

    public CashierActionPacket(int action, int rateIndex, int count) {
        this.action = action;
        this.rateIndex = rateIndex;
        this.count = count;
    }

    public static void encode(CashierActionPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.action);
        buf.writeInt(msg.rateIndex);
        buf.writeInt(msg.count);
    }

    public static CashierActionPacket decode(FriendlyByteBuf buf) {
        return new CashierActionPacket(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(CashierActionPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer player = ctxSupplier.get().getSender();
        if (player == null) return;

        if (msg.action == ACTION_CONSOLIDATE) {
            ChipEconomy.consolidateChips(player);
            player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.3F);
            return;
        }

        List<ChipEconomy.ExchangeEntry> entries = ChipEconomy.getExchangeEntries();
        if (msg.rateIndex < 0 || msg.rateIndex >= entries.size()) return;
        ChipEconomy.ExchangeEntry entry = entries.get(msg.rateIndex);

        if (msg.action == ACTION_BUY) {
            int available = ChipEconomy.countItem(player, entry.item());
            int toExchange = msg.count < 0 ? available : Math.min(msg.count, available);
            if (toExchange > 0 && ChipEconomy.removeItem(player, entry.item(), toExchange)) {
                ChipEconomy.giveChips(player, entry.chipValue() * toExchange);
                player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.2F);
            }
        } else if (msg.action == ACTION_SELL) {
            long totalChips = ChipEconomy.getTotalChips(player);
            int maxAffordable = (int) Math.min(totalChips / entry.chipValue(), 1024);
            int toExchange = msg.count < 0 ? maxAffordable : Math.min(msg.count, maxAffordable);
            if (toExchange > 0 && ChipEconomy.deductChips(player, entry.chipValue() * toExchange)) {
                ChipEconomy.giveItem(player, entry.item(), toExchange);
                player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 0.9F);
            }
        }
    }
}
