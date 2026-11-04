package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.GlassRenderer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {
    @Inject(method = "bindDefaultUniforms", at = @At("TAIL"))
    private static void proteus$bindGlass(RenderPass pass, CallbackInfo callback) {
        GlassRenderer.bindUniforms(pass);
    }
}
