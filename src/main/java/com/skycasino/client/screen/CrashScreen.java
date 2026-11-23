package com.skycasino.client.screen;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.network.CrashActionPacket;
import com.skycasino.network.CrashStatePacket;
import com.skycasino.network.ModNetwork;
import com.skycasino.network.ServerCrashManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CrashScreen extends Screen {
    private static final int PANEL_W = 340;
    private static final int PANEL_H = 232;
    private static final long[] BETS = {25L, 100L, 500L, 2500L, 10000L};

    private final BlockPos pos;
    private int left;
    private int top;

    private int betIndex = 1;
    private int phase = ServerCrashManager.PHASE_IDLE;
    private double displayMult = 1.00;
    private long startClientMs = 0;
    private long activeBet = 0;
    private long lastPayout = 0;
    private int pollTicks = 0;
    private final List<Double> history = new ArrayList<>();

    private Button launchBtn;
    private Button cashoutBtn;

    public CrashScreen(BlockPos pos) {
        super(Component.translatable("screen.skycasino.crash"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        for (int i = 0; i < BETS.length; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(String.valueOf(BETS[i])), b -> {
                if (phase != ServerCrashManager.PHASE_RUNNING) betIndex = idx;
            }).bounds(left + 16 + i * 48, top + 174, 44, 18).build());
        }

        this.launchBtn = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.crash.launch"),
                b -> ModNetwork.sendToServer(new CrashActionPacket(CrashActionPacket.ACTION_START, BETS[betIndex], pos)))
                .bounds(left + 16, top + 198, 86, 22).build());

        addRenderableWidget(Button.builder(Component.literal("§c§lALL-IN"),
                b -> {
                    long bal = Math.min(CasinoGuiHelper.getClientBalance(), 10_000_000L);
                    if (bal > 0 && phase != ServerCrashManager.PHASE_RUNNING) {
                        ModNetwork.sendToServer(new CrashActionPacket(CrashActionPacket.ACTION_START, bal, pos));
                    }
                })
                .bounds(left + 106, top + 198, 54, 22).build());

        this.cashoutBtn = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.crash.cashout"),
                b -> ModNetwork.sendToServer(new CrashActionPacket(CrashActionPacket.ACTION_CASHOUT, 0, pos)))
                .bounds(left + 164, top + 198, 96, 22).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.close"), b -> this.onClose())
                .bounds(left + PANEL_W - 72, top + 198, 56, 22).build());

        updateButtons();
    }

    private void updateButtons() {
        boolean running = (phase == ServerCrashManager.PHASE_RUNNING);
        if (launchBtn != null) launchBtn.active = !running && CasinoGuiHelper.getClientBalance() >= BETS[betIndex];
        if (cashoutBtn != null) cashoutBtn.active = running;
    }

    public void onStateUpdate(CrashStatePacket packet) {
        int prevPhase = this.phase;
        this.phase = packet.phase;
        this.activeBet = packet.bet;
        this.lastPayout = packet.payout;

        if (packet.phase == ServerCrashManager.PHASE_RUNNING && prevPhase != ServerCrashManager.PHASE_RUNNING) {
            this.startClientMs = System.currentTimeMillis();
            this.displayMult = 1.00;
            this.pollTicks = 0;
        } else if (packet.phase == ServerCrashManager.PHASE_CRASHED) {
            this.displayMult = packet.crashPoint;
            addHistory(packet.crashPoint);
        } else if (packet.phase == ServerCrashManager.PHASE_CASHED_OUT) {
            this.displayMult = packet.currentMultiplier;
            addHistory(packet.crashPoint);
        }
        updateButtons();
    }

    private void addHistory(double val) {
        if (val <= 0) return;
        history.add(0, val);
        if (history.size() > 6) history.remove(history.size() - 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (phase == ServerCrashManager.PHASE_RUNNING) {
            double elapsedSec = Math.max(0.0, (System.currentTimeMillis() - startClientMs) / 1000.0);
            displayMult = Math.max(1.0, Math.exp(elapsedSec * ServerCrashManager.GROWTH_RATE));
            pollTicks++;
            if (pollTicks % 6 == 0) {
                ModNetwork.sendToServer(new CrashActionPacket(CrashActionPacket.ACTION_POLL, 0, pos));
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.playSound(SoundEvents.NOTE_BLOCK_BIT.value(), 0.25F, (float) Math.min(2.0, 0.8 + displayMult * 0.08));
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        updateButtons();

        long balance = CasinoGuiHelper.getClientBalance();
        CasinoGuiHelper.drawLuxuryPanel(g, left, top, PANEL_W, PANEL_H, false);
        CasinoGuiHelper.drawHeader(g, this.font, left, top, PANEL_W, this.title, balance);

        // Flight chart viewport
        int gx = left + 16;
        int gy = top + 36;
        int gw = PANEL_W - 32;
        int gh = 112;
        g.fillGradient(gx, gy, gx + gw, gy + gh, 0xFF0A0D1A, 0xFF141126);
        CasinoGuiHelper.drawBorder(g, gx, gy, gw, gh, phase == ServerCrashManager.PHASE_CRASHED ? 0xFFFF4444 : CasinoGuiHelper.COLOR_GOLD);

        // Grid lines
        for (int i = 1; i <= 3; i++) {
            int ly = gy + gh - (gh * i / 4);
            g.fill(gx + 1, ly, gx + gw - 1, ly + 1, 0x22FFFFFF);
        }

        // Draw rising curve
        double progress = Math.min(1.0, Math.log(Math.max(1.0, displayMult)) / Math.log(12.0));
        int curveColor = switch (phase) {
            case ServerCrashManager.PHASE_CRASHED -> 0xFFFF4455;
            case ServerCrashManager.PHASE_CASHED_OUT -> 0xFF44FF88;
            default -> 0xFF44DDFF;
        };
        int steps = 40;
        int lastX = gx + 12;
        int lastY = gy + gh - 12;
        for (int i = 1; i <= steps; i++) {
            double t = (i / (double) steps) * progress;
            int px = gx + 12 + (int) ((gw - 36) * t);
            int py = gy + gh - 12 - (int) ((gh - 26) * Math.pow(t, 1.5));
            g.fill(px - 1, py - 1, px + 2, py + 2, curveColor);
            lastX = px;
            lastY = py;
        }

        // Rocket icon at tip
        g.renderItem(new ItemStack(phase == ServerCrashManager.PHASE_CRASHED ? Items.TNT : Items.FIREWORK_ROCKET), lastX - 8, lastY - 8);

        // Big multiplier display in center
        String multStr = String.format(Locale.US, "×%.2f", displayMult);
        int multColor = switch (phase) {
            case ServerCrashManager.PHASE_CRASHED -> 0xFFFF5555;
            case ServerCrashManager.PHASE_CASHED_OUT -> 0xFF66FF88;
            default -> 0xFFFFE566;
        };
        g.pose().pushPose();
        g.pose().scale(2.0F, 2.0F, 1.0F);
        g.drawCenteredString(this.font, multStr, (gx + gw / 2) / 2, (gy + 32) / 2, multColor);
        g.pose().popPose();

        // Status subtitle
        if (phase == ServerCrashManager.PHASE_RUNNING) {
            long potential = Math.round(activeBet * displayMult);
            g.drawCenteredString(this.font, Component.translatable("gui.skycasino.crash.potential", ChipEconomy.formatChips(potential)),
                    gx + gw / 2, gy + 66, 0xFF88EEFF);
        } else if (phase == ServerCrashManager.PHASE_CASHED_OUT) {
            g.drawCenteredString(this.font, Component.translatable("gui.skycasino.won_chips", ChipEconomy.formatChips(lastPayout)),
                    gx + gw / 2, gy + 66, 0xFF66FF88);
        } else if (phase == ServerCrashManager.PHASE_CRASHED) {
            g.drawCenteredString(this.font, Component.translatable("gui.skycasino.crash.exploded"),
                    gx + gw / 2, gy + 66, 0xFFFF6666);
        }

        // History badges
        int hx = left + 16;
        int hy = top + 153;
        g.drawString(this.font, Component.translatable("gui.skycasino.rl.history"), hx, hy + 3, 0xFFBBBBCC, false);
        hx += 52;
        for (double h : history) {
            int col = h >= 5.0 ? 0xFF9E6B1E : (h >= 2.0 ? 0xFF1A6E3E : 0xFF7A222C);
            String s = String.format(Locale.US, "×%.2f", h);
            int w = this.font.width(s) + 8;
            g.fill(hx, hy, hx + w, hy + 14, col);
            g.drawCenteredString(this.font, s, hx + w / 2, hy + 3, 0xFFFFFFFF);
            hx += w + 4;
        }

        // Selected bet border
        int selBx = left + 16 + betIndex * 48;
        CasinoGuiHelper.drawBorder(g, selBx - 1, top + 173, 46, 20, CasinoGuiHelper.COLOR_GOLD);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
