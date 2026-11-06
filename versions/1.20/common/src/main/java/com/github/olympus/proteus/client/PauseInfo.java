package com.github.olympus.proteus.client;

import com.github.olympus.proteus.mixin.PauseScreenAccess;
import net.minecraft.client.gui.screens.PauseScreen;

public final class PauseInfo {
    private PauseInfo() {
    }

    public static boolean shown(PauseScreen screen) {
        return ((PauseScreenAccess) screen).proteus$showPauseMenu();
    }
}
