package com.github.olympus.proteus.client;

import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProteusClient {
    public static final String MOD_ID = "proteus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static Path configDirectory = Path.of("config");
    private static KeyBindings keys;

    private ProteusClient() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void configure(Path directory) {
        configDirectory = directory;
    }

    public static Path configDirectory() {
        return configDirectory;
    }

    public static synchronized KeyBindings keys() {
        if (keys == null) {
            keys = KeyFactory.create(KeyFactory.registerCategory());
        }
        return keys;
    }

    public static synchronized void installKeys(KeyBindings bindings) {
        keys = bindings;
    }

    /** Runs at the start of every client tick, called from ClientTickMixin. */
    public static void tick(Minecraft minecraft) {
        keys().poll(minecraft);
    }
}
