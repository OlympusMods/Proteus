package com.github.olympus.proteus;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(Proteus.MOD_ID)
public class Proteus {
    public static final String MOD_ID = "proteus";

    public Proteus() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeClient.registerConfigScreen();
        }
    }
}
