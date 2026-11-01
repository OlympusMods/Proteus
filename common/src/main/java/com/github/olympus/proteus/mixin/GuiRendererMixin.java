package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.GlassRenderer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import java.util.function.Supplier;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Inject(method = "executeDrawRange", at = @At("HEAD"), cancellable = true)
    private void proteus$drawGlass(Supplier<String> label, RenderTarget target, @Coerce Object transforms, int start,
            int end, CallbackInfo callback) {
        if (GlassRenderer.drawRange(this, label, target, transforms, start, end)) {
            callback.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void proteus$endGlassFrame(CallbackInfo callback) {
        GlassRenderer.endFrame();
    }
}
