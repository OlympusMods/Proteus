package com.github.olympus.proteus.client.svc;

import de.maxhenkel.voicechat.gui.widgets.MicTestButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class Svc {
    private Svc() {
    }

    static String text(String key, Object... arguments) {
        return Component.translatable(key, arguments).getString();
    }

    static void open(Screen screen) {
        Minecraft.getInstance().setScreen(screen);
    }

    static void press(MicTestButton button) {
        button.onPress(null);
    }

    static Screen current() {
        return Minecraft.getInstance().screen;
    }
}
