package com.github.olympus.proteus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

final class ChatScreens {
    private ChatScreens() {
    }

    static boolean open() {
        return Minecraft.getInstance().gui.screen() instanceof ChatScreen;
    }
}
