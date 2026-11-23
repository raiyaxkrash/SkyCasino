package com.skycasino.client.screen;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.network.FortuneWheelSpinRequestPacket;
import com.skycasino.network.FortuneWheelSpinResultPacket;
import com.skycasino.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class FortuneWheelScreen extends Screen {
    private static final int PANEL_W = 320;
    private static final int PANEL_H = 232;
    private static final long[] BETS = {10L, 25L, 100L, 500L, 1000L};

    private static final String[] SECTOR_LABELS = {
            "×0", "×2", "×0.5", "×3", "×0", "×5",
            "×0.5", "×1.5", "×0", "×10", "×0.5", "×25"
    };
    private static final int[] SECTOR_COLORS = {
            0xFF2B2B38, 0xFF1F7A4C, 0xFF5A421C, 0xFF1D5E9E,
            0xFF2B2B38, 0xFF8C2D8C, 0xFF5A421C, 0xFF2E6B5E,
            0xFF2B2B38, 0xFFB83227, 0xFF5A421C, 0xFFD99B00
    };

    private final BlockPos pos;
    private int left;
    private int top;

    private int betIndex = 1;
    private boolean spinning = false;
    private double currentIndex = 0.0;
    private double targetIndex = 0.0;
    private int spinTick = 0;
    private int lastSector = -1;
    private long lastPayout = 0;

    private Button spinBtn;

    public FortuneWheelScreen(BlockPos pos) {
        super(Component.translatable("screen.skycasino.fortune_wheel"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        for (int i = 0; i < BETS.length; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(String.valueOf(BETS[i])), b -> {
                if (!spinning) betIndex = idx;
            }).bounds(left + 16 + i * 42, top + 174, 38, 18).build());
        }

        this.spinBtn = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.spin"), b -> startSpin(false))
                .bounds(left + 16, top + 198, 146, 22).build());

        addRenderableWidget(Button.builder(Component.literal("§c§lALL-IN"), b -> startSpin(true))
                .bounds(left + 166, top + 198, 60, 22).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.close"), b -> this.onClose())
                .bounds(left + PANEL_W - 84, top + 198, 68, 22).build());
    }

    private void startSpin(boolean allIn) {
        if (spinning) return;
        long bal = CasinoGuiHelper.getClientBalance();
        long bet = allIn ? Math.min(bal, 10_000_000L) : BETS[betIndex];
        if (bet <= 0 || bal < bet) return;

        spinning = true;
        spinTick = 0;
        spinBtn.active = false;
        ModNetwork.sendToServer(new FortuneWheelSpinRequestPacket(bet, pos));
    }

    public void onSpinResult(FortuneWheelSpinResultPacket packet) {
        this.lastSector = packet.sectorIndex;
        this.lastPayout = packet.payout;

        double mod = currentIndex % 12.0;
        double delta = (packet.sectorIndex - mod + 12.0) % 12.0;
        this.targetIndex = currentIndex + 36.0 + delta; // 3 full rotations + target
    }

    @Override
    public void tick() {
        super.tick();
        if (spinning) {
            spinTick++;
            double rem = targetIndex - currentIndex;
            if (rem > 0.04 && spinTick < 70) {
                int prev = (int) Math.floor(currentIndex);
                currentIndex += Math.max(0.06, rem * 0.13);
                if ((int) Math.floor(currentIndex) != prev && Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 0.5F, 1.4F);
                }
            } else {
                currentIndex = targetIndex;
                spinning = false;
                spinBtn.active = true;
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        long balance = CasinoGuiHelper.getClientBalance();
        CasinoGuiHelper.drawLuxuryPanel(g, left, top, PANEL_W, PANEL_H, false);
        CasinoGuiHelper.drawHeader(g, this.font, left, top, PANEL_W, this.title, balance);

        // Draw circular 12-sector Wheel of Fortune using polar badge layout
        int cx = left + 106;
        int cy = top + 102;
        int radius = 50;

        // Outer golden ring glow
        g.fill(cx - radius - 14, cy - radius - 14, cx + radius + 14, cy + radius + 14, 0x33FFD15C);
        CasinoGuiHelper.drawBorder(g, cx - radius - 14, cy - radius - 14, (radius + 14) * 2, (radius + 14) * 2, CasinoGuiHelper.COLOR_GOLD_DARK);

        int activeSector = ((int) Math.round(currentIndex) % 12 + 12) % 12;

        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians((i - currentIndex) * 30.0 - 90.0);
            int sx = cx + (int) Math.round(Math.cos(angle) * radius);
            int sy = cy + (int) Math.round(Math.sin(angle) * radius);
            boolean isTop = (i == activeSector);

            int bw = 28;
            int bh = 16;
            g.fill(sx - bw / 2, sy - bh / 2, sx + bw / 2, sy + bh / 2, SECTOR_COLORS[i]);
            CasinoGuiHelper.drawBorder(g, sx - bw / 2, sy - bh / 2, bw, bh, isTop ? 0xFFFFEA55 : 0x88FFD15C);
            g.drawCenteredString(this.font, SECTOR_LABELS[i], sx, sy - 4, isTop ? 0xFFFFFF88 : 0xFFFFFFFF);
        }

        // Center hub & pointer arrow at top
        g.fill(cx - 16, cy - 16, cx + 16, cy + 16, 0xFF2B1218);
        CasinoGuiHelper.drawBorder(g, cx - 16, cy - 16, 32, 32, CasinoGuiHelper.COLOR_GOLD);
        g.drawCenteredString(this.font, "★", cx, cy - 4, CasinoGuiHelper.COLOR_GOLD);
        g.drawCenteredString(this.font, "▼", cx, cy - radius - 22, 0xFFFFEA55);

        // Right info panel
        int infoX = left + 188;
        int infoY = top + 42;
        int infoW = 118;
        int infoH = 122;
        g.fill(infoX, infoY, infoX + infoW, infoY + infoH, 0xAA101018);
        CasinoGuiHelper.drawBorder(g, infoX, infoY, infoW, infoH, CasinoGuiHelper.COLOR_GOLD_DARK);

        g.drawCenteredString(this.font, Component.translatable("gui.skycasino.fw.prizes"), infoX + infoW / 2, infoY + 8, CasinoGuiHelper.COLOR_GOLD);
        g.drawString(this.font, "§6§l×25  JACKPOT!", infoX + 10, infoY + 26, 0xFFFFD700, false);
        g.drawString(this.font, "§c×10  MEGA WIN", infoX + 10, infoY + 40, 0xFFFF6666, false);
        g.drawString(this.font, "§d×5   SUPER", infoX + 10, infoY + 54, 0xFFEE88FF, false);
        g.drawString(this.font, "§b×3 / §a×2 / ×1.5", infoX + 10, infoY + 68, 0xFF88EEFF, false);
        g.drawString(this.font, "§7×0.5 / ×0", infoX + 10, infoY + 82, 0xFFAAAAAA, false);

        if (!spinning && lastSector >= 0) {
            if (lastPayout > 0) {
                g.drawCenteredString(this.font, Component.translatable("gui.skycasino.won_chips", ChipEconomy.formatChips(lastPayout)),
                        infoX + infoW / 2, infoY + 104, 0xFF66FF88);
            } else {
                g.drawCenteredString(this.font, Component.translatable("gui.skycasino.try_again"),
                        infoX + infoW / 2, infoY + 104, 0xFFFF7777);
            }
        }

        // Bet highlight
        int selBx = left + 16 + betIndex * 42;
        CasinoGuiHelper.drawBorder(g, selBx - 1, top + 173, 40, 20, CasinoGuiHelper.COLOR_GOLD);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
