package com.skycasino.client.screen;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.network.LudikActionPacket;
import com.skycasino.network.LudikSyncPacket;
import com.skycasino.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class LudikPanelScreen extends Screen {
    private static final int PANEL_W = 352;
    private static final int PANEL_H = 236;

    private int left;
    private int top;

    private int addiction = 0;
    private int winStreak = 0;
    private int lossStreak = 0;
    private long debt = 0;
    private int debtSecsLeft = 0;
    private int withdrawalStage = 0;
    private long pawnValue = 0;

    private Button pawnBtn;
    private Button repayBtn;
    private Button dodepBtn;

    public LudikPanelScreen() {
        super(Component.translatable("screen.skycasino.ludik_panel"));
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        // Micro-loan buttons (+25% interest, Collectors in 5 min!)
        int ly = top + 114;
        addRenderableWidget(Button.builder(Component.literal("+1 000"),
                b -> ModNetwork.sendToServer(new LudikActionPacket(LudikActionPacket.ACTION_LOAN, 1_000L)))
                .bounds(left + 16, ly, 58, 20).build());

        addRenderableWidget(Button.builder(Component.literal("+5 000"),
                b -> ModNetwork.sendToServer(new LudikActionPacket(LudikActionPacket.ACTION_LOAN, 5_000L)))
                .bounds(left + 78, ly, 58, 20).build());

        addRenderableWidget(Button.builder(Component.literal("+25 000"),
                b -> ModNetwork.sendToServer(new LudikActionPacket(LudikActionPacket.ACTION_LOAN, 25_000L)))
                .bounds(left + 140, ly, 64, 20).build());

        this.repayBtn = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.ludik.repay"),
                b -> ModNetwork.sendToServer(new LudikActionPacket(LudikActionPacket.ACTION_REPAY, 0)))
                .bounds(left + 210, ly, 126, 20).build());

        // Pawn Shop button (only usable in Withdrawal!)
        this.pawnBtn = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.ludik.pawn_btn"),
                b -> ModNetwork.sendToServer(new LudikActionPacket(LudikActionPacket.ACTION_PAWN, 0)))
                .bounds(left + 186, top + 162, 150, 22).build());

        // Emergency Dodep button (+100 chips when balance == 0)
        this.dodepBtn = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.ludik.dodep"),
                b -> ModNetwork.sendToServer(new LudikActionPacket(LudikActionPacket.ACTION_DODEP, 0)))
                .bounds(left + 16, top + PANEL_H - 30, 176, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.close"), b -> this.onClose())
                .bounds(left + PANEL_W - 82, top + PANEL_H - 30, 66, 20).build());

        ModNetwork.sendToServer(new LudikActionPacket(LudikActionPacket.ACTION_SYNC, 0));
    }

    public void onSync(LudikSyncPacket pkt) {
        this.addiction = pkt.addiction;
        this.winStreak = pkt.winStreak;
        this.lossStreak = pkt.lossStreak;
        this.debt = pkt.debt;
        this.debtSecsLeft = pkt.debtSecsLeft;
        this.withdrawalStage = pkt.withdrawalStage;
        this.pawnValue = pkt.pawnValue;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        long balance = CasinoGuiHelper.getClientBalance();
        if (repayBtn != null) repayBtn.active = debt > 0 && balance > 0;
        if (pawnBtn != null) pawnBtn.active = withdrawalStage > 0 && pawnValue > 0;
        if (dodepBtn != null) dodepBtn.active = balance == 0;

        CasinoGuiHelper.drawLuxuryPanel(g, left, top, PANEL_W, PANEL_H, false);
        CasinoGuiHelper.drawHeader(g, this.font, left, top, PANEL_W, this.title, balance);

        // === Section 1: Addiction Meter & Streaks ===
        g.fill(left + 14, top + 34, left + PANEL_W - 14, top + 90, 0xAA12101A);
        CasinoGuiHelper.drawBorder(g, left + 14, top + 34, PANEL_W - 28, 56, CasinoGuiHelper.COLOR_GOLD_DARK);

        g.drawString(this.font, Component.translatable("gui.skycasino.ludik.addiction", addiction), left + 22, top + 40, 0xFFE8B4FF, true);
        // Progress bar
        int barX = left + 22;
        int barY = top + 52;
        int barW = PANEL_W - 44;
        g.fill(barX, barY, barX + barW, barY + 8, 0xFF1F1D2C);
        int fillW = (int) (barW * (addiction / 100.0));
        int barCol = withdrawalStage > 0 ? 0xFFFF3355 : (addiction >= 60 ? 0xFFD946EF : 0xFFFFB830);
        g.fill(barX, barY, barX + fillW, barY + 8, barCol);

        // Status line: Hot Hand / Tilt / Withdrawal
        String streakTxt = winStreak >= 3
                ? "§6§l🔥 ГОРЯЧАЯ РУКА ×" + winStreak + " (+" + (winStreak >= 7 ? 60 : winStreak >= 5 ? 40 : 25) + "%)"
                : (lossStreak >= 3 ? "§4💢 ТИЛЬТ (" + lossStreak + " слива подряд)" : "§7Серия побед: §f" + winStreak);
        g.drawString(this.font, streakTxt, left + 22, top + 66, 0xFFFFFFFF, false);

        String lomkaTxt = withdrawalStage > 0 ? "§c§lЛОМКА! (Ломбард открыт)" : "§aЛомки нет";
        g.drawString(this.font, lomkaTxt, left + PANEL_W - 22 - this.font.width(lomkaTxt), top + 66, 0xFFFFFFFF, false);

        // === Section 2: Micro-Loan & Collectors ===
        g.drawString(this.font, Component.translatable("gui.skycasino.ludik.loan_title"), left + 16, top + 98, 0xFFFFD15C, true);
        String debtInfo = debt > 0
                ? "§cДолг: " + ChipEconomy.formatChips(debt) + " §7(Коллекторы: " + debtSecsLeft + "с)"
                : "§aДолгов нет (+25% при займе)";
        g.drawString(this.font, debtInfo, left + 130, top + 98, 0xFFFFFFFF, false);

        // === Section 3: Pawn Shop (Ломбард при ломке) ===
        int py = top + 142;
        g.fill(left + 14, py, left + PANEL_W - 14, py + 52, withdrawalStage > 0 ? 0xAA2B0E1A : 0x8810121C);
        CasinoGuiHelper.drawBorder(g, left + 14, py, PANEL_W - 28, 52, withdrawalStage > 0 ? 0xFFFF4477 : 0x55777788);

        g.drawString(this.font, Component.translatable("gui.skycasino.ludik.pawn_title"), left + 22, py + 6,
                withdrawalStage > 0 ? 0xFFFF77AA : 0xFF9999AA, true);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack held = mc.player.getMainHandItem();
            if (held.isEmpty()) held = mc.player.getOffhandItem();
            if (!held.isEmpty()) {
                g.renderItem(held, left + 22, py + 22);
            }
        }
        String pawnDesc = withdrawalStage > 0
                ? (pawnValue > 0 ? "§6Оценка: +" + ChipEconomy.formatChips(pawnValue) + " фишек" : "§7Возьми вещь в руку")
                : "§7Доступно только в Ломке!";
        g.drawString(this.font, pawnDesc, left + 44, py + 26, 0xFFFFFFFF, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
