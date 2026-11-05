package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import fi.dy.masa.malilib.gui.widgets.WidgetSlider;
import fi.dy.masa.malilib.render.GuiContext;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WidgetSlider.class, remap = false)
public abstract class WidgetSliderMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/GuiContext;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
    private void proteus$track(GuiContext ctx, RenderPipeline pipeline, Identifier sprite, int x, int y, int w, int h) {
        if (!MalilibSkin.inset(ctx, x, y, w, h)) {
            ctx.blitSprite(pipeline, sprite, x, y, w, h);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/GuiContext;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 1))
    private void proteus$thumb(GuiContext ctx, RenderPipeline pipeline, Identifier sprite, int x, int y, int w, int h) {
        if (!MalilibSkin.button(ctx, x, y, w, h, false, true)) {
            ctx.blitSprite(pipeline, sprite, x, y, w, h);
        }
    }
}
