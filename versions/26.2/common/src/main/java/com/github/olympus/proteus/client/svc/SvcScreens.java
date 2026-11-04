package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ProteusScreen;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class SvcScreens {
    private SvcScreens() {
    }

    static Screen replace(Screen screen) {
        if (screen instanceof VoiceChatScreen) {
            return new ProteusScreen(Component.translatable("gui.voicechat.voice_chat.title"), parent(),
                    SvcMenuApp::new);
        }
        if (screen instanceof VoiceChatSettingsScreen) {
            return new ProteusScreen(Component.translatable("gui.voicechat.voice_chat_settings.title"), parent(),
                    host -> new SvcSettingsApp(host, screen));
        }
        return screen;
    }

    private static Screen parent() {
        Screen current = Minecraft.getInstance().gui.screen();
        return current instanceof ProteusScreen proteus ? proteus.parent() : current;
    }
}
