package com.skycasino.network;

import com.skycasino.economy.ChipEconomy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PrizeShopBuyPacket {
    private final int itemIndex;

    public PrizeShopBuyPacket(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public static void encode(PrizeShopBuyPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.itemIndex);
    }

    public static PrizeShopBuyPacket decode(FriendlyByteBuf buf) {
        return new PrizeShopBuyPacket(buf.readInt());
    }

    public static void handle(PrizeShopBuyPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer player = ctxSupplier.get().getSender();
        if (player == null) return;

        List<PrizeShopCatalog.ShopItem> items = PrizeShopCatalog.getShopItems();
        if (msg.itemIndex < 0 || msg.itemIndex >= items.size()) return;

        PrizeShopCatalog.ShopItem entry = items.get(msg.itemIndex);
        if (ChipEconomy.deductChips(player, entry.price())) {
            ChipEconomy.giveItem(player, entry.item(), entry.count());
            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.3F);
        }
    }
}
