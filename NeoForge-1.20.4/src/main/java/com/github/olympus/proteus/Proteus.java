package com.github.olympus.proteus;

import com.github.olympus.proteus.client.ProteusClient;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(Proteus.MOD_ID)
public class Proteus {
    public static final String MOD_ID = "proteus";

    public Proteus(IEventBus modBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(Proteus::onRegisterKeys);
        }
    }

    private static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        ProteusClient.configure(FMLPaths.CONFIGDIR.get());
        for (KeyMapping key : ProteusClient.keys().all()) {
            event.register(key);
        }
    }
}
