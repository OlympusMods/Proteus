package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import fi.dy.masa.malilib.gui.widgets.WidgetSlider;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.client.gui.GuiGraphics;

@Mixin(value = WidgetSlider.class, remap = false)
public abstract class WidgetSliderMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_52706(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIII)V", ordinal = 0))
    private void proteus$track(GuiGraphics ctx, RenderPipeline pipeline, ResourceLocation sprite, int x, int y, int w, int h) {
        if (!MalilibSkin.inset(ctx, x, y, w, h)) {
            ctx.blitSprite(pipeline, sprite, x, y, w, h);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_52706(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIII)V", ordinal = 1))
    private void proteus$thumb(GuiGraphics ctx, RenderPipeline pipeline, ResourceLocation sprite, int x, int y, int w, int h) {
        if (!MalilibSkin.button(ctx, x, y, w, h, false, true)) {
            ctx.blitSprite(pipeline, sprite, x, y, w, h);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 0))
    private void proteus$trackNamed(GuiGraphics ctx, RenderPipeline pipeline, ResourceLocation sprite, int x, int y, int w, int h) {
        proteus$track(ctx, pipeline, sprite, x, y, w, h);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 1))
    private void proteus$thumbNamed(GuiGraphics ctx, RenderPipeline pipeline, ResourceLocation sprite, int x, int y, int w, int h) {
        proteus$thumb(ctx, pipeline, sprite, x, y, w, h);
    }
}
