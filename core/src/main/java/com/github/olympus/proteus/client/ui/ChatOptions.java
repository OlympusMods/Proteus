package com.github.olympus.proteus.client.ui;

import net.minecraft.network.chat.Component;

final class ChatOptions {
    private static final int ROW = 20;
    private static final int GAP = 6;

    private ChatOptions() {
    }

    static int render(SurfacePanel panel, int y) {
        Gui gui = panel.gui();
        Theme theme = panel.theme();
        UiPrefs prefs = panel.prefs();
        int ix = panel.x();
        int lw = panel.width();

        gui.text(text("proteus.chat.heading"), ix + 2, y + 1, theme.textSecondary());
        y += 13;
        for (String line : Draw.wrap(panel.fade(), text("proteus.chat.note"), lw - 8)) {
            gui.text(line, ix + 4, y, theme.textSecondary());
            y += 11;
        }
        y += 4;

        int cardH = 8 + 4 * (ROW + 6) - 6 + 8;
        theme.card(panel.fade(), ix, y, lw, cardH);
        int rowY = y + 8;

        String tip = text("proteus.chat.opacity.tooltip");
        String[] choices = {text("proteus.chat.opacity.theme"), text("proteus.chat.opacity.game")};
        int choiceW = Math.min(150, lw / 2);
        gui.text(gui.fit(text("proteus.chat.opacity"), lw - choiceW - 28), ix + 12, gui.textY(rowY, ROW),
                theme.textPrimary());
        if (gui.over(ix, rowY, lw - choiceW - 16, ROW)) {
            gui.tooltip(tip);
        }
        int picked = gui.segmented("chat-opacity", ix + lw - 10 - choiceW, rowY, choiceW, ROW, choices,
                prefs.option(ChatSkin.OPACITY, 0), new String[] {tip, tip}, null);
        if (picked >= 0) {
            prefs.setOption(ChatSkin.OPACITY, picked);
        }
        rowY += ROW + 6;

        boolean animate = prefs.flag(ChatSkin.ANIMATE, true);
        if (gui.switchRow("chat-animate", ix + 12, rowY, lw - 24, ROW, text("proteus.chat.animate"), animate, true,
                text("proteus.chat.animate.tooltip"), text("proteus.state.on"), text("proteus.state.off"))) {
            prefs.setFlag(ChatSkin.ANIMATE, !animate);
        }
        rowY += ROW + 6;

        boolean oneBox = prefs.flag(ChatSkin.PANEL, false);
        if (gui.switchRow("chat-panel", ix + 12, rowY, lw - 24, ROW, text("proteus.chat.panel"), oneBox, true,
                text("proteus.chat.panel.tooltip"), text("proteus.state.on"), text("proteus.state.off"))) {
            prefs.setFlag(ChatSkin.PANEL, !oneBox);
        }
        rowY += ROW + 6;

        boolean compact = prefs.flag(ChatSkin.COMPACT, false);
        if (gui.switchRow("chat-compact", ix + 12, rowY, lw - 24, ROW, text("proteus.chat.compact"), compact, true,
                text("proteus.chat.compact.tooltip"), text("proteus.state.on"), text("proteus.state.off"))) {
            prefs.setFlag(ChatSkin.COMPACT, !compact);
        }
        return y + cardH + GAP;
    }

    private static String text(String key) {
        return Component.translatable(key).getString();
    }
}
