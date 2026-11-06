package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import fi.dy.masa.malilib.gui.widgets.WidgetSlider;
import fi.dy.masa.malilib.render.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WidgetSlider.class, remap = false)
public abstract class WidgetSliderMixin {
    @Shadow
    protected int sliderWidth;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/RenderUtils;drawTexturedRect(IIIIII)V", ordinal = 0))
    private void proteus$track(int x, int y, int u, int v, int w, int h) {
        if (!MalilibSkin.inset(x, y, ((WidgetSlider) (Object) this).getWidth() - 3, 20)) {
            RenderUtils.drawTexturedRect(x, y, u, v, w, h);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/RenderUtils;drawTexturedRect(IIIIII)V", ordinal = 1))
    private void proteus$trackEnd(int x, int y, int u, int v, int w, int h) {
        if (!MalilibSkin.active()) {
            RenderUtils.drawTexturedRect(x, y, u, v, w, h);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/RenderUtils;drawTexturedRect(IIIIII)V", ordinal = 2))
    private void proteus$thumb(int x, int y, int u, int v, int w, int h) {
        if (!MalilibSkin.button(x, y, sliderWidth, 20, false, true)) {
            RenderUtils.drawTexturedRect(x, y, u, v, w, h);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/RenderUtils;drawTexturedRect(IIIIII)V", ordinal = 3))
    private void proteus$thumbEnd(int x, int y, int u, int v, int w, int h) {
        if (!MalilibSkin.active()) {
            RenderUtils.drawTexturedRect(x, y, u, v, w, h);
        }
    }
}
