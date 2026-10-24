package com.github.olympus.proteus;

import com.github.olympus.proteus.client.ProteusClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public final class ProteusFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(ProteusClient.openKey());
    }
}
