package com.github.olympus.proteus.client;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.SpriteIconButton;

public final class ButtonShape {
    private ButtonShape() {
    }

    public static boolean labelled(AbstractButton button) {
        return !(button instanceof SpriteIconButton);
    }
}
