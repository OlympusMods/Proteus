package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import fi.dy.masa.malilib.gui.widgets.WidgetSlider;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WidgetSlider.class, remap = false)
public abstract class WidgetSliderMixin {
    @Shadow
    protected int sliderWidth;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_52706(Ljava/util/function/Function;Lnet/minecraft/class_2960;IIII)V", ordinal = 0))
    private void proteus$track(GuiGraphics graphics, Function<ResourceLocation, RenderType> type, ResourceLocation sprite, int x, int y,
            int w, int h) {
        if (!MalilibSkin.inset(x, y, w, h)) {
            graphics.blitSprite(type, sprite, x, y, w, h);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_52706(Ljava/util/function/Function;Lnet/minecraft/class_2960;IIII)V", ordinal = 1))
    private void proteus$thumb(GuiGraphics graphics, Function<ResourceLocation, RenderType> type, ResourceLocation sprite, int x, int y,
            int w, int h) {
        if (!MalilibSkin.button(x, y, w, h, false, true)) {
            graphics.blitSprite(type, sprite, x, y, w, h);
        }
    }
}
