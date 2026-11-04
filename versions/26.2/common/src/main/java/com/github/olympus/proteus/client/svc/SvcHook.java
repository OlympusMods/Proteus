package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.Mods;
import net.minecraft.client.gui.screens.Screen;

public final class SvcHook {
    private SvcHook() {
    }

    public static Screen replace(Screen screen) {
        if (screen == null || !screen.getClass().getName().startsWith("de.maxhenkel.voicechat.")
                || !Mods.active(Mods.VOICECHAT)) {
            return screen;
        }
        return SvcScreens.replace(screen);
    }
}
