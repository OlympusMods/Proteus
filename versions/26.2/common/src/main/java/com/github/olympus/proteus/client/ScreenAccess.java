package com.github.olympus.proteus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

final class ScreenAccess {
    private ScreenAccess() {
    }

    static Screen current() {
        return Minecraft.getInstance().gui.screen();
    }
}
