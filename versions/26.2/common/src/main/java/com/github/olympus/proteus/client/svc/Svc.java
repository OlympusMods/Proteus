package com.github.olympus.proteus.client.svc;

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
        Minecraft.getInstance().gui.setScreen(screen);
    }
}
