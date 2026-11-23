package com.skycasino.registry;

import com.skycasino.SkyCasinoMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SkyCasinoMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SKY_CASINO_TAB = CREATIVE_MODE_TABS.register("skycasino_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.GOLD_CHIP.get()))
                    .title(Component.translatable("creativetab.skycasino"))
                    .displayItems((params, output) -> {
                        // Portable & VIP
                        output.accept(ModItems.POCKET_CASINO.get());
                        output.accept(ModItems.VIP_CARD.get());
                        output.accept(ModItems.ANTI_TILT_COFFEE.get());
                        output.accept(ModItems.LOOT_CASE_CLASSIC.get());
                        output.accept(ModItems.LOOT_CASE_VIP.get());

                        // Chips
                        output.accept(ModItems.WHITE_CHIP.get());
                        output.accept(ModItems.RED_CHIP.get());
                        output.accept(ModItems.BLUE_CHIP.get());
                        output.accept(ModItems.PURPLE_CHIP.get());
                        output.accept(ModItems.GOLD_CHIP.get());
                        output.accept(ModItems.EMERALD_CHIP.get());
                        output.accept(ModItems.NETHERITE_CHIP.get());

                        // Machines & Tables
                        output.accept(ModBlocks.CASHIER_TERMINAL.get());
                        output.accept(ModBlocks.SLOT_MACHINE.get());
                        output.accept(ModBlocks.BLACKJACK_TABLE.get());
                        output.accept(ModBlocks.ROULETTE_TABLE.get());
                        output.accept(ModBlocks.FORTUNE_WHEEL.get());
                        output.accept(ModBlocks.CRASH_MACHINE.get());
                        output.accept(ModBlocks.MINES_MACHINE.get());
                        output.accept(ModBlocks.PRIZE_SHOP.get());

                        // Luxury Decor
                        output.accept(ModBlocks.CASINO_STOOL.get());
                        output.accept(ModBlocks.CASINO_CHANDELIER.get());
                        output.accept(ModBlocks.CASINO_COUNTER.get());
                        output.accept(ModBlocks.CASINO_SIGN_BLOCK.get());
                        output.accept(ModBlocks.CASINO_CARPET_RED.get());
                        output.accept(ModBlocks.CASINO_CARPET_GREEN.get());
                        output.accept(ModBlocks.GOLD_MARBLE.get());
                        output.accept(ModBlocks.BLACK_GOLD_MARBLE.get());
                        output.accept(ModBlocks.NEON_GOLD.get());
                        output.accept(ModBlocks.NEON_PINK.get());
                        output.accept(ModBlocks.NEON_CYAN.get());
                        output.accept(ModBlocks.NEON_EMERALD.get());
                    })
                    .build());
}
