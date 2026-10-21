package com.github.olympus.proteus.client;

import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProteusClient {
    public static final String MOD_ID = "proteus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private ProteusClient() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    /** The game's config folder, which is where every loader keeps its mod configs. */
    public static Path configDirectory() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config");
    }
}
