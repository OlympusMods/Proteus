package com.github.olympus.proteus;

import com.github.olympus.proteus.client.ProteusClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public final class ProteusFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(ProteusClient.openKey());
    }
}
