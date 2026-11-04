package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.ScreenTakeover;
import net.minecraft.client.gui.screens.Screen;

public final class SvcTakeover implements ScreenTakeover {
    @Override
    public Screen replace(Screen screen) {
        if (!screen.getClass().getName().startsWith("de.maxhenkel.voicechat.")) {
            return screen;
        }
        return SvcScreens.replace(screen);
    }
}
