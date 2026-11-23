package com.skycasino.client.screen;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.network.BlackjackActionPacket;
import com.skycasino.network.BlackjackStatePacket;
import com.skycasino.network.ModNetwork;
import com.skycasino.network.ServerBlackjackManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class BlackjackScreen extends Screen {
    private static final int PANEL_W = 340;
    private static final int PANEL_H = 232;
    private static final long[] BETS = {10L, 25L, 100L, 500L, 1000L};

    private final BlockPos pos;
    private int left;
    private int top;

    private int betIndex = 1;
    private List<Integer> playerHand = new ArrayList<>();
    private List<Integer> dealerHand = new ArrayList<>();
    private int phase = ServerBlackjackManager.PHASE_IDLE;
    private String statusKey = "place_bet";
    private long activeBet = 0;
    private long lastPayout = 0;

    private Button dealBtn;
    private Button hitBtn;
    private Button standBtn;
    private Button doubleBtn;

    public BlackjackScreen(BlockPos pos) {
        super(Component.translatable("screen.skycasino.blackjack"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_W) / 2;
        this.top = (this.height - PANEL_H) / 2;

        // Bet selector row
        int bx = left + 16;
        int by = top + 174;
        for (int i = 0; i < BETS.length; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(String.valueOf(BETS[i])), b -> {
                if (phase != ServerBlackjackManager.PHASE_PLAYER_TURN) {
                    betIndex = idx;
                }
            }).bounds(bx + i * 42, by, 38, 18).build());
        }

        // Action buttons
        this.dealBtn = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.bj.deal"),
                b -> ModNetwork.sendToServer(new BlackjackActionPacket(BlackjackActionPacket.ACTION_DEAL, BETS[betIndex], pos)))
                .bounds(left + 16, top + 198, 52, 22).build());

        addRenderableWidget(Button.builder(Component.literal("§c§lALL-IN"),
                b -> {
                    long bal = Math.min(CasinoGuiHelper.getClientBalance(), 10_000_000L);
                    if (bal > 0 && phase != ServerBlackjackManager.PHASE_PLAYER_TURN) {
                        ModNetwork.sendToServer(new BlackjackActionPacket(BlackjackActionPacket.ACTION_DEAL, bal, pos));
                    }
                })
                .bounds(left + 72, top + 198, 48, 22).build());

        this.hitBtn = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.bj.hit"),
                b -> ModNetwork.sendToServer(new BlackjackActionPacket(BlackjackActionPacket.ACTION_HIT, 0, pos)))
                .bounds(left + 124, top + 198, 48, 22).build());

        this.standBtn = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.bj.stand"),
                b -> ModNetwork.sendToServer(new BlackjackActionPacket(BlackjackActionPacket.ACTION_STAND, 0, pos)))
                .bounds(left + 176, top + 198, 56, 22).build());

        this.doubleBtn = addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.bj.double"),
                b -> ModNetwork.sendToServer(new BlackjackActionPacket(BlackjackActionPacket.ACTION_DOUBLE, 0, pos)))
                .bounds(left + 236, top + 198, 44, 22).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.skycasino.close"), b -> this.onClose())
                .bounds(left + PANEL_W - 52, top + 198, 40, 22).build());

        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean playing = (phase == ServerBlackjackManager.PHASE_PLAYER_TURN);
        long bal = CasinoGuiHelper.getClientBalance();
        if (dealBtn != null) dealBtn.active = !playing && bal >= BETS[betIndex];
        if (hitBtn != null) hitBtn.active = playing;
        if (standBtn != null) standBtn.active = playing;
        if (doubleBtn != null) doubleBtn.active = playing && playerHand.size() == 2 && bal >= activeBet;
    }

    public void onStateUpdate(BlackjackStatePacket packet) {
        this.playerHand = packet.playerHand;
        this.dealerHand = packet.dealerHand;
        this.phase = packet.phase;
        this.statusKey = packet.statusKey;
        this.activeBet = packet.currentBet;
        this.lastPayout = packet.payout;
        updateButtonStates();
    }

    private int visibleHandValue(List<Integer> hand) {
        List<Integer> visible = new ArrayList<>();
        for (int c : hand) {
            if (c >= 0) visible.add(c);
        }
        return ServerBlackjackManager.handValue(visible);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        updateButtonStates();

        long balance = CasinoGuiHelper.getClientBalance();
        CasinoGuiHelper.drawLuxuryPanel(g, left, top, PANEL_W, PANEL_H, true);
        CasinoGuiHelper.drawHeader(g, this.font, left, top, PANEL_W, this.title, balance);

        // Dealer section
        String dealerVal = dealerHand.isEmpty() ? "" : " (" + visibleHandValue(dealerHand) + ")";
        g.drawString(this.font, Component.translatable("gui.skycasino.bj.dealer").getString() + dealerVal,
                left + 18, top + 34, 0xFFFFD15C, true);

        int dx = left + 22;
        for (int c : dealerHand) {
            CasinoGuiHelper.drawPlayingCard(g, this.font, dx, top + 46, c);
            dx += 42;
        }

        // Player section
        String playerVal = playerHand.isEmpty() ? "" : " (" + visibleHandValue(playerHand) + ")";
        g.drawString(this.font, Component.translatable("gui.skycasino.bj.player").getString() + playerVal,
                left + 18, top + 104, 0xFF88FFAA, true);

        int px = left + 22;
        for (int c : playerHand) {
            CasinoGuiHelper.drawPlayingCard(g, this.font, px, top + 116, c);
            px += 42;
        }

        // Status banner on the right
        int statusX = left + 235;
        int statusY = top + 174;
        Component statusComp = switch (statusKey) {
            case "blackjack" -> Component.translatable("gui.skycasino.bj.status.blackjack", ChipEconomy.formatChips(lastPayout));
            case "win", "dealer_bust" -> Component.translatable("gui.skycasino.bj.status.win", ChipEconomy.formatChips(lastPayout));
            case "push" -> Component.translatable("gui.skycasino.bj.status.push");
            case "bust" -> Component.translatable("gui.skycasino.bj.status.bust");
            case "lose" -> Component.translatable("gui.skycasino.bj.status.lose");
            case "your_turn" -> Component.translatable("gui.skycasino.bj.status.turn");
            default -> Component.translatable("gui.skycasino.bj.status.ready");
        };
        int statusColor = switch (statusKey) {
            case "blackjack" -> 0xFFFFE555;
            case "win", "dealer_bust" -> 0xFF66FF88;
            case "push" -> 0xFFAACCFF;
            case "bust", "lose" -> 0xFFFF6666;
            default -> 0xFFDDDDDD;
        };
        g.drawCenteredString(this.font, statusComp, statusX + 46, statusY + 5, statusColor);

        // Selected bet border
        int selBx = left + 16 + betIndex * 42;
        CasinoGuiHelper.drawBorder(g, selBx - 1, top + 173, 40, 20, CasinoGuiHelper.COLOR_GOLD);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
