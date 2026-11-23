package com.skycasino.registry;

import com.skycasino.SkyCasinoMod;
import com.skycasino.block.CasinoChandelierBlock;
import com.skycasino.block.CasinoMachineBlock;
import com.skycasino.block.CasinoStoolBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SkyCasinoMod.MOD_ID);

    // === Interactive Casino Machines & Tables ===
    public static final RegistryObject<Block> CASHIER_TERMINAL = registerBlock("cashier_terminal",
            () -> new CasinoMachineBlock(
                    CasinoMachineBlock.MachineType.CASHIER,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> 12)
                            .noOcclusion()
            ));

    public static final RegistryObject<Block> SLOT_MACHINE = registerBlock("slot_machine",
            () -> new CasinoMachineBlock(
                    CasinoMachineBlock.MachineType.SLOT_MACHINE,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_RED)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> 13)
                            .noOcclusion()
            ));

    public static final RegistryObject<Block> BLACKJACK_TABLE = registerBlock("blackjack_table",
            () -> new CasinoMachineBlock(
                    CasinoMachineBlock.MachineType.BLACKJACK,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GREEN)
                            .strength(2.5F, 5.0F)
                            .sound(SoundType.WOOD)
                            .lightLevel(state -> 4)
                            .noOcclusion()
            ));

    public static final RegistryObject<Block> ROULETTE_TABLE = registerBlock("roulette_table",
            () -> new CasinoMachineBlock(
                    CasinoMachineBlock.MachineType.ROULETTE,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GREEN)
                            .strength(2.5F, 5.0F)
                            .sound(SoundType.WOOD)
                            .lightLevel(state -> 6)
                            .noOcclusion()
            ));

    public static final RegistryObject<Block> FORTUNE_WHEEL = registerBlock("fortune_wheel",
            () -> new CasinoMachineBlock(
                    CasinoMachineBlock.MachineType.FORTUNE_WHEEL,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .strength(2.5F, 5.0F)
                            .sound(SoundType.WOOD)
                            .lightLevel(state -> 14)
                            .noOcclusion()
            ));

    public static final RegistryObject<Block> CRASH_MACHINE = registerBlock("crash_machine",
            () -> new CasinoMachineBlock(
                    CasinoMachineBlock.MachineType.CRASH,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_CYAN)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> 14)
                            .noOcclusion()
            ));

    public static final RegistryObject<Block> MINES_MACHINE = registerBlock("mines_machine",
            () -> new CasinoMachineBlock(
                    CasinoMachineBlock.MachineType.MINES,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLUE)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> 13)
                            .noOcclusion()
            ));

    public static final RegistryObject<Block> PRIZE_SHOP = registerBlock("prize_shop",
            () -> new CasinoMachineBlock(
                    CasinoMachineBlock.MachineType.PRIZE_SHOP,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.GLASS)
                            .lightLevel(state -> 15)
                            .noOcclusion()
            ));

    // === Luxury Casino Decor ===
    public static final RegistryObject<Block> CASINO_CARPET_RED = registerBlock("casino_carpet_red",
            () -> new CarpetBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(0.2F)
                    .sound(SoundType.WOOL)));

    public static final RegistryObject<Block> CASINO_CARPET_GREEN = registerBlock("casino_carpet_green",
            () -> new CarpetBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.2F)
                    .sound(SoundType.WOOL)));

    public static final RegistryObject<Block> GOLD_MARBLE = registerBlock("gold_marble",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> BLACK_GOLD_MARBLE = registerBlock("black_gold_marble",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.5F, 6.0F)
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> NEON_GOLD = registerBlock("neon_gold",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(1.5F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)));

    public static final RegistryObject<Block> NEON_PINK = registerBlock("neon_pink",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_MAGENTA)
                    .strength(1.5F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)));

    public static final RegistryObject<Block> NEON_CYAN = registerBlock("neon_cyan",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(1.5F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)));

    public static final RegistryObject<Block> NEON_EMERALD = registerBlock("neon_emerald",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.EMERALD)
                    .strength(1.5F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)));

    public static final RegistryObject<Block> CASINO_COUNTER = registerBlock("casino_counter",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_RED)
                    .strength(2.5F, 5.0F)
                    .sound(SoundType.WOOD)));

    public static final RegistryObject<Block> CASINO_SIGN_BLOCK = registerBlock("casino_sign_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(2.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)));

    public static final RegistryObject<Block> CASINO_CHANDELIER = registerBlock("casino_chandelier",
            () -> new CasinoChandelierBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(1.5F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)
                    .noOcclusion()));

    public static final RegistryObject<Block> CASINO_STOOL = registerBlock("casino_stool",
            () -> new CasinoStoolBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(1.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }
}
