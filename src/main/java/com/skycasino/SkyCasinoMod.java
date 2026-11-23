package com.skycasino;

import com.skycasino.network.ModNetwork;
import com.skycasino.registry.ModBlocks;
import com.skycasino.registry.ModCreativeTabs;
import com.skycasino.registry.ModEntities;
import com.skycasino.registry.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SkyCasinoMod.MOD_ID)
public class SkyCasinoMod {
    public static final String MOD_ID = "skycasino";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public SkyCasinoMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
        LOGGER.info("Sky Casino mod initialized!");
    }
}
