package com.github.olympus.proteus.client.ji;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class Ji {
    private Ji() {
    }

    static String text(String key, Object... arguments) {
        return ChatFormatting.stripFormatting(Component.translatable("config.ji_afkcinematic." + key, arguments).getString());
    }

    static String tip(String key) {
        return text("tooltip." + key);
    }

    static String proteus(String key) {
        return Component.translatable(key).getString();
    }

    static void open(Screen screen) {
        Minecraft.getInstance().gui.setScreen(screen);
    }

    static Screen current() {
        return Minecraft.getInstance().gui.screen();
    }
}
