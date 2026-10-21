package com.github.olympus.proteus;

import com.github.olympus.proteus.client.ProteusClient;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

public final class ForgeClient {
    private ForgeClient() {
    }

    static void register() {
        RegisterKeyMappingsEvent.getBus(FMLJavaModLoadingContext.get().getModBusGroup()).addListener(ForgeClient::onRegisterKeys);
    }

    private static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        ProteusClient.configure(FMLPaths.CONFIGDIR.get());
        for (KeyMapping key : ProteusClient.keys().all()) {
            event.register(key);
        }
    }
}
