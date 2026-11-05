package com.github.olympus.proteus.client.ji;

import com.github.olympus.proteus.client.ui.ScreenTakeover;
import com.ji.afkcinematic.config.ConfigScreen;
import net.minecraft.client.gui.screens.Screen;

public final class JiTakeover implements ScreenTakeover {
    @Override
    public Screen replace(Screen screen) {
        if (screen instanceof JiConfigScreen || !(screen instanceof ConfigScreen)) {
            return screen;
        }
        return new JiConfigScreen(Ji.current());
    }
}
