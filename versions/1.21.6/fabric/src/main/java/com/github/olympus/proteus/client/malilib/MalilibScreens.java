package com.github.olympus.proteus.client.malilib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

final class MalilibScreens {
    private MalilibScreens() {
    }

    static Screen current() {
        return Minecraft.getInstance().screen;
    }
}
