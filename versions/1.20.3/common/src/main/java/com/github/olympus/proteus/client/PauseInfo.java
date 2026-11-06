package com.github.olympus.proteus.client;

import net.minecraft.client.gui.screens.PauseScreen;

public final class PauseInfo {
    private PauseInfo() {
    }

    public static boolean shown(PauseScreen screen) {
        return screen.showsPauseMenu();
    }
}
