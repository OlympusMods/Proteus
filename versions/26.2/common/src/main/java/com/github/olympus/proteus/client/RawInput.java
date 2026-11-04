package com.github.olympus.proteus.client;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public interface RawInput {
    default boolean rawKey(KeyEvent event) {
        return false;
    }

    default boolean rawMouse(MouseButtonEvent event) {
        return false;
    }
}
