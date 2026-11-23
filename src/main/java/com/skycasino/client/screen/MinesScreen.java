package com.skycasino.client.screen;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.network.MinesActionPacket;
import com.skycasino.network.MinesStatePacket;
import com.skycasino.network.ModNetwork;
import com.skycasino.network.ServerMinesManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;

public class MinesScreen extends Screen {
    private static final int PANEL_W = 340;
    private static final int PANEL_H = 232;
    private static final long[] BETS = {25L, 100L, 500L, 2500L};
    private static final int[] MINE_OPTIONS = {3, 5, 10, 15};

    private final BlockPos pos;
    private int left;
    private int top;

    private int betIndex = 1;
    private int mineOptIdx = 1; // default 5 mines

    private int phase = ServerMinesManager.PHASE_IDLE;
    private int revealedMask = 0;
    private int mineMask = 0;
    private double multiplier = 1.0;
    private double nextMultiplier = 1.0;
    private long activeBet = 0;
    private long lastPayout = 0;

    private Button startBtn;
    private Button cashoutBtn;

    public MinesScreen(BlockPos pos) {
        super(Component.translatable("screen.skycasino.mines"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        // 5x5 Tile Grid on the left
        int gx = left + 16;
        int gy = top + 36;
        int tileSize = 32;
        int gap = 3;

        for (int i = 0; i < 25; i++) {
            final int idx = i;
            int r = i / 5;
            int c = i % 5;
            addRenderableWidget(Button.builder(Component.empty(), b -> {
                if (phase == ServerMinesManager.PHASE_PLAYING && (revealedMask & (1 << idx)) == 0) {
                    ModNetwork.sendToServer(new MinesActionPacket(MinesActionPacket.ACTION_CLICK, idx, 0, pos));
                }
            }).bounds(gx + c * (tileSize + gap), gy + r * (tileSize + gap), tileSize, tileSize).build());
        }

        // Right side controls
        int rx = left + 198;

        // Bet presets
        for (int i = 0; i < BETS.length; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(String.valueOf(BETS[i])), b -> {
                if (phase != ServerMinesManager.PHASE_PLAYING) betIndex = idx;
            }).bounds(rx + (i % 2) * 64, top + 48 + (i / 2) * 20, 60, 18).build());
        }

        // Mine count presets
        for (int i = 0; i < MINE_OPTIONS.length; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(MINE_OPTIONS[i] + " 💣"), b -> {
                if (phase != ServerMinesManager.PHASE_PLAYING) mineOptIdx = idx;
            }).bounds(rx + i * 32, top + 104, 30, 18).build());
        }

        this.startBtn = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.mines.start"),
                b -> ModNetwork.sendToServer(new MinesActionPacket(MinesActionPacket.ACTION_START, MINE_OPTIONS[mineOptIdx], BETS[betIndex], pos)))
                .bounds(rx, top + 164, 126, 20).build());

        this.cashoutBtn = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.crash.cashout"),
                b -> ModNetwork.sendToServer(new MinesActionPacket(MinesActionPacket.ACTION_CASHOUT, 0, 0, pos)))
                .bounds(rx, top + 188, 126, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.close"), b -> this.onClose())
                .bounds(left + 16, top + PANEL_H - 22, 64, 16).build());

        updateButtons();
    }

    private void updateButtons() {
        boolean playing = (phase == ServerMinesManager.PHASE_PLAYING);
        if (startBtn != null) startBtn.active = !playing && CasinoGuiHelper.getClientBalance() >= BETS[betIndex];
        if (cashoutBtn != null) cashoutBtn.active = playing && revealedMask != 0;
    }

    public void onStateUpdate(MinesStatePacket packet) {
        this.phase = packet.phase;
        this.revealedMask = packet.revealedMask;
        this.mineMask = packet.mineMask;
        this.multiplier = packet.multiplier;
        this.nextMultiplier = packet.nextMultiplier;
        this.activeBet = packet.bet;
        this.lastPayout = packet.payout;
        updateButtons();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        updateButtons();

        long balance = CasinoGuiHelper.getClientBalance();
        CasinoGuiHelper.drawLuxuryPanel(g, left, top, PANEL_W, PANEL_H, false);
        CasinoGuiHelper.drawHeader(g, this.font, left, top, PANEL_W, this.title, balance);

        int rx = left + 198;
        g.drawString(this.font, Component.translatable("gui.skycasino.mines.bet"), rx, top + 36, 0xFFBBBBCC, false);
        g.drawString(this.font, Component.translatable("gui.skycasino.mines.count"), rx, top + 92, 0xFFBBBBCC, false);

        // Multiplier info box
        g.fill(rx, top + 128, rx + 126, top + 158, 0xAA0E1018);
        CasinoGuiHelper.drawBorder(g, rx, top + 128, 126, 30, CasinoGuiHelper.COLOR_GOLD_DARK);

        if (phase == ServerMinesManager.PHASE_PLAYING) {
            g.drawString(this.font, String.format(Locale.US, "×%.2f  →  ×%.2f", multiplier, nextMultiplier),
                    rx + 8, top + 133, 0xFFFFE566, true);
            long pot = Math.round(activeBet * multiplier);
            g.drawString(this.font, "+ " + ChipEconomy.formatChips(pot), rx + 8, top + 145, 0xFF66FF88, false);
        } else if (phase == ServerMinesManager.PHASE_WON) {
            g.drawCenteredString(this.font, Component.translatable("gui.skycasino.won_chips", ChipEconomy.formatChips(lastPayout)),
                    rx + 63, top + 139, 0xFF66FF88);
        } else if (phase == ServerMinesManager.PHASE_LOST) {
            g.drawCenteredString(this.font, Component.translatable("gui.skycasino.mines.boom"),
                    rx + 63, top + 139, 0xFFFF5555);
        } else {
            g.drawCenteredString(this.font, Component.translatable("gui.skycasino.bj.status.ready"),
                    rx + 63, top + 139, 0xFFCCCCCC);
        }

        // Highlight selected bet & mine count
        int selBx = rx + (betIndex % 2) * 64;
        int selBy = top + 48 + (betIndex / 2) * 20;
        CasinoGuiHelper.drawBorder(g, selBx - 1, selBy - 1, 62, 20, CasinoGuiHelper.COLOR_GOLD);
        CasinoGuiHelper.drawBorder(g, rx + mineOptIdx * 32 - 1, top + 103, 32, 20, CasinoGuiHelper.COLOR_GOLD);

        super.render(g, mouseX, mouseY, partialTick);

        // Overlay Diamonds & TNT Mines on top of the 5x5 tile buttons
        int gx = left + 16;
        int gy = top + 36;
        int tileSize = 32;
        int gap = 3;

        for (int i = 0; i < 25; i++) {
            int r = i / 5;
            int c = i % 5;
            int tx = gx + c * (tileSize + gap);
            int ty = gy + r * (tileSize + gap);

            boolean isRevealed = (revealedMask & (1 << i)) != 0;
            boolean isMine = (mineMask & (1 << i)) != 0;

            if (isRevealed && isMine) {
                g.fill(tx + 1, ty + 1, tx + tileSize - 1, ty + tileSize - 1, 0xDDAA1824);
                g.renderItem(new ItemStack(Items.TNT), tx + 8, ty + 8);
            } else if (isRevealed) {
                g.fill(tx + 1, ty + 1, tx + tileSize - 1, ty + tileSize - 1, 0xDD115E3C);
                g.renderItem(new ItemStack(Items.DIAMOND), tx + 8, ty + 8);
            } else if (isMine && phase != ServerMinesManager.PHASE_PLAYING) {
                g.fill(tx + 1, ty + 1, tx + tileSize - 1, ty + tileSize - 1, 0x8855181C);
                g.renderItem(new ItemStack(Items.TNT), tx + 8, ty + 8);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
