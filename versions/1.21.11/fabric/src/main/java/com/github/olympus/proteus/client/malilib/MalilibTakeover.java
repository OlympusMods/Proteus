package com.github.olympus.proteus.client.malilib;

import com.github.olympus.proteus.client.ui.ScreenTakeover;
import net.minecraft.client.gui.screens.Screen;

public final class MalilibTakeover implements ScreenTakeover {
    @Override
    public Screen replace(Screen screen) {
        return screen;
    }
}
