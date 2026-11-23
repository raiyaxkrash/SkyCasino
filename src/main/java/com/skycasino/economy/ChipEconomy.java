package com.skycasino.economy;

import com.skycasino.item.ChipItem;
import com.skycasino.item.VipCardItem;
import com.skycasino.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChipEconomy {

    public record ExchangeEntry(Item item, long chipValue, String displayKey, boolean isAtm) {}

    private static List<ExchangeEntry> cachedEntries = null;

    public static List<ExchangeEntry> getExchangeEntries() {
        if (cachedEntries != null) return cachedEntries;

        List<ExchangeEntry> list = new ArrayList<>();
        list.add(new ExchangeEntry(Items.GOLD_INGOT, 5L, Items.GOLD_INGOT.getDescriptionId(), false));
        list.add(new ExchangeEntry(Items.EMERALD, 10L, Items.EMERALD.getDescriptionId(), false));
        list.add(new ExchangeEntry(Items.DIAMOND, 50L, Items.DIAMOND.getDescriptionId(), false));
        list.add(new ExchangeEntry(Items.NETHERITE_INGOT, 500L, Items.NETHERITE_INGOT.getDescriptionId(), false));
        list.add(new ExchangeEntry(Items.NETHER_STAR, 2000L, Items.NETHER_STAR.getDescriptionId(), false));

        // Optional Modded Metals, Gems & Essences (safely detected from Forge registry!)
        tryAddModItem(list, "create", "brass_ingot", 15L);
        tryAddModItem(list, "mekanism", "ingot_osmium", 15L);
        tryAddModItem(list, "ars_nouveau", "source_gem", 20L);
        tryAddModItem(list, "pneumaticcraft", "ingot_iron_compressed", 25L);
        tryAddModItem(list, "thermal", "signalum_ingot", 60L);
        tryAddModItem(list, "thermal", "lumium_ingot", 60L);
        tryAddModItem(list, "enderio", "vibrant_alloy_ingot", 80L);
        tryAddModItem(list, "mekanism", "ingot_refined_obsidian", 100L);
        tryAddModItem(list, "botania", "elementium_ingot", 120L);
        tryAddModItem(list, "occultism", "iesnium_ingot", 150L);
        tryAddModItem(list, "thermal", "enderium_ingot", 180L);
        tryAddModItem(list, "mekanism", "alloy_atomic", 250L);
        tryAddModItem(list, "industrialforegoing", "pink_slime_ingot", 350L);
        tryAddModItem(list, "draconicevolution", "draconium_ingot", 400L);
        tryAddModItem(list, "powah", "crystal_spirited", 600L);
        tryAddModItem(list, "botania", "terrasteel_ingot", 900L);
        tryAddModItem(list, "mysticalagriculture", "supremium_essence", 1_500L);
        tryAddModItem(list, "apotheosis", "mythic_material", 2_000L);
        tryAddModItem(list, "powah", "crystal_nitro", 2_500L);
        tryAddModItem(list, "botania", "gaia_ingot", 3_500L);
        tryAddModItem(list, "draconicevolution", "awakened_draconium_ingot", 5_000L);
        tryAddModItem(list, "mysticalagradditions", "insanium_essence", 6_000L);
        tryAddModItem(list, "allthemodium", "allthemodium_ingot", 10_000L);
        tryAddModItem(list, "allthemodium", "vibranium_ingot", 50_000L);
        tryAddModItem(list, "allthemodium", "unobtainium_ingot", 250_000L);
        tryAddModItem(list, "allthetweaks", "atm_star_shard", 500_000L);
        tryAddModItem(list, "allthetweaks", "atm_star", 5_000_000L);

        cachedEntries = list;
        return cachedEntries;
    }

    private static void tryAddModItem(List<ExchangeEntry> list, String modid, String path, long chipValue) {
        ResourceLocation id = new ResourceLocation(modid, path);
        if (ForgeRegistries.ITEMS.containsKey(id)) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null && item != Items.AIR) {
                list.add(new ExchangeEntry(item, chipValue, item.getDescriptionId(), true));
            }
        }
    }

    public static String formatChips(long amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount).replace(',', ' ');
    }

    public static ItemStack findVipCard(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof VipCardItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static long getLooseChips(Player player) {
        long total = 0L;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ChipItem chip) {
                total += chip.getValue() * stack.getCount();
            }
        }
        return total;
    }

    public static long getTotalChips(Player player) {
        long total = getLooseChips(player);
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof VipCardItem) {
                total += VipCardItem.getBalance(stack);
            }
        }
        return total;
    }

    public static int countItem(Player player, Item item) {
        int total = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static boolean removeItem(ServerPlayer player, Item item, int count) {
        if (count <= 0 || countItem(player, item) < count) {
            return false;
        }
        int remaining = count;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
        inv.setChanged();
        return true;
    }

    public static void giveItem(ServerPlayer player, Item item, int count) {
        int remaining = count;
        int maxStack = item.getMaxStackSize();
        while (remaining > 0) {
            int toGive = Math.min(remaining, maxStack);
            ItemStack stack = new ItemStack(item, toGive);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            remaining -= toGive;
        }
        player.getInventory().setChanged();
    }

    public static boolean deductChips(ServerPlayer player, long amount) {
        if (amount <= 0) return true;
        long total = getTotalChips(player);
        if (total < amount) {
            return false;
        }

        long toDeduct = amount;
        // 1. Deduct from VIP Card(s) first for zero inventory clutter
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && toDeduct > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof VipCardItem) {
                long cardBal = VipCardItem.getBalance(stack);
                long take = Math.min(cardBal, toDeduct);
                if (take > 0) {
                    VipCardItem.setBalance(stack, cardBal - take);
                    toDeduct -= take;
                }
            }
        }

        // 2. Deduct any remaining from loose physical chips
        if (toDeduct > 0) {
            long loose = getLooseChips(player);
            clearLooseChips(player);
            long remainingLoose = loose - toDeduct;
            if (remainingLoose > 0) {
                givePhysicalChips(player, remainingLoose);
            }
        }
        inv.setChanged();
        return true;
    }

    public static void clearLooseChips(ServerPlayer player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ChipItem) {
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
        inv.setChanged();
    }

    public static void giveChips(ServerPlayer player, long amount) {
        if (amount <= 0) return;

        // If the player holds a VIP Card, credit it automatically!
        ItemStack vipCard = findVipCard(player);
        if (!vipCard.isEmpty()) {
            VipCardItem.addBalance(vipCard, amount);
            player.getInventory().setChanged();
            return;
        }

        givePhysicalChips(player, amount);
    }

    public static void givePhysicalChips(ServerPlayer player, long amount) {
        if (amount <= 0) return;

        ChipItem[] denoms = new ChipItem[] {
                ModItems.NETHERITE_CHIP.get(),
                ModItems.EMERALD_CHIP.get(),
                ModItems.GOLD_CHIP.get(),
                ModItems.PURPLE_CHIP.get(),
                ModItems.BLUE_CHIP.get(),
                ModItems.RED_CHIP.get(),
                ModItems.WHITE_CHIP.get()
        };

        long remaining = amount;
        for (ChipItem chip : denoms) {
            long val = chip.getValue();
            if (remaining >= val) {
                long count = remaining / val;
                remaining %= val;
                giveItem(player, chip, (int) Math.min(count, Integer.MAX_VALUE));
            }
        }
    }

    public static void consolidateChips(ServerPlayer player) {
        ItemStack vipCard = findVipCard(player);
        long loose = getLooseChips(player);
        if (loose <= 0) return;
        clearLooseChips(player);
        if (!vipCard.isEmpty()) {
            VipCardItem.addBalance(vipCard, loose);
            player.getInventory().setChanged();
        } else {
            givePhysicalChips(player, loose);
        }
    }
}
