package com.skycasino.network;

import com.skycasino.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PrizeShopCatalog {
    // Rarity tiers: 0=Common(Blue), 1=Rare(Cyan), 2=Epic(Purple), 3=Legendary(Red/Pink), 4=Mythic(Gold)
    public record ShopItem(Item item, int count, long price, int rarity) {}
    public record CaseReward(Item item, int count, long bonusChips, int weight, int rarity) {}

    private static List<ShopItem> shopItems;
    private static List<CaseReward> classicRewards;
    private static List<CaseReward> vipRewards;

    public static List<ShopItem> getShopItems() {
        if (shopItems != null) return shopItems;
        List<ShopItem> list = new ArrayList<>();

        // Page 1: Cases & Vanilla Classics
        list.add(new ShopItem(ModItems.LOOT_CASE_CLASSIC.get(), 1, 500L, 1));
        list.add(new ShopItem(ModItems.LOOT_CASE_VIP.get(), 1, 5_000L, 4));
        list.add(new ShopItem(Items.TOTEM_OF_UNDYING, 1, 500L, 2));
        list.add(new ShopItem(Items.ENCHANTED_GOLDEN_APPLE, 1, 1_000L, 2));
        list.add(new ShopItem(Items.SHULKER_BOX, 1, 1_500L, 2));
        list.add(new ShopItem(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1, 2_500L, 3));
        list.add(new ShopItem(Items.BEACON, 1, 4_000L, 3));
        list.add(new ShopItem(Items.ELYTRA, 1, 6_000L, 4));

        // Utility & Skyblock Essentials (Ex Deorum, Waystones, TIAB, Building Gadgets, Pipez, LaserIO)
        tryAddShop(list, "waystones", "waystone", 1, 800L, 1);
        tryAddShop(list, "waystones", "warp_stone", 1, 1_200L, 1);
        tryAddShop(list, "exdeorum", "diamond_mesh", 1, 1_500L, 1);
        tryAddShop(list, "pipez", "ultimate_upgrade", 4, 2_000L, 2);
        tryAddShop(list, "laserio", "overclocker_node", 4, 2_200L, 2);
        tryAddShop(list, "buildinggadgets2", "gadget_building", 1, 2_500L, 2);
        tryAddShop(list, "buildinggadgets2", "gadget_copy_paste", 1, 4_500L, 2);
        tryAddShop(list, "tiab", "time_in_a_bottle", 1, 4_000L, 2);
        tryAddShop(list, "exdeorum", "netherite_mesh", 1, 6_000L, 3);
        tryAddShop(list, "pipez", "infinity_upgrade", 1, 18_000L, 3);

        // Create
        tryAddShop(list, "create", "precision_mechanism", 4, 1_200L, 1);
        tryAddShop(list, "create", "blaze_cake", 8, 1_500L, 1);
        tryAddShop(list, "create", "mechanical_arm", 1, 2_500L, 2);
        tryAddShop(list, "create", "extendo_grip", 1, 3_500L, 2);
        tryAddShop(list, "create", "potato_cannon", 1, 3_000L, 2);

        // Tech: Thermal, Ender IO, Flux Networks, Powah, Industrial Foregoing, Hostile Neural Networks
        tryAddShop(list, "enderio", "octadic_capacitor", 2, 1_800L, 1);
        tryAddShop(list, "enderio", "staff_of_travelling", 1, 4_500L, 2);
        tryAddShop(list, "fluxnetworks", "flux_controller", 1, 3_500L, 2);
        tryAddShop(list, "thermal", "upgrade_augment_3", 1, 5_000L, 2);
        tryAddShop(list, "hostilenetworks", "sim_chamber", 1, 4_000L, 2);
        tryAddShop(list, "powah", "battery_nitro", 1, 14_000L, 3);
        tryAddShop(list, "powah", "thermo_generator_nitro", 1, 16_000L, 3);
        tryAddShop(list, "industrialforegoing", "infinity_drill", 1, 22_000L, 3);
        tryAddShop(list, "industrialforegoing", "infinity_backpack", 1, 25_000L, 3);

        // Storage & Backpacks (AE2, Refined Storage, Sophisticated Backpacks, Occultism)
        tryAddShop(list, "occultism", "storage_remote", 1, 3_000L, 2);
        tryAddShop(list, "ae2", "item_storage_cell_64k", 1, 5_000L, 2);
        tryAddShop(list, "refinedstorage", "wireless_grid", 1, 5_500L, 2);
        tryAddShop(list, "refinedstorage", "64k_storage_disk", 1, 6_000L, 2);
        tryAddShop(list, "sophisticatedbackpacks", "netherite_backpack", 1, 8_000L, 2);
        tryAddShop(list, "ae2", "item_storage_cell_256k", 1, 18_000L, 3);

        // Magic: Botania, Ars Nouveau, Apotheosis, Mystical Agriculture, Productive Bees
        tryAddShop(list, "ars_nouveau", "apprentice_spell_book", 1, 2_500L, 1);
        tryAddShop(list, "ars_nouveau", "ring_of_greater_discount", 1, 5_000L, 2);
        tryAddShop(list, "ars_nouveau", "archmage_spell_book", 1, 12_000L, 3);
        tryAddShop(list, "botania", "blacker_lotus", 2, 3_500L, 2);
        tryAddShop(list, "botania", "magnet_ring_greater", 1, 4_000L, 2);
        tryAddShop(list, "botania", "flight_tiara", 1, 14_000L, 3);
        tryAddShop(list, "apotheosis", "mythic_material", 2, 5_500L, 3);
        tryAddShop(list, "mysticalagriculture", "master_infusion_crystal", 1, 12_000L, 3);
        tryAddShop(list, "productivebees", "upgrade_productivity_4", 1, 16_000L, 3);

        // Endgame: Mekanism, Draconic Evolution, Allthemodium, ATM Star
        tryAddShop(list, "mekanism", "ultimate_control_circuit", 4, 6_000L, 2);
        tryAddShop(list, "mekanism", "jetpack_armored", 1, 12_000L, 2);
        tryAddShop(list, "mekanism", "atomic_disassembler", 1, 15_000L, 3);
        tryAddShop(list, "mekanism", "mekasuit_bodyarmor", 1, 40_000L, 4);
        tryAddShop(list, "mekanism", "pellet_antimatter", 1, 75_000L, 4);
        tryAddShop(list, "mekanism", "qio_drive_supermassive", 1, 90_000L, 4);
        tryAddShop(list, "draconicevolution", "chaotic_core", 1, 120_000L, 4);
        tryAddShop(list, "allthemodium", "allthemodium_apple", 1, 20_000L, 3);
        tryAddShop(list, "allthemodium", "allthemodium_pickaxe", 1, 45_000L, 3);
        tryAddShop(list, "allthemodium", "allthemodium_sword", 1, 45_000L, 3);
        tryAddShop(list, "allthetweaks", "atm_star_shard", 1, 600_000L, 4);
        tryAddShop(list, "allthetweaks", "atm_star", 1, 6_000_000L, 4);

        shopItems = list;
        return shopItems;
    }

    private static void tryAddShop(List<ShopItem> list, String modid, String path, int count, long price, int rarity) {
        Item item = getOptionalItem(modid, path);
        if (item != null) {
            list.add(new ShopItem(item, count, price, rarity));
        }
    }

    private static void tryAddReward(List<CaseReward> list, String modid, String path, int count, int weight, int rarity) {
        Item item = getOptionalItem(modid, path);
        if (item != null) {
            list.add(new CaseReward(item, count, 0, weight, rarity));
        }
    }

    public static List<CaseReward> getCaseRewards(boolean vip) {
        if (vip) {
            if (vipRewards != null) return vipRewards;
            List<CaseReward> list = new ArrayList<>();
            // Base rewards
            list.add(new CaseReward(Items.DIAMOND_BLOCK, 4, 0, 18, 0));
            list.add(new CaseReward(Items.NETHERITE_INGOT, 4, 0, 14, 1));
            list.add(new CaseReward(Items.ENCHANTED_GOLDEN_APPLE, 3, 0, 10, 2));
            list.add(new CaseReward(Items.NETHER_STAR, 2, 0, 9, 3));
            list.add(new CaseReward(Items.ELYTRA, 1, 0, 5, 3));
            list.add(new CaseReward(ModItems.EMERALD_CHIP.get(), 0, 25_000L, 4, 4));

            // Modded VIP Case Drops (Create, Botania, Ars, Thermal, EnderIO, Powah, Mekanism, AE2, RS, Apotheosis, IF, Draconic, ATM9)
            tryAddReward(list, "mekanism", "alloy_atomic", 8, 14, 1);
            tryAddReward(list, "powah", "crystal_nitro", 4, 13, 1);
            tryAddReward(list, "mysticalagriculture", "supremium_essence", 8, 13, 1);
            tryAddReward(list, "botania", "terrasteel_ingot", 4, 12, 1);
            tryAddReward(list, "industrialforegoing", "pink_slime_ingot", 4, 12, 1);
            tryAddReward(list, "thermal", "upgrade_augment_3", 1, 10, 2);
            tryAddReward(list, "create", "mechanical_arm", 2, 10, 2);
            tryAddReward(list, "create", "extendo_grip", 1, 9, 2);
            tryAddReward(list, "enderio", "staff_of_travelling", 1, 9, 2);
            tryAddReward(list, "exdeorum", "netherite_mesh", 1, 9, 2);
            tryAddReward(list, "tiab", "time_in_a_bottle", 1, 9, 2);
            tryAddReward(list, "botania", "blacker_lotus", 2, 9, 2);
            tryAddReward(list, "apotheosis", "mythic_material", 3, 8, 2);
            tryAddReward(list, "sophisticatedbackpacks", "netherite_backpack", 1, 8, 2);
            tryAddReward(list, "mysticalagradditions", "insanium_essence", 4, 8, 2);
            tryAddReward(list, "ae2", "item_storage_cell_64k", 1, 7, 2);
            tryAddReward(list, "refinedstorage", "64k_storage_disk", 1, 7, 2);
            tryAddReward(list, "buildinggadgets2", "gadget_copy_paste", 1, 7, 2);
            tryAddReward(list, "ars_nouveau", "archmage_spell_book", 1, 6, 3);
            tryAddReward(list, "botania", "flight_tiara", 1, 6, 3);
            tryAddReward(list, "botania", "gaia_ingot", 3, 6, 3);
            tryAddReward(list, "mekanism", "atomic_disassembler", 1, 6, 3);
            tryAddReward(list, "powah", "thermo_generator_nitro", 1, 5, 3);
            tryAddReward(list, "productivebees", "upgrade_productivity_4", 1, 5, 3);
            tryAddReward(list, "pipez", "infinity_upgrade", 1, 5, 3);
            tryAddReward(list, "industrialforegoing", "infinity_drill", 1, 4, 3);
            tryAddReward(list, "allthemodium", "allthemodium_ingot", 2, 6, 3);
            tryAddReward(list, "allthemodium", "allthemodium_pickaxe", 1, 4, 3);
            tryAddReward(list, "draconicevolution", "awakened_draconium_ingot", 4, 4, 3);
            tryAddReward(list, "mekanism", "mekasuit_bodyarmor", 1, 3, 4);
            tryAddReward(list, "mekanism", "pellet_antimatter", 1, 3, 4);
            tryAddReward(list, "mekanism", "qio_drive_supermassive", 1, 2, 4);
            tryAddReward(list, "draconicevolution", "chaotic_core", 1, 2, 4);
            tryAddReward(list, "allthemodium", "vibranium_ingot", 2, 3, 4);
            tryAddReward(list, "allthemodium", "unobtainium_ingot", 1, 2, 4);
            tryAddReward(list, "allthetweaks", "atm_star_shard", 1, 1, 4);
            tryAddReward(list, "allthetweaks", "atm_star", 1, 1, 4);

            vipRewards = list;
            return vipRewards;
        } else {
            if (classicRewards != null) return classicRewards;
            List<CaseReward> list = new ArrayList<>();
            list.add(new CaseReward(Items.GOLD_INGOT, 16, 0, 22, 0));
            list.add(new CaseReward(Items.EXPERIENCE_BOTTLE, 32, 0, 18, 0));
            list.add(new CaseReward(Items.EMERALD, 16, 0, 16, 1));
            list.add(new CaseReward(Items.DIAMOND, 8, 0, 14, 1));
            list.add(new CaseReward(Items.TOTEM_OF_UNDYING, 1, 0, 8, 2));
            list.add(new CaseReward(Items.NETHERITE_INGOT, 1, 0, 6, 2));
            list.add(new CaseReward(Items.BEACON, 1, 0, 2, 3));
            list.add(new CaseReward(ModItems.GOLD_CHIP.get(), 0, 5000L, 2, 4));

            // Modded Classic Case Drops
            tryAddReward(list, "create", "brass_ingot", 16, 16, 0);
            tryAddReward(list, "hostilenetworks", "prediction_matrix", 32, 16, 0);
            tryAddReward(list, "apotheosis", "gem_dust", 8, 14, 0);
            tryAddReward(list, "create", "precision_mechanism", 4, 12, 1);
            tryAddReward(list, "create", "blaze_cake", 6, 12, 1);
            tryAddReward(list, "fluxnetworks", "flux_core", 16, 12, 1);
            tryAddReward(list, "enderio", "vibrant_alloy_ingot", 8, 12, 1);
            tryAddReward(list, "enderio", "octadic_capacitor", 2, 10, 1);
            tryAddReward(list, "thermal", "enderium_ingot", 8, 10, 1);
            tryAddReward(list, "botania", "elementium_ingot", 8, 10, 1);
            tryAddReward(list, "exdeorum", "diamond_mesh", 1, 10, 1);
            tryAddReward(list, "waystones", "waystone", 1, 9, 1);
            tryAddReward(list, "pipez", "ultimate_upgrade", 2, 9, 1);
            tryAddReward(list, "laserio", "overclocker_node", 2, 9, 1);
            tryAddReward(list, "mekanism", "ingot_refined_obsidian", 8, 9, 2);
            tryAddReward(list, "mekanism", "ultimate_control_circuit", 2, 7, 2);
            tryAddReward(list, "occultism", "iesnium_ingot", 6, 8, 2);
            tryAddReward(list, "ars_nouveau", "apprentice_spell_book", 1, 6, 2);
            tryAddReward(list, "buildinggadgets2", "gadget_building", 1, 6, 2);
            tryAddReward(list, "tiab", "time_in_a_bottle", 1, 4, 3);
            tryAddReward(list, "allthemodium", "allthemodium_ingot", 1, 2, 4);

            classicRewards = list;
            return classicRewards;
        }
    }

    private static Item getOptionalItem(String modid, String path) {
        ResourceLocation id = new ResourceLocation(modid, path);
        if (ForgeRegistries.ITEMS.containsKey(id)) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null && item != Items.AIR) return item;
        }
        return null;
    }

    public static int rollRewardIndex(boolean vip, Random random) {
        List<CaseReward> rewards = getCaseRewards(vip);
        int totalWeight = 0;
        for (CaseReward r : rewards) totalWeight += r.weight();
        int roll = random.nextInt(totalWeight);
        int sum = 0;
        for (int i = 0; i < rewards.size(); i++) {
            sum += rewards.get(i).weight();
            if (roll < sum) return i;
        }
        return 0;
    }

    public static int rarityColor(int rarity) {
        return switch (rarity) {
            case 1 -> 0xFF38BDF8; // Rare Cyan
            case 2 -> 0xFFC084FC; // Epic Purple
            case 3 -> 0xFFFB7185; // Legendary Rose-Red
            case 4 -> 0xFFFFD700; // Mythic Gold
            default -> 0xFF60A5FA; // Common Blue
        };
    }

    public static String rarityKey(int rarity) {
        return switch (rarity) {
            case 1 -> "rarity.skycasino.rare";
            case 2 -> "rarity.skycasino.epic";
            case 3 -> "rarity.skycasino.legendary";
            case 4 -> "rarity.skycasino.mythic";
            default -> "rarity.skycasino.common";
        };
    }
}
