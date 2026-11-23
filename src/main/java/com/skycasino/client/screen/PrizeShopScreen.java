package com.skycasino.client.screen;

import com.skycasino.client.ClientScreenOpener;
import com.skycasino.economy.ChipEconomy;
import com.skycasino.network.ModNetwork;
import com.skycasino.network.PrizeShopBuyPacket;
import com.skycasino.network.PrizeShopCatalog;
import com.skycasino.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PrizeShopScreen extends Screen {
    private static final int PANEL_W = 356;
    private static final int PANEL_H = 236;
    private static final int ITEMS_PER_PAGE = 8;

    private final BlockPos pos;
    private int left;
    private int top;
    private int page = 0;

    public PrizeShopScreen(BlockPos pos) {
        super(Component.translatable("screen.skycasino.prize_shop"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        List<PrizeShopCatalog.ShopItem> items = PrizeShopCatalog.getShopItems();
        int maxPage = Math.max(0, (items.size() - 1) / ITEMS_PER_PAGE);
        if (page > maxPage) page = maxPage;

        int startIdx = page * ITEMS_PER_PAGE;
        int endIdx = Math.min(items.size(), startIdx + ITEMS_PER_PAGE);

        int cardW = 162;
        int cardH = 34;

        for (int i = startIdx; i < endIdx; i++) {
            final int idx = i;
            int local = i - startIdx;
            int col = local % 2;
            int row = local / 2;
            int cx = left + 12 + col * (cardW + 8);
            int cy = top + 36 + row * (cardH + 5);

            addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.buy"),
                    b -> ModNetwork.sendToServer(new PrizeShopBuyPacket(idx)))
                    .bounds(cx + cardW - 44, cy + 7, 40, 20).build());
        }

        // Pagination buttons when ATM9 items add pages 2-3
        int caseStartX = left + 12;
        if (maxPage > 0) {
            Button prev = addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                if (page > 0) { page--; init(); }
            }).bounds(left + 12, top + PANEL_H - 30, 20, 20).build());
            prev.active = page > 0;

            Button next = addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                if (page < maxPage) { page++; init(); }
            }).bounds(left + 34, top + PANEL_H - 30, 20, 20).build());
            next.active = page < maxPage;

            caseStartX = left + 60;
        }

        int caseBtnW = maxPage > 0 ? 104 : 126;
        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.open_classic"),
                b -> ClientScreenOpener.openLootCase(false, pos))
                .bounds(caseStartX, top + PANEL_H - 30, caseBtnW, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.open_vip"),
                b -> ClientScreenOpener.openLootCase(true, pos))
                .bounds(caseStartX + caseBtnW + 4, top + PANEL_H - 30, caseBtnW, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.close"), b -> this.onClose())
                .bounds(left + PANEL_W - 72, top + PANEL_H - 30, 60, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        long balance = CasinoGuiHelper.getClientBalance();
        CasinoGuiHelper.drawLuxuryPanel(g, left, top, PANEL_W, PANEL_H, false);
        CasinoGuiHelper.drawHeader(g, this.font, left, top, PANEL_W, this.title, balance);

        List<PrizeShopCatalog.ShopItem> items = PrizeShopCatalog.getShopItems();
        int maxPage = Math.max(0, (items.size() - 1) / ITEMS_PER_PAGE);
        int startIdx = page * ITEMS_PER_PAGE;
        int endIdx = Math.min(items.size(), startIdx + ITEMS_PER_PAGE);

        int cardW = 162;
        int cardH = 34;

        for (int i = startIdx; i < endIdx; i++) {
            PrizeShopCatalog.ShopItem item = items.get(i);
            int local = i - startIdx;
            int col = local % 2;
            int row = local / 2;
            int cx = left + 12 + col * (cardW + 8);
            int cy = top + 36 + row * (cardH + 5);

            int rCol = PrizeShopCatalog.rarityColor(item.rarity());
            g.fill(cx, cy, cx + cardW, cy + cardH, 0xBB12141E);
            g.fill(cx, cy, cx + 3, cy + cardH, rCol);
            CasinoGuiHelper.drawBorder(g, cx, cy, cardW, cardH, 0x55FFD15C);

            g.renderItem(new ItemStack(item.item(), item.count()), cx + 7, cy + 9);

            String name = Component.translatable(item.item().getDescriptionId()).getString();
            if (name.length() > 15) name = name.substring(0, 14) + "…";
            g.drawString(this.font, name, cx + 27, cy + 6, rCol, false);
            g.drawString(this.font, ChipEconomy.formatChips(item.price()), cx + 27, cy + 19, 0xFFFFD15C, false);
        }

        // Page indicator + owned case counts
        if (maxPage > 0) {
            g.drawString(this.font, (page + 1) + "/" + (maxPage + 1), left + 22, top + PANEL_H - 42, 0xFFBBBBCC, false);
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            int cCount = ChipEconomy.countItem(mc.player, ModItems.LOOT_CASE_CLASSIC.get());
            int vCount = ChipEconomy.countItem(mc.player, ModItems.LOOT_CASE_VIP.get());
            g.drawString(this.font, "Classic: " + cCount + "  VIP: " + vCount, left + 130, top + PANEL_H - 42, 0xFFFFD15C, false);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
