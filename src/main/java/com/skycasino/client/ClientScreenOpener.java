package com.skycasino.client;

import com.skycasino.block.CasinoMachineBlock;
import com.skycasino.client.screen.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class ClientScreenOpener {
    public static void openMachineScreen(CasinoMachineBlock.MachineType type, BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        switch (type) {
            case CASHIER -> mc.setScreen(new CashierScreen(pos));
            case SLOT_MACHINE -> mc.setScreen(new SlotMachineScreen(pos));
            case BLACKJACK -> mc.setScreen(new BlackjackScreen(pos));
            case ROULETTE -> mc.setScreen(new RouletteScreen(pos));
            case FORTUNE_WHEEL -> mc.setScreen(new FortuneWheelScreen(pos));
            case CRASH -> mc.setScreen(new CrashScreen(pos));
            case MINES -> mc.setScreen(new MinesScreen(pos));
            case PRIZE_SHOP -> mc.setScreen(new PrizeShopScreen(pos));
        }
    }

    public static void openPocketHub(BlockPos pos) {
        Minecraft.getInstance().setScreen(new PocketCasinoScreen(pos));
    }

    public static void openLootCase(boolean vip, BlockPos pos) {
        Minecraft.getInstance().setScreen(new LootCaseScreen(vip, pos));
    }

    public static void openLudikPanel() {
        Minecraft.getInstance().setScreen(new LudikPanelScreen());
    }
}
