package com.github.olympus.proteus;

import com.github.olympus.proteus.client.ProteusClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;

public final class ProteusFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ProteusClient.configure(FabricLoader.getInstance().getConfigDir());
        for (KeyMapping key : ProteusClient.keys().all()) {
            KeyMappingHelper.registerKeyMapping(key);
        }
    }
}
