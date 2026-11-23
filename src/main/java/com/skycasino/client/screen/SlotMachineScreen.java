package com.skycasino.client.screen;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.network.ModNetwork;
import com.skycasino.network.SlotSpinRequestPacket;
import com.skycasino.network.SlotSpinResultPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Random;

public class SlotMachineScreen extends Screen {
    private static final int PANEL_W = 320;
    private static final int PANEL_H = 228;
    private static final long[] BETS = {5L, 25L, 100L, 500L, 1000L};

    private static final ItemStack[] SYMBOL_ITEMS = {
            new ItemStack(Items.SWEET_BERRIES), // 0: Cherry
            new ItemStack(Items.GLOW_BERRIES),  // 1: Lemon
            new ItemStack(Items.BELL),          // 2: Bell
            new ItemStack(Items.EMERALD),       // 3: Emerald
            new ItemStack(Items.DIAMOND),       // 4: Diamond
            new ItemStack(Items.NETHER_STAR),   // 5: Star
            new ItemStack(Items.GOLD_BLOCK)     // 6: Lucky 7 (drawn specially)
    };
    private static final String[] PAYOUT_LABELS = {"×3", "×4", "×6", "×10", "×16", "×25", "×50"};

    private final BlockPos pos;
    private int left;
    private int top;

    private int betIndex = 1; // Default 25
    private int[] displayGrid = {6, 5, 4, 6, 6, 6, 3, 2, 1};
    private int[] targetGrid = {6, 5, 4, 6, 6, 6, 3, 2, 1};

    private boolean spinning = false;
    private int spinTicks = 0;
    private long lastPayout = 0;
    private int winMask = 0;
    private boolean lastJackpot = false;
    private boolean hasSpun = false;
    private final Random random = new Random();

    private Button spinButton;

    public SlotMachineScreen(BlockPos pos) {
        super(Component.translatable("screen.skycasino.slot_machine"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        // Bet selector buttons
        int bx = left + 16;
        int by = top + 168;
        for (int i = 0; i < BETS.length; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(String.valueOf(BETS[i])), b -> {
                if (!spinning) this.betIndex = idx;
            }).bounds(bx + i * 40, by, 36, 18).build());
        }

        // SPIN button + ALL-IN button
        this.spinButton = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.spin"), b -> startSpin(false))
                .bounds(left + 16, top + 194, 134, 22).build());

        addRenderableWidget(Button.builder(Component.literal("§c§lALL-IN"), b -> startSpin(true))
                .bounds(left + 154, top + 194, 58, 22).build());

