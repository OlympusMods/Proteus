package com.github.olympus.proteus.client.ui;

import java.util.Locale;

public enum ToastKind {
    ADVANCEMENTS,
    RECIPES,
    TUTORIAL,
    SYSTEM;

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }
}
