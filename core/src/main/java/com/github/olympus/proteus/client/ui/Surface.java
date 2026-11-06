package com.github.olympus.proteus.client.ui;

import java.util.Locale;

public enum Surface {
    TOASTS(true),
    CHAT(false),
    MENUS(false),
    EFFECTS(false);

    private final boolean defaultOn;

    Surface(boolean defaultOn) {
        this.defaultOn = defaultOn;
    }

    public boolean defaultOn() {
        return defaultOn;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String nameKey() {
        return "proteus.surface." + key();
    }

    public String tooltipKey() {
        return "proteus.surface." + key() + ".tooltip";
    }
}
