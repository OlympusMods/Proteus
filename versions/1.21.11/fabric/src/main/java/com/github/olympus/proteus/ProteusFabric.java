package com.github.olympus.proteus;

import com.github.olympus.proteus.client.ProteusClient;
import com.github.olympus.proteus.client.ThemeReloader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class ProteusFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(ProteusClient.openKey());
        ThemeReloader themes = new ThemeReloader();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return ProteusClient.id("themes");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager manager) {
                        themes.onResourceManagerReload(manager);
                    }
                });
    }
}
