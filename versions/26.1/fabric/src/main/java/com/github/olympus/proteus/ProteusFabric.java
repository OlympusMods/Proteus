package com.github.olympus.proteus;

import com.github.olympus.proteus.client.ProteusClient;
import com.github.olympus.proteus.client.ThemeReloader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.PackType;

public final class ProteusFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(ProteusClient.openKey());
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(ProteusClient.id("themes"),
                new ThemeReloader());
    }
}