        // Close button
        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.close"), b -> this.onClose())
                .bounds(left + PANEL_W - 86, top + 194, 70, 22).build());
    }

    private void startSpin(boolean allIn) {
        if (spinning) return;
        long bal = CasinoGuiHelper.getClientBalance();
        long bet = allIn ? Math.min(bal, 10_000_000L) : BETS[betIndex];
        if (bet <= 0 || bal < bet) {
            return;
        }
        this.spinning = true;
        this.spinTicks = 0;
        this.winMask = 0;
        this.lastPayout = 0;
        this.lastJackpot = false;
        this.spinButton.active = false;
        ModNetwork.sendToServer(new SlotSpinRequestPacket(bet, pos));
    }

    public void onSpinResult(SlotSpinResultPacket packet) {
        System.arraycopy(packet.grid, 0, this.targetGrid, 0, 9);
        this.lastPayout = packet.payout;
        this.winMask = packet.winMask;
        this.lastJackpot = packet.jackpot;
        this.hasSpun = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (spinning) {
            spinTicks++;
            if (spinTicks % 2 == 0) {
                for (int col = 0; col < 3; col++) {
                    int stopTick = 18 + col * 10; // Col 0 stops at 18, Col 1 at 28, Col 2 at 38
                    if (spinTicks < stopTick) {
                        for (int row = 0; row < 3; row++) {
                            displayGrid[row * 3 + col] = random.nextInt(7);
                        }
                    } else if (spinTicks == stopTick) {
                        for (int row = 0; row < 3; row++) {
                            displayGrid[row * 3 + col] = targetGrid[row * 3 + col];
                        }
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.7F, 1.0F + col * 0.15F);
                        }
                    }
                }
            }
            if (spinTicks >= 40) {
                System.arraycopy(targetGrid, 0, displayGrid, 0, 9);
                spinning = false;
                spinButton.active = true;
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        long balance = CasinoGuiHelper.getClientBalance();
        CasinoGuiHelper.drawLuxuryPanel(g, left, top, PANEL_W, PANEL_H, false);
        CasinoGuiHelper.drawHeader(g, this.font, left, top, PANEL_W, this.title, balance);

        // Slot machine frame
        int reelAreaX = left + 18;
        int reelAreaY = top + 36;
        int cellW = 60;
        int cellH = 38;
        int reelAreaW = cellW * 3 + 12;
        int reelAreaH = cellH * 3 + 12;

        g.fill(reelAreaX, reelAreaY, reelAreaX + reelAreaW, reelAreaY + reelAreaH, 0xFF0A0A10);
        CasinoGuiHelper.drawBorder(g, reelAreaX, reelAreaY, reelAreaW, reelAreaH, CasinoGuiHelper.COLOR_GOLD);

        // Highlight middle payline indicator arrows
        int midY = reelAreaY + 6 + cellH + cellH / 2;
        g.drawString(this.font, "▶", reelAreaX - 9, midY - 4, CasinoGuiHelper.COLOR_GOLD, false);
        g.drawString(this.font, "◀", reelAreaX + reelAreaW + 2, midY - 4, CasinoGuiHelper.COLOR_GOLD, false);

        // Draw 3x3 reel cells
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int cx = reelAreaX + 6 + col * cellW;
                int cy = reelAreaY + 6 + row * cellH;
                boolean isWinCell = !spinning && isCellInWinningLine(row, col);

                int bg = isWinCell ? 0xFF4A360A : (row == 1 ? 0xFF1F1D2C : 0xFF15141F);
                g.fill(cx + 2, cy + 2, cx + cellW - 2, cy + cellH - 2, bg);
                CasinoGuiHelper.drawBorder(g, cx + 2, cy + 2, cellW - 4, cellH - 4,
                        isWinCell ? 0xFFFFEA55 : 0xFF3A354A);

                int sym = displayGrid[row * 3 + col];
                if (sym == 6) {
                    // Glowing Lucky 7
                    g.fill(cx + 12, cy + 6, cx + cellW - 12, cy + cellH - 6, 0x55FFD15C);
                    g.drawCenteredString(this.font, "§6§l7", cx + cellW / 2, cy + cellH / 2 - 4, 0xFFFFD700);
                } else {
                    g.renderItem(SYMBOL_ITEMS[sym], cx + cellW / 2 - 8, cy + cellH / 2 - 8);
                }
            }
        }

        // Payout table on the right
        int payX = left + 220;
        int payY = top + 36;
        int payW = 88;
        int payH = 126;
        g.fill(payX, payY, payX + payW, payY + payH, 0xAA101018);
        CasinoGuiHelper.drawBorder(g, payX, payY, payW, payH, CasinoGuiHelper.COLOR_GOLD_DARK);
        g.drawCenteredString(this.font, Component.translatable("gui.skycasino.payouts"), payX + payW / 2, payY + 5, CasinoGuiHelper.COLOR_GOLD);

        for (int i = 6; i >= 0; i--) {
            int ry = payY + 18 + (6 - i) * 15;
            if (i == 6) {
                g.drawString(this.font, "§6§l7-7-7", payX + 6, ry + 3, 0xFFFFD700, false);
            } else {
                g.renderItem(SYMBOL_ITEMS[i], payX + 6, ry - 1);
                g.drawString(this.font, "×3", payX + 24, ry + 3, 0xFFCCCCCC, false);
            }
            g.drawString(this.font, PAYOUT_LABELS[i], payX + payW - this.font.width(PAYOUT_LABELS[i]) - 6, ry + 3, 0xFF77EE88, false);
        }

        // Current bet highlight
        int selBx = left + 16 + betIndex * 40;
        CasinoGuiHelper.drawBorder(g, selBx - 1, top + 167, 38, 20, CasinoGuiHelper.COLOR_GOLD);

        // Result banner
        if (!spinning && hasSpun) {
            if (lastPayout > 0) {
                Component msg = lastJackpot
                        ? Component.translatable("gui.skycasino.jackpot", ChipEconomy.formatChips(lastPayout))
                        : Component.translatable("gui.skycasino.won_chips", ChipEconomy.formatChips(lastPayout));
                g.drawCenteredString(this.font, msg, payX + payW / 2, top + 172, lastJackpot ? 0xFFFFE555 : 0xFF66FF88);
            } else {
                g.drawCenteredString(this.font, Component.translatable("gui.skycasino.try_again"), payX + payW / 2, top + 172, 0xFFCC7777);
            }
        } else if (CasinoGuiHelper.getClientBalance() < BETS[betIndex]) {
            g.drawCenteredString(this.font, Component.translatable("gui.skycasino.not_enough_chips"), payX + payW / 2, top + 172, 0xFFFF5555);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private boolean isCellInWinningLine(int row, int col) {
        if ((winMask & (1 << 0)) != 0 && row == 0) return true;
        if ((winMask & (1 << 1)) != 0 && row == 1) return true;
        if ((winMask & (1 << 2)) != 0 && row == 2) return true;
        if ((winMask & (1 << 3)) != 0 && row == col) return true;
        if ((winMask & (1 << 4)) != 0 && (row + col == 2)) return true;
        if ((winMask & (1 << 5)) != 0 && row == 1) return true;
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
