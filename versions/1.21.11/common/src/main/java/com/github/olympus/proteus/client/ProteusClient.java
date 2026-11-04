package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.Hub;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProteusClient {
    public static final String MOD_ID = "proteus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyMapping openKey;

    private ProteusClient() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static Path configDirectory() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config");
    }

    public static synchronized KeyMapping openKey() {
        if (openKey == null) {
            openKey = new KeyMapping("proteus.key.open", InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC);
        }
        return openKey;
    }

    public static void tick(Minecraft minecraft) {
        if (openKey == null) {
            return;
        }
        while (openKey.consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(ProteusScreen.open(null, Hub.PROTEUS));
            }
        }
    }
}
