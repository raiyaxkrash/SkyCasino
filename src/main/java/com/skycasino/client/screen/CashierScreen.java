package com.skycasino.client.screen;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.network.CashierActionPacket;
import com.skycasino.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CashierScreen extends Screen {
    private static final int PANEL_W = 352;
    private static final int PANEL_H = 236;
    private static final int ROWS_PER_PAGE = 5;

    private final BlockPos pos;
    private int left;
    private int top;
    private int page = 0;

    public CashierScreen(BlockPos pos) {
        super(Component.translatable("screen.skycasino.cashier"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        List<ChipEconomy.ExchangeEntry> entries = ChipEconomy.getExchangeEntries();
        int maxPage = Math.max(0, (entries.size() - 1) / ROWS_PER_PAGE);
        if (page > maxPage) page = maxPage;

        int startIdx = page * ROWS_PER_PAGE;
        int endIdx = Math.min(entries.size(), startIdx + ROWS_PER_PAGE);

        int rowStartY = top + 46;
        int rowHeight = 28;

        for (int i = startIdx; i < endIdx; i++) {
            final int idx = i;
            int ry = rowStartY + (i - startIdx) * rowHeight;

            // Buy chips buttons (Resource -> Chips)
            addRenderableWidget(Button.builder(Component.literal("+1"),
                    b -> ModNetwork.sendToServer(new CashierActionPacket(CashierActionPacket.ACTION_BUY, idx, 1)))
                    .bounds(left + 162, ry + 2, 26, 20).build());

            addRenderableWidget(Button.builder(Component.literal("+10"),
                    b -> ModNetwork.sendToServer(new CashierActionPacket(CashierActionPacket.ACTION_BUY, idx, 10)))
                    .bounds(left + 190, ry + 2, 28, 20).build());

            addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.all"),
                    b -> ModNetwork.sendToServer(new CashierActionPacket(CashierActionPacket.ACTION_BUY, idx, -1)))
                    .bounds(left + 220, ry + 2, 32, 20).build());

            // Cash out buttons (Chips -> Resource)
            addRenderableWidget(Button.builder(Component.literal("-1"),
                    b -> ModNetwork.sendToServer(new CashierActionPacket(CashierActionPacket.ACTION_SELL, idx, 1)))
                    .bounds(left + 260, ry + 2, 26, 20).build());

            addRenderableWidget(Button.builder(Component.literal("-10"),
                    b -> ModNetwork.sendToServer(new CashierActionPacket(CashierActionPacket.ACTION_SELL, idx, 10)))
                    .bounds(left + 288, ry + 2, 28, 20).build());

            addRenderableWidget(Button.builder(Component.literal("Max"),
                    b -> ModNetwork.sendToServer(new CashierActionPacket(CashierActionPacket.ACTION_SELL, idx, -1)))
                    .bounds(left + 318, ry + 2, 24, 20).build());
        }

        // Page buttons if ATM9 metals add a 2nd page
        if (maxPage > 0) {
            Button prev = addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                if (page > 0) { page--; init(); }
            }).bounds(left + 12, top + PANEL_H - 30, 22, 20).build());
            prev.active = page > 0;

            Button next = addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                if (page < maxPage) { page++; init(); }
            }).bounds(left + 38, top + PANEL_H - 30, 22, 20).build());
            next.active = page < maxPage;
        }

        // Consolidate / Deposit to VIP Card button
        int consX = maxPage > 0 ? left + 66 : left + 12;
        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.consolidate"),
                b -> ModNetwork.sendToServer(new CashierActionPacket(CashierActionPacket.ACTION_CONSOLIDATE, 0, 0)))
                .bounds(consX, top + PANEL_H - 30, 114, 20).build());

        // Ludik Panel (Loans / Pawn / Dodep)
        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.ludik.open"),
                b -> com.skycasino.client.ClientScreenOpener.openLudikPanel())
                .bounds(consX + 118, top + PANEL_H - 30, 82, 20).build());

        // Close button
        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.close"),
                b -> this.onClose())
                .bounds(left + PANEL_W - 72, top + PANEL_H - 30, 60, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        long balance = CasinoGuiHelper.getClientBalance();
        CasinoGuiHelper.drawLuxuryPanel(g, left, top, PANEL_W, PANEL_H, false);
        CasinoGuiHelper.drawHeader(g, this.font, left, top, PANEL_W, this.title, balance);

        // Column headers
        g.drawString(this.font, Component.translatable("gui.skycasino.resource"), left + 14, top + 33, 0xFFBBBBCC, false);
        g.drawString(this.font, Component.translatable("gui.skycasino.buy_chips"), left + 162, top + 33, 0xFF77EE88, false);
        g.drawString(this.font, Component.translatable("gui.skycasino.cash_out"), left + 260, top + 33, 0xFFFFAA55, false);

        List<ChipEconomy.ExchangeEntry> entries = ChipEconomy.getExchangeEntries();
        int startIdx = page * ROWS_PER_PAGE;
        int endIdx = Math.min(entries.size(), startIdx + ROWS_PER_PAGE);

        int rowStartY = top + 46;
        int rowHeight = 28;
        Minecraft mc = Minecraft.getInstance();

        for (int i = startIdx; i < endIdx; i++) {
            ChipEconomy.ExchangeEntry entry = entries.get(i);
            int ry = rowStartY + (i - startIdx) * rowHeight;

            int rowBg = entry.isAtm() ? 0x662B1A38 : ((i % 2 == 0) ? 0x66191D28 : 0x4410131C);
            g.fill(left + 10, ry, left + PANEL_W - 10, ry + 24, rowBg);
            g.renderItem(new ItemStack(entry.item()), left + 14, ry + 4);

            int owned = mc.player != null ? ChipEconomy.countItem(mc.player, entry.item()) : 0;
            g.drawString(this.font, Component.translatable(entry.displayKey()), left + 35, ry + 3, entry.isAtm() ? 0xFFEEBBFF : 0xFFFFFFFF, false);
            g.drawString(this.font, "= " + ChipEconomy.formatChips(entry.chipValue()) + " (" + owned + ")", left + 35, ry + 13, 0xFFFFD15C, false);
        }

        // VIP card status note
        if (mc.player != null && !ChipEconomy.findVipCard(mc.player).isEmpty()) {
            g.drawString(this.font, Component.translatable("gui.skycasino.vip_active"), left + 14, top + PANEL_H - 44, 0xFF77EE99, false);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
