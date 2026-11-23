package com.skycasino.registry;

import com.skycasino.SkyCasinoMod;
import com.skycasino.item.AntiTiltCoffeeItem;
import com.skycasino.item.ChipItem;
import com.skycasino.item.LootCaseItem;
import com.skycasino.item.PocketCasinoItem;
import com.skycasino.item.VipCardItem;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SkyCasinoMod.MOD_ID);

    // Portable Casino & VIP Card
    public static final RegistryObject<PocketCasinoItem> POCKET_CASINO = ITEMS.register("pocket_casino",
            () -> new PocketCasinoItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<VipCardItem> VIP_CARD = ITEMS.register("vip_card",
            () -> new VipCardItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<AntiTiltCoffeeItem> ANTI_TILT_COFFEE = ITEMS.register("anti_tilt_coffee",
            () -> new AntiTiltCoffeeItem(new Item.Properties().stacksTo(16)));

    // Loot Cases
    public static final RegistryObject<LootCaseItem> LOOT_CASE_CLASSIC = ITEMS.register("loot_case_classic",
            () -> new LootCaseItem(false, new Item.Properties().stacksTo(16)));

    public static final RegistryObject<LootCaseItem> LOOT_CASE_VIP = ITEMS.register("loot_case_vip",
            () -> new LootCaseItem(true, new Item.Properties().stacksTo(16)));

    // Casino Chips (1 .. 100,000)
    public static final RegistryObject<ChipItem> WHITE_CHIP = ITEMS.register("white_chip",
            () -> new ChipItem(1L, ChatFormatting.WHITE, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ChipItem> RED_CHIP = ITEMS.register("red_chip",
            () -> new ChipItem(5L, ChatFormatting.RED, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ChipItem> BLUE_CHIP = ITEMS.register("blue_chip",
            () -> new ChipItem(25L, ChatFormatting.AQUA, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ChipItem> PURPLE_CHIP = ITEMS.register("purple_chip",
            () -> new ChipItem(100L, ChatFormatting.LIGHT_PURPLE, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ChipItem> GOLD_CHIP = ITEMS.register("gold_chip",
            () -> new ChipItem(1000L, ChatFormatting.GOLD, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ChipItem> EMERALD_CHIP = ITEMS.register("emerald_chip",
            () -> new ChipItem(10_000L, ChatFormatting.GREEN, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<ChipItem> NETHERITE_CHIP = ITEMS.register("netherite_chip",
            () -> new ChipItem(100_000L, ChatFormatting.DARK_PURPLE, new Item.Properties().stacksTo(64)));
}
