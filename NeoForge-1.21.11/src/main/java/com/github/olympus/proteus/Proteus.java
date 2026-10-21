package com.github.olympus.proteus;

import com.github.olympus.proteus.client.KeyFactory;
import com.github.olympus.proteus.client.ProteusClient;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(value = Proteus.MOD_ID, dist = Dist.CLIENT)
public class Proteus {
    public static final String MOD_ID = "proteus";

    public Proteus(IEventBus modBus) {
        modBus.addListener(Proteus::onRegisterKeys);
    }

    private static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        ProteusClient.configure(FMLPaths.CONFIGDIR.get());
        KeyMapping.Category category = new KeyMapping.Category(ProteusClient.id("main"));
        event.registerCategory(category);
        ProteusClient.installKeys(KeyFactory.create(category));
        for (KeyMapping key : ProteusClient.keys().all()) {
            event.register(key);
        }
    }
}
