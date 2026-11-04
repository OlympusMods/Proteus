package com.github.olympus.proteus.client.ui;

public interface RawInput {
    default boolean rawKey(RawKey key) {
        return false;
    }

    default boolean rawMouse(RawMouse mouse) {
        return false;
    }
}
