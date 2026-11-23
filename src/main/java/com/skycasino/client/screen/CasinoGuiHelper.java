package com.skycasino.client.screen;

import com.skycasino.economy.ChipEconomy;
import com.skycasino.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CasinoGuiHelper {
    public static final int COLOR_BG_TOP = 0xF2120814;
    public static final int COLOR_BG_BOTTOM = 0xF2081A14;
    public static final int COLOR_FELT_TOP = 0xFF0E4A2E;
    public static final int COLOR_FELT_BOTTOM = 0xFF072B1A;
    public static final int COLOR_GOLD = 0xFFFFD15C;
    public static final int COLOR_GOLD_DARK = 0xFF9E6B1E;
    public static final int COLOR_PANEL_DARK = 0xCC0D0E14;
    public static final int COLOR_CRIMSON = 0xFF9E1B2A;

    public static void drawLuxuryPanel(GuiGraphics g, int x, int y, int w, int h, boolean feltStyle) {
        // Drop shadow
        g.fill(x + 4, y + 4, x + w + 4, y + h + 4, 0x88000000);
        // Main gradient body
        if (feltStyle) {
            g.fillGradient(x, y, x + w, y + h, COLOR_FELT_TOP, COLOR_FELT_BOTTOM);
        } else {
            g.fillGradient(x, y, x + w, y + h, COLOR_BG_TOP, COLOR_BG_BOTTOM);
        }
        // Double gold border
        drawBorder(g, x, y, w, h, COLOR_GOLD_DARK);
        drawBorder(g, x + 2, y + 2, w - 4, h - 4, COLOR_GOLD);
        // Corner gold studs
        g.fill(x + 4, y + 4, x + 7, y + 7, COLOR_GOLD);
        g.fill(x + w - 7, y + 4, x + w - 4, y + 7, COLOR_GOLD);
        g.fill(x + 4, y + h - 7, x + 7, y + h - 4, COLOR_GOLD);
        g.fill(x + w - 7, y + h - 7, x + w - 4, y + h - 4, COLOR_GOLD);
    }

    public static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    public static void drawHeader(GuiGraphics g, Font font, int panelX, int panelY, int panelW, Component title, long balance) {
        // Header bar
        g.fillGradient(panelX + 3, panelY + 3, panelX + panelW - 3, panelY + 26, 0xEE2B1018, 0xEE18090F);
        g.fill(panelX + 3, panelY + 26, panelX + panelW - 3, panelY + 27, COLOR_GOLD);

        g.drawString(font, title, panelX + 12, panelY + 11, COLOR_GOLD, true);

        // Balance pill on the right
        String balText = ChipEconomy.formatChips(balance);
        int textW = font.width(balText);
        int pillW = textW + 28;
        int pillX = panelX + panelW - pillW - 10;
        int pillY = panelY + 6;
        g.fill(pillX, pillY, pillX + pillW, pillY + 18, 0xCC0A0A0E);
        drawBorder(g, pillX, pillY, pillW, 18, COLOR_GOLD_DARK);
        g.renderItem(new ItemStack(ModItems.GOLD_CHIP.get()), pillX + 2, pillY + 1);
        g.drawString(font, balText, pillX + 21, pillY + 5, 0xFFFFE57F, true);
    }

    public static long getClientBalance() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null ? ChipEconomy.getTotalChips(mc.player) : 0L;
    }

    public static void drawPlayingCard(GuiGraphics g, Font font, int x, int y, int card) {
        int w = 36;
        int h = 50;
        // Shadow
        g.fill(x + 2, y + 2, x + w + 2, y + h + 2, 0x66000000);

        if (card < 0) {
            // Face-down luxury red & gold card back
            g.fill(x, y, x + w, y + h, 0xFFF5F5F0);
            g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xFF8B1528);
            drawBorder(g, x + 4, y + 4, w - 8, h - 8, COLOR_GOLD);
            g.drawCenteredString(font, "★", x + w / 2, y + h / 2 - 4, COLOR_GOLD);
            return;
        }

        // Card front
        g.fill(x, y, x + w, y + h, 0xFFFAF8F2);
        drawBorder(g, x, y, w, h, 0xFF333333);

        int suit = card / 13;
        int rank = card % 13;
        String[] suits = {"♠", "♥", "♦", "♣"};
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        boolean red = (suit == 1 || suit == 2);
        int textColor = red ? 0xFFCC1B2B : 0xFF1A1A24;

        g.drawString(font, ranks[rank], x + 4, y + 4, textColor, false);
        g.drawString(font, suits[suit], x + 4, y + 14, textColor, false);
        g.drawCenteredString(font, suits[suit], x + w / 2, y + h / 2 - 4, textColor);
        g.drawString(font, ranks[rank], x + w - font.width(ranks[rank]) - 3, y + h - 11, textColor, false);
    }
}
