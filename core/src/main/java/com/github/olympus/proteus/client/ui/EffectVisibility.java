package com.github.olympus.proteus.client.ui;

import java.lang.reflect.Method;
import net.minecraft.world.effect.MobEffectInstance;

public final class EffectVisibility {
    private static final String[] PACKAGES = {
        "net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions",
        "net.minecraftforge.client.extensions.common.IClientMobEffectExtensions"
    };

    private static final Method OF;
    private static final Method GUI;
    private static final Method INVENTORY;

    static {
        Method of = null;
        Method gui = null;
        Method inventory = null;
        for (String name : PACKAGES) {
            try {
                Class<?> type = Class.forName(name);
                of = type.getMethod("of", MobEffectInstance.class);
                gui = type.getMethod("isVisibleInGui", MobEffectInstance.class);
                inventory = type.getMethod("isVisibleInInventory", MobEffectInstance.class);
                break;
            } catch (ReflectiveOperationException | LinkageError exception) {
                of = null;
                gui = null;
                inventory = null;
            }
        }
        OF = of;
        GUI = gui;
        INVENTORY = inventory;
    }

    private EffectVisibility() {
    }

    public static boolean gui(MobEffectInstance instance) {
        return ask(GUI, instance);
    }

    public static boolean inventory(MobEffectInstance instance) {
        return ask(INVENTORY, instance);
    }

    private static boolean ask(Method method, MobEffectInstance instance) {
        if (OF == null || method == null) {
            return true;
        }
        try {
            Object extensions = OF.invoke(null, instance);
            return !(method.invoke(extensions, instance) instanceof Boolean visible) || visible;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return true;
        }
    }
}
