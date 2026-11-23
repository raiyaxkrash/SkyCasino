package com.skycasino.client.screen;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.network.LootCaseOpenRequestPacket;
import com.skycasino.network.LootCaseOpenResultPacket;
import com.skycasino.network.ModNetwork;
import com.skycasino.network.PrizeShopCatalog;
import com.skycasino.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootCaseScreen extends Screen {
    private static final int PANEL_W = 352;
    private static final int PANEL_H = 220;
    private static final int STRIP_LEN = 46;
    private static final int WIN_SLOT = 38;

    private final boolean vip;
    private final BlockPos pos;
    private int left;
    private int top;

    private final List<PrizeShopCatalog.CaseReward> strip = new ArrayList<>();
    private boolean rolling = false;
    private boolean finished = false;
    private double scrollPos = 0.0;
    private int lastPassedSlot = 0;
    private PrizeShopCatalog.CaseReward wonReward = null;

    private Button openBtn;

    public LootCaseScreen(boolean vip, BlockPos pos) {
        super(Component.translatable(vip ? "screen.skycasino.case_vip" : "screen.skycasino.case_classic"));
        this.vip = vip;
        this.pos = pos;
        populatePreviewStrip();
    }

    private void populatePreviewStrip() {
        strip.clear();
        Random r = new Random();
        List<PrizeShopCatalog.CaseReward> pool = PrizeShopCatalog.getCaseRewards(vip);
        for (int i = 0; i < STRIP_LEN; i++) {
            strip.add(pool.get(PrizeShopCatalog.rollRewardIndex(vip, r)));
        }
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        this.openBtn = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.case.open"), b -> requestOpen())
                .bounds(left + 16, top + PANEL_H - 32, 236, 22).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.close"), b -> this.onClose())
                .bounds(left + PANEL_W - 88, top + PANEL_H - 32, 72, 22).build());

        updateButton();
    }

    private int ownedCases() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 0;
        Item item = vip ? ModItems.LOOT_CASE_VIP.get() : ModItems.LOOT_CASE_CLASSIC.get();
        return ChipEconomy.countItem(mc.player, item);
    }

    private void updateButton() {
        if (openBtn != null) {
            openBtn.active = !rolling && ownedCases() > 0;
        }
    }

    private void requestOpen() {
        if (rolling || ownedCases() <= 0) return;
        rolling = true;
        finished = false;
        wonReward = null;
        openBtn.active = false;
        ModNetwork.sendToServer(new LootCaseOpenRequestPacket(vip, pos));
    }

    public void onCaseResult(LootCaseOpenResultPacket packet) {
        List<PrizeShopCatalog.CaseReward> pool = PrizeShopCatalog.getCaseRewards(vip);
        if (packet.winIndex < 0 || packet.winIndex >= pool.size()) {
            rolling = false;
            updateButton();
            return;
        }
        populatePreviewStrip();
        this.wonReward = pool.get(packet.winIndex);
        strip.set(WIN_SLOT, this.wonReward);
        this.scrollPos = 2.0;
        this.lastPassedSlot = 2;
        this.rolling = true;
        this.finished = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (rolling && wonReward != null) {
            double rem = WIN_SLOT - scrollPos;
            if (rem > 0.03) {
                double step = Math.max(0.045, rem * 0.105);
                scrollPos += step;
                int slot = (int) Math.floor(scrollPos);
                if (slot != lastPassedSlot) {
                    lastPassedSlot = slot;
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.55F, 1.35F);
                    }
                }
            } else {
                scrollPos = WIN_SLOT;
                rolling = false;
                finished = true;
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.playSound(SoundEvents.PLAYER_LEVELUP, 0.85F, 1.15F);
                }
                updateButton();
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        updateButton();

        long balance = CasinoGuiHelper.getClientBalance();
        CasinoGuiHelper.drawLuxuryPanel(g, left, top, PANEL_W, PANEL_H, false);
        CasinoGuiHelper.drawHeader(g, this.font, left, top, PANEL_W, this.title, balance);

        // CS:GO Roulette Viewport
        int vx = left + 16;
        int vy = top + 44;
        int vw = PANEL_W - 32;
        int vh = 68;

        g.fill(vx, vy, vx + vw, vy + vh, 0xFF0A0B12);
        CasinoGuiHelper.drawBorder(g, vx, vy, vw, vh, vip ? 0xFFFFD700 : 0xFF38BDF8);

        int cardW = 54;
        int cardH = 56;
        int centerX = vx + vw / 2;

        g.enableScissor(vx + 2, vy + 2, vx + vw - 2, vy + vh - 2);
        for (int i = 0; i < strip.size(); i++) {
            double dx = (i - scrollPos) * (cardW + 4);
            int cx = (int) Math.round(centerX + dx - cardW / 2.0);
            if (cx + cardW < vx - 10 || cx > vx + vw + 10) continue;

            PrizeShopCatalog.CaseReward r = strip.get(i);
            int rCol = PrizeShopCatalog.rarityColor(r.rarity());
            int cy = vy + 6;

            g.fill(cx, cy, cx + cardW, cy + cardH, 0xFF171926);
            g.fill(cx, cy + cardH - 4, cx + cardW, cy + cardH, rCol);
            CasinoGuiHelper.drawBorder(g, cx, cy, cardW, cardH, rCol);

            g.renderItem(new ItemStack(r.item(), Math.max(1, r.count())), cx + cardW / 2 - 8, cy + 12);
            String sub = r.bonusChips() > 0 ? ("+" + ChipEconomy.formatChips(r.bonusChips())) : ("×" + r.count());
            g.drawCenteredString(this.font, sub, cx + cardW / 2, cy + 36, 0xFFFFFFFF);
        }
        g.disableScissor();

        // Center Golden Needle
        g.fill(centerX - 1, vy - 3, centerX + 1, vy + vh + 3, 0xFFFFEA55);
        g.drawCenteredString(this.font, "▼", centerX, vy - 9, 0xFFFFEA55);

        // Result / Owned Cases Box
        int infoY = top + 124;
        g.fill(left + 16, infoY, left + PANEL_W - 16, infoY + 54, 0xAA10121C);
        CasinoGuiHelper.drawBorder(g, left + 16, infoY, PANEL_W - 32, 54, CasinoGuiHelper.COLOR_GOLD_DARK);

        if (finished && wonReward != null) {
            int rCol = PrizeShopCatalog.rarityColor(wonReward.rarity());
            g.drawCenteredString(this.font, Component.translatable(PrizeShopCatalog.rarityKey(wonReward.rarity())),
                    left + PANEL_W / 2, infoY + 8, rCol);
            String rewardText = wonReward.bonusChips() > 0
                    ? "+" + ChipEconomy.formatChips(wonReward.bonusChips()) + " фишек!"
                    : Component.translatable(wonReward.item().getDescriptionId()).getString() + " ×" + wonReward.count();
            g.drawCenteredString(this.font, rewardText, left + PANEL_W / 2, infoY + 24, 0xFFFFFFFF);
        } else {
            g.drawCenteredString(this.font, Component.translatable("gui.skycasino.case.owned", ownedCases()),
                    left + PANEL_W / 2, infoY + 14, ownedCases() > 0 ? 0xFFFFD15C : 0xFFFF6666);
            g.drawCenteredString(this.font, Component.translatable("gui.skycasino.case.hint"),
                    left + PANEL_W / 2, infoY + 32, 0xFFAAAABB);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
