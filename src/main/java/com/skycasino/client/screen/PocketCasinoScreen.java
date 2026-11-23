package com.skycasino.client.screen;

import com.skycasino.block.CasinoMachineBlock;
import com.skycasino.client.ClientScreenOpener;
import com.skycasino.economy.ChipEconomy;
import com.skycasino.registry.ModBlocks;
import com.skycasino.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PocketCasinoScreen extends Screen {
    private static final int PANEL_W = 340;
    private static final int PANEL_H = 228;

    private final BlockPos pos;
    private int left;
    private int top;

    public PocketCasinoScreen(BlockPos pos) {
        super(Component.translatable("screen.skycasino.pocket_hub"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        int col1 = left + 18;
        int col2 = left + 178;
        int bw = 144;
        int bh = 28;

        addRenderableWidget(Button.builder(Component.translatable("block.skycasino.slot_machine"),
                b -> ClientScreenOpener.openMachineScreen(CasinoMachineBlock.MachineType.SLOT_MACHINE, pos))
                .bounds(col1, top + 44, bw, bh).build());

        addRenderableWidget(Button.builder(Component.translatable("block.skycasino.blackjack_table"),
                b -> ClientScreenOpener.openMachineScreen(CasinoMachineBlock.MachineType.BLACKJACK, pos))
                .bounds(col2, top + 44, bw, bh).build());

        addRenderableWidget(Button.builder(Component.translatable("block.skycasino.roulette_table"),
                b -> ClientScreenOpener.openMachineScreen(CasinoMachineBlock.MachineType.ROULETTE, pos))
                .bounds(col1, top + 80, bw, bh).build());

        addRenderableWidget(Button.builder(Component.translatable("block.skycasino.fortune_wheel"),
                b -> ClientScreenOpener.openMachineScreen(CasinoMachineBlock.MachineType.FORTUNE_WHEEL, pos))
                .bounds(col2, top + 80, bw, bh).build());

        addRenderableWidget(Button.builder(Component.translatable("block.skycasino.crash_machine"),
                b -> ClientScreenOpener.openMachineScreen(CasinoMachineBlock.MachineType.CRASH, pos))
                .bounds(col1, top + 116, bw, bh).build());

        addRenderableWidget(Button.builder(Component.translatable("block.skycasino.mines_machine"),
                b -> ClientScreenOpener.openMachineScreen(CasinoMachineBlock.MachineType.MINES, pos))
                .bounds(col2, top + 116, bw, bh).build());

        // Cashier & Prize Shop buttons side by side
        addRenderableWidget(Button.builder(Component.translatable("block.skycasino.cashier_terminal"),
                b -> ClientScreenOpener.openMachineScreen(CasinoMachineBlock.MachineType.CASHIER, pos))
                .bounds(col1, top + 156, bw, 26).build());

        addRenderableWidget(Button.builder(Component.translatable("block.skycasino.prize_shop"),
                b -> ClientScreenOpener.openMachineScreen(CasinoMachineBlock.MachineType.PRIZE_SHOP, pos))
                .bounds(col2, top + 156, bw, 26).build());

        // Ludik Panel (Pawn Shop, Micro-Loans, Dodep) button in footer
        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.ludik.open"),
                b -> ClientScreenOpener.openLudikPanel())
                .bounds(left + 150, top + PANEL_H - 30, 100, 20).build());

        // Close button
        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.close"), b -> this.onClose())
                .bounds(left + PANEL_W - 86, top + PANEL_H - 30, 70, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        long balance = CasinoGuiHelper.getClientBalance();
        CasinoGuiHelper.drawLuxuryPanel(g, left, top, PANEL_W, PANEL_H, false);
        CasinoGuiHelper.drawHeader(g, this.font, left, top, PANEL_W, this.title, balance);

        super.render(g, mouseX, mouseY, partialTick);

        // Overlay crisp 3D block icons on the left of each hub button
        g.renderItem(new ItemStack(ModBlocks.SLOT_MACHINE.get()), left + 22, top + 50);
        g.renderItem(new ItemStack(ModBlocks.BLACKJACK_TABLE.get()), left + 182, top + 50);
        g.renderItem(new ItemStack(ModBlocks.ROULETTE_TABLE.get()), left + 22, top + 86);
        g.renderItem(new ItemStack(ModBlocks.FORTUNE_WHEEL.get()), left + 182, top + 86);
        g.renderItem(new ItemStack(ModBlocks.CRASH_MACHINE.get()), left + 22, top + 122);
        g.renderItem(new ItemStack(ModBlocks.MINES_MACHINE.get()), left + 182, top + 122);
        g.renderItem(new ItemStack(ModBlocks.CASHIER_TERMINAL.get()), left + 22, top + 161);
        g.renderItem(new ItemStack(ModBlocks.PRIZE_SHOP.get()), left + 182, top + 161);

        // VIP Card status in footer
        Minecraft mc = Minecraft.getInstance();
        boolean hasVip = mc.player != null && !ChipEconomy.findVipCard(mc.player).isEmpty();
        g.renderItem(new ItemStack(ModItems.VIP_CARD.get()), left + 18, top + PANEL_H - 28);
        g.drawString(this.font,
                hasVip ? Component.translatable("gui.skycasino.vip_active") : Component.translatable("gui.skycasino.vip_missing"),
                left + 38, top + PANEL_H - 24, hasVip ? 0xFF77EE99 : 0xFFAAAABB, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
