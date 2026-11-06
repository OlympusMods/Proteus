package com.github.olympus.proteus.client.ui;

import net.minecraft.network.chat.Component;

final class EffectsOptions {
    private static final int ROW = 20;
    private static final int GAP = 6;

    private record Option(String key, String name, boolean fallback) {
    }

    private static final Option[] SWITCHES = {
        new Option(EffectsOverlay.HUD, "hud", true),
        new Option(EffectsOverlay.HUD_TIME, "hud_time", false),
        new Option(EffectsOverlay.INVENTORY, "inventory", true),
        new Option(EffectsOverlay.COMPACT, "compact", false)
    };

    private EffectsOptions() {
    }

    static int render(SurfacePanel panel, int y) {
        Gui gui = panel.gui();
        UiPrefs prefs = panel.prefs();
        Theme theme = panel.theme();
        gui.text(Component.translatable("proteus.effects.heading").getString(), panel.x() + 2, y + 1,
                theme.textSecondary());
        y += 13;
        int cardH = 8 + SWITCHES.length * (ROW + 6) - 6 + 8;
        theme.card(panel.fade(), panel.x(), y, panel.width(), cardH);
        int rowY = y + 8;
        for (Option entry : SWITCHES) {
            boolean value = prefs.flag(entry.key(), entry.fallback());
            String base = "proteus.effects." + entry.name();
            if (gui.switchRow("effects-" + entry.name(), panel.x() + 12, rowY, panel.width() - 24, ROW,
                    Component.translatable(base).getString(), value, true,
                    Component.translatable(base + ".tooltip").getString(),
                    Component.translatable("proteus.state.on").getString(),
                    Component.translatable("proteus.state.off").getString())) {
                prefs.setFlag(entry.key(), !value);
            }
            rowY += ROW + 6;
        }
        return y + cardH + GAP;
    }
}
