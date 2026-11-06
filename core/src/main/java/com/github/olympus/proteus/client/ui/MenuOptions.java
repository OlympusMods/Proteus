package com.github.olympus.proteus.client.ui;

import net.minecraft.network.chat.Component;

final class MenuOptions {
    private static final int ROW = 20;
    private static final int GAP = 6;
    private static final String[] FLAGS = {Menus.TITLE_FLAG, Menus.PAUSE_FLAG, Menus.SCREENS_FLAG};
    private static final String[] KEYS = {"title", "pause", "screens"};

    private MenuOptions() {
    }

    static int render(SurfacePanel panel, int y) {
        Gui gui = panel.gui();
        Theme theme = panel.theme();
        int ix = panel.x();
        int lw = panel.width();

        gui.text(text("proteus.menus.heading"), ix + 2, y + 1, theme.textSecondary());
        y += 13;
        for (String line : Draw.wrap(panel.fade(), text("proteus.menus.note"), lw - 8)) {
            gui.text(line, ix + 4, y, theme.textSecondary());
            y += 11;
        }
        y += 4;

        int cardH = 8 + FLAGS.length * (ROW + 6) - 6 + 8;
        theme.card(panel.fade(), ix, y, lw, cardH);
        int rowY = y + 8;
        for (int i = 0; i < FLAGS.length; i++) {
            boolean on = panel.prefs().flag(FLAGS[i], true);
            String base = "proteus.menus." + KEYS[i];
            if (gui.switchRow("menus-" + KEYS[i], ix + 12, rowY, lw - 24, ROW, text(base), on, true,
                    text(base + ".tooltip"), text("proteus.state.on"), text("proteus.state.off"))) {
                panel.prefs().setFlag(FLAGS[i], !on);
            }
            rowY += ROW + 6;
        }
        return y + cardH + GAP;
    }

    private static String text(String key) {
        return Component.translatable(key).getString();
    }
}
