package com.github.olympus.proteus;

import com.github.olympus.proteus.client.ProteusClient;
import com.github.olympus.proteus.client.ProteusScreen;
import com.github.olympus.proteus.client.ui.Hub;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(Proteus.MOD_ID)
public class Proteus {
    public static final String MOD_ID = "proteus";

    public Proteus(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(Proteus::onRegisterKeys);
            container.registerExtensionPoint(IConfigScreenFactory.class,
                    (IConfigScreenFactory) (minecraft, parent) -> ProteusScreen.open(parent, Hub.PROTEUS));
        }
    }

    private static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(ProteusClient.openKey());
    }
}
