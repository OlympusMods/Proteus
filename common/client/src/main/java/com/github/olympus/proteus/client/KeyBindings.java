package com.github.olympus.proteus.client;

import com.github.olympus.proteus.core.PressCounter;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class KeyBindings {
    private final KeyMapping action;
    private final PressCounter counter = new PressCounter();

    public KeyBindings(KeyMapping action) {
        this.action = action;
    }

    public List<KeyMapping> all() {
        return List.of(action);
    }

    public void poll(Minecraft minecraft) {
        while (action.consumeClick()) {
            Notifier.announce(minecraft, Component.translatable("proteus.message.pressed", counter.press()));
        }
    }
}
