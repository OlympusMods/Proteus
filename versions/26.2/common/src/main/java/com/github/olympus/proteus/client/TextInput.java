package com.github.olympus.proteus.client;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.minecraft.client.Minecraft;

final class TextInput {
    private static final MethodHandle MANAGER = manager();
    private static final MethodHandle START = managerMethod("startTextInput");
    private static final MethodHandle STOP = managerMethod("stopTextInput");

    private final Object owner;
    private boolean active;

    TextInput(Object owner) {
        this.owner = owner;
    }

    void sync(boolean wanted) {
        if (wanted == active || START == null || STOP == null) {
            return;
        }
        active = wanted;
        invoke(wanted ? START : STOP);
    }

    void release() {
        sync(false);
    }

    private void invoke(MethodHandle handle) {
        try {
            handle.invoke(MANAGER.invoke(Minecraft.getInstance()), owner);
        } catch (Throwable throwable) {
            ProteusClient.LOGGER.warn("Proteus: could not change text input state", throwable);
        }
    }

    private static MethodHandle manager() {
        try {
            return MethodHandles.publicLookup().findVirtual(Minecraft.class, "textInputManager",
                    MethodType.methodType(managerType()));
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    private static Class<?> managerType() throws ClassNotFoundException {
        return Class.forName("com.mojang.blaze3d.platform.TextInputManager");
    }

    private static MethodHandle managerMethod(String name) {
        if (MANAGER == null) {
            return null;
        }
        try {
            return MethodHandles.publicLookup().findVirtual(managerType(), name,
                    MethodType.methodType(void.class, Object.class));
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}
