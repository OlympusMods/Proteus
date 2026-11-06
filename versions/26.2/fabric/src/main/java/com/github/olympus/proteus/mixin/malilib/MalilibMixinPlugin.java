package com.github.olympus.proteus.mixin.malilib;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

public final class MalilibMixinPlugin implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return FabricLoader.getInstance().isModLoaded("malilib");
    }
}
