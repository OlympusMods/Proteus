package com.github.olympus.proteus.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class KeyFactory {
    private static final String CATEGORY = "key.categories.proteus";

    private KeyFactory() {
    }

    public static KeyBindings create() {
        return new KeyBindings(key("action", GLFW.GLFW_KEY_K));
    }

    private static KeyMapping key(String name, int code) {
        return new KeyMapping("key.proteus." + name, InputConstants.Type.KEYSYM, code, CATEGORY);
    }
}
