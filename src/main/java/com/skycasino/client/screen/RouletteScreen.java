package com.skycasino.client.screen;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.network.ModNetwork;
import com.skycasino.network.RouletteSpinRequestPacket;
import com.skycasino.network.RouletteSpinResultPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;

public class RouletteScreen extends Screen {
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 240;
    private static final long[] BETS = {10L, 25L, 100L, 500L, 1000L};

    // Authentic European roulette wheel sequence
    private static final int[] WHEEL_ORDER = {
            0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10,
            5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
    };

    private final BlockPos pos;
    private int left;
    private int top;

    private int betIndex = 1;
    private int selectedBetType = RouletteSpinRequestPacket.BET_RED;
    private int selectedNumber = 7;

    private boolean spinning = false;
    private double wheelOffset = 0.0;
    private double targetOffset = 0.0;
    private int spinTick = 0;
    private int lastWinningNumber = -1;
    private long lastPayout = 0;
    private final List<Integer> history = new ArrayList<>();

    private Button spinBtn;

    public RouletteScreen(BlockPos pos) {
        super(Component.translatable("screen.skycasino.roulette"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        // Outside bets row 1
        int by1 = top + 76;
        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.rl.red"), b -> selectBet(RouletteSpinRequestPacket.BET_RED, 0))
                .bounds(left + 12, by1, 54, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.rl.black"), b -> selectBet(RouletteSpinRequestPacket.BET_BLACK, 0))
                .bounds(left + 68, by1, 54, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.rl.even"), b -> selectBet(RouletteSpinRequestPacket.BET_EVEN, 0))
                .bounds(left + 124, by1, 50, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.rl.odd"), b -> selectBet(RouletteSpinRequestPacket.BET_ODD, 0))
                .bounds(left + 176, by1, 50, 18).build());
        addRenderableWidget(Button.builder(Component.literal("1-18"), b -> selectBet(RouletteSpinRequestPacket.BET_LOW, 0))
                .bounds(left + 228, by1, 38, 18).build());
        addRenderableWidget(Button.builder(Component.literal("19-36"), b -> selectBet(RouletteSpinRequestPacket.BET_HIGH, 0))
                .bounds(left + 268, by1, 40, 18).build());
        addRenderableWidget(Button.builder(Component.literal("0 (×36)"), b -> selectBet(RouletteSpinRequestPacket.BET_STRAIGHT, 0))
                .bounds(left + 310, by1, 38, 18).build());

        // Dozens row
        int by2 = top + 98;
        addRenderableWidget(Button.builder(Component.literal("1-12 (×3)"), b -> selectBet(RouletteSpinRequestPacket.BET_DOZEN1, 0))
                .bounds(left + 12, by2, 76, 18).build());
        addRenderableWidget(Button.builder(Component.literal("13-24 (×3)"), b -> selectBet(RouletteSpinRequestPacket.BET_DOZEN2, 0))
                .bounds(left + 92, by2, 76, 18).build());
        addRenderableWidget(Button.builder(Component.literal("25-36 (×3)"), b -> selectBet(RouletteSpinRequestPacket.BET_DOZEN3, 0))
                .bounds(left + 172, by2, 76, 18).build());

        // Straight number selector (- / +)
        addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            selectedNumber = (selectedNumber + 36) % 37;
            selectedBetType = RouletteSpinRequestPacket.BET_STRAIGHT;
        }).bounds(left + 254, by2, 18, 18).build());

        addRenderableWidget(Button.builder(Component.literal("№"), b -> selectedBetType = RouletteSpinRequestPacket.BET_STRAIGHT)
                .bounds(left + 274, by2, 54, 18).build());

        addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            selectedNumber = (selectedNumber + 1) % 37;
            selectedBetType = RouletteSpinRequestPacket.BET_STRAIGHT;
        }).bounds(left + 330, by2, 18, 18).build());

        // Bet amounts
        int betY = top + 182;
        for (int i = 0; i < BETS.length; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(String.valueOf(BETS[i])), b -> {
                if (!spinning) betIndex = idx;
            }).bounds(left + 12 + i * 42, betY, 38, 18).build());
        }

        // Spin, ALL-IN & Close
        this.spinBtn = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.spin"), b -> startSpin(false))
                .bounds(left + 12, top + 206, 184, 22).build());

        addRenderableWidget(Button.builder(Component.literal("§c§lALL-IN"), b -> startSpin(true))
                .bounds(left + 200, top + 206, 62, 22).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.close"), b -> this.onClose())
                .bounds(left + PANEL_W - 84, top + 206, 72, 22).build());
    }

    private void selectBet(int type, int num) {
        if (!spinning) {
            this.selectedBetType = type;
            if (type == RouletteSpinRequestPacket.BET_STRAIGHT) {
                this.selectedNumber = num;
            }
        }
    }

    private void startSpin(boolean allIn) {
        if (spinning) return;
        long bal = CasinoGuiHelper.getClientBalance();
        long bet = allIn ? Math.min(bal, 10_000_000L) : BETS[betIndex];
        if (bet <= 0 || bal < bet) return;

        this.spinning = true;
        this.spinTick = 0;
        this.spinBtn.active = false;
        ModNetwork.sendToServer(new RouletteSpinRequestPacket(selectedBetType, selectedNumber, bet, pos));
    }

    public void onSpinResult(RouletteSpinResultPacket packet) {
        this.lastWinningNumber = packet.winningNumber;
        this.lastPayout = packet.payout;

        // Find index in WHEEL_ORDER
        int targetIdx = 0;
        for (int i = 0; i < WHEEL_ORDER.length; i++) {
            if (WHEEL_ORDER[i] == packet.winningNumber) {
                targetIdx = i;
                break;
            }
        }
        // Spin 2 full rotations + land on targetIdx
        double currentMod = wheelOffset % WHEEL_ORDER.length;
        double delta = (targetIdx - currentMod + WHEEL_ORDER.length) % WHEEL_ORDER.length;
        this.targetOffset = wheelOffset + WHEEL_ORDER.length * 2 + delta;
    }

    @Override
    public void tick() {
        super.tick();
        if (spinning) {
            spinTick++;
            double remaining = targetOffset - wheelOffset;
            if (remaining > 0.05 && spinTick < 65) {
                double step = Math.max(0.08, remaining * 0.14);
                int prevSlot = (int) Math.floor(wheelOffset);
                wheelOffset += step;
                if ((int) Math.floor(wheelOffset) != prevSlot && Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.35F, 1.5F);
                }
            } else {
                wheelOffset = targetOffset;
                spinning = false;
                spinBtn.active = true;
                if (lastWinningNumber >= 0) {
                    history.add(0, lastWinningNumber);
                    if (history.size() > 10) history.remove(history.size() - 1);
                }
            }
        }
    }

    private int colorForNumber(int n) {
        if (n == 0) return 0xFF1A8F3E;
        return RouletteSpinRequestPacket.RED_NUMBERS.contains(n) ? 0xFFC41E2E : 0xFF1B1C24;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        long balance = CasinoGuiHelper.getClientBalance();
        CasinoGuiHelper.drawLuxuryPanel(g, left, top, PANEL_W, PANEL_H, true);
        CasinoGuiHelper.drawHeader(g, this.font, left, top, PANEL_W, this.title, balance);

        // Animated horizontal roulette wheel ribbon
        int stripX = left + 14;
        int stripY = top + 36;
        int stripW = PANEL_W - 28;
        int stripH = 32;
        g.fill(stripX, stripY, stripX + stripW, stripY + stripH, 0xFF0A0A0E);
        CasinoGuiHelper.drawBorder(g, stripX, stripY, stripW, stripH, CasinoGuiHelper.COLOR_GOLD);

        int slotW = 28;
        int centerX = stripX + stripW / 2;
        int baseIdx = (int) Math.floor(wheelOffset);
        double frac = wheelOffset - baseIdx;

        g.enableScissor(stripX + 1, stripY + 1, stripX + stripW - 1, stripY + stripH - 1);
        for (int offset = -7; offset <= 7; offset++) {
            int idx = ((baseIdx + offset) % WHEEL_ORDER.length + WHEEL_ORDER.length) % WHEEL_ORDER.length;
            int num = WHEEL_ORDER[idx];
            int sx = (int) Math.round(centerX + (offset - frac) * slotW - slotW / 2.0);
            g.fill(sx + 1, stripY + 3, sx + slotW - 1, stripY + stripH - 3, colorForNumber(num));
            CasinoGuiHelper.drawBorder(g, sx + 1, stripY + 3, slotW - 2, stripH - 6, 0x66FFD15C);
            g.drawCenteredString(this.font, String.valueOf(num), sx + slotW / 2, stripY + 12, 0xFFFFFFFF);
        }
        g.disableScissor();

        // Center golden pointer
        g.fill(centerX - 1, stripY - 2, centerX + 1, stripY + stripH + 2, 0xFFFFE555);
        g.drawCenteredString(this.font, "▼", centerX, stripY - 7, 0xFFFFE555);

        // Clickable mini number grid (1..36) display & current bet summary
        int infoY = top + 124;
        g.fill(left + 12, infoY, left + PANEL_W - 12, infoY + 50, 0x880B1A12);
        CasinoGuiHelper.drawBorder(g, left + 12, infoY, PANEL_W - 24, 50, CasinoGuiHelper.COLOR_GOLD_DARK);

        String betName = switch (selectedBetType) {
            case RouletteSpinRequestPacket.BET_RED -> Component.translatable("gui.skycasino.rl.red").getString() + " (×2)";
            case RouletteSpinRequestPacket.BET_BLACK -> Component.translatable("gui.skycasino.rl.black").getString() + " (×2)";
            case RouletteSpinRequestPacket.BET_EVEN -> Component.translatable("gui.skycasino.rl.even").getString() + " (×2)";
            case RouletteSpinRequestPacket.BET_ODD -> Component.translatable("gui.skycasino.rl.odd").getString() + " (×2)";
            case RouletteSpinRequestPacket.BET_LOW -> "1-18 (×2)";
            case RouletteSpinRequestPacket.BET_HIGH -> "19-36 (×2)";
            case RouletteSpinRequestPacket.BET_DOZEN1 -> "1-12 (×3)";
            case RouletteSpinRequestPacket.BET_DOZEN2 -> "13-24 (×3)";
            case RouletteSpinRequestPacket.BET_DOZEN3 -> "25-36 (×3)";
            default -> "№ " + selectedNumber + " (×36)";
        };
        g.drawString(this.font, Component.translatable("gui.skycasino.rl.current_bet", betName).getString(),
                left + 20, infoY + 8, 0xFFFFD15C, true);

        // History badges
        g.drawString(this.font, Component.translatable("gui.skycasino.rl.history"), left + 20, infoY + 26, 0xFFBBBBCC, false);
        int hx = left + 82;
        for (int hNum : history) {
            g.fill(hx, infoY + 23, hx + 20, infoY + 38, colorForNumber(hNum));
            CasinoGuiHelper.drawBorder(g, hx, infoY + 23, 20, 15, 0x88FFD15C);
            g.drawCenteredString(this.font, String.valueOf(hNum), hx + 10, infoY + 27, 0xFFFFFFFF);
            hx += 23;
        }

        // Result text
        if (!spinning && lastWinningNumber >= 0) {
            if (lastPayout > 0) {
                g.drawString(this.font, Component.translatable("gui.skycasino.won_chips", ChipEconomy.formatChips(lastPayout)),
                        left + 228, top + 186, 0xFF66FF88, true);
            } else {
                g.drawString(this.font, Component.translatable("gui.skycasino.rl.landed", lastWinningNumber),
                        left + 228, top + 186, 0xFFFF8888, true);
            }
        }

        // Bet highlight
        int selBx = left + 12 + betIndex * 42;
        CasinoGuiHelper.drawBorder(g, selBx - 1, top + 181, 40, 20, CasinoGuiHelper.COLOR_GOLD);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
