package com.skycasino.network;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class LootCaseOpenRequestPacket {
    private final boolean vip;
    private final BlockPos pos;

    public LootCaseOpenRequestPacket(boolean vip, BlockPos pos) {
        this.vip = vip;
        this.pos = pos;
    }

    public static void encode(LootCaseOpenRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.vip);
        buf.writeBlockPos(msg.pos);
    }

    public static LootCaseOpenRequestPacket decode(FriendlyByteBuf buf) {
        return new LootCaseOpenRequestPacket(buf.readBoolean(), buf.readBlockPos());
    }

    public static void handle(LootCaseOpenRequestPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        ServerPlayer player = ctxSupplier.get().getSender();
        if (player == null) return;

        Item caseItem = msg.vip ? ModItems.LOOT_CASE_VIP.get() : ModItems.LOOT_CASE_CLASSIC.get();
        if (!ChipEconomy.removeItem(player, caseItem, 1)) {
            return;
        }

        Random random = new Random();
        int winIdx = PrizeShopCatalog.rollRewardIndex(msg.vip, random);
        List<PrizeShopCatalog.CaseReward> rewards = PrizeShopCatalog.getCaseRewards(msg.vip);
        PrizeShopCatalog.CaseReward reward = rewards.get(winIdx);

        if (reward.bonusChips() > 0) {
            ChipEconomy.giveChips(player, reward.bonusChips());
        } else {
            ChipEconomy.giveItem(player, reward.item(), reward.count());
        }

        if (reward.rarity() >= 2) {
            CasinoEffects.playWinEffect(player, msg.pos, reward.rarity() >= 3);
        }

        ModNetwork.sendToPlayer(new LootCaseOpenResultPacket(msg.vip, winIdx), player);
    }
}
