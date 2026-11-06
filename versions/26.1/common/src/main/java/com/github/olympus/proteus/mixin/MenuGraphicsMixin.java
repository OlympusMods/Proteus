package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class MenuGraphicsMixin {
    @Inject(method = "blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V",
            at = @At("HEAD"), cancellable = true)
    private void proteus$texture(RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int w,
            int h, int textureW, int textureH, CallbackInfo callback) {
        if (texture.getNamespace().equals("minecraft") && MenuHud.texture(texture.getPath(), x, y, w, h)) {
            callback.cancel();
        }
    }
}
