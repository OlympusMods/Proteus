package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.RenderUtils;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderUtils.class, remap = false)
public abstract class RenderUtilsMixin {
    @Inject(method = "drawHoverText(Lfi/dy/masa/malilib/render/GuiContext;IILjava/util/List;)V", at = @At("HEAD"), cancellable = true)
    private static void proteus$tooltip(GuiContext ctx, int x, int y, List<String> lines, CallbackInfo ci) {
        if (MalilibSkin.tooltip(ctx, x, y, lines)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawRect(Lfi/dy/masa/malilib/render/GuiContext;IIIII)V", at = @At("HEAD"), cancellable = true)
    private static void proteus$rect(GuiContext ctx, int x, int y, int width, int height, int color, CallbackInfo ci) {
        if (MalilibSkin.rect(ctx, x, y, width, height, color)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawOutlinedBox(Lfi/dy/masa/malilib/render/GuiContext;IIIIII)V", at = @At("HEAD"), cancellable = true)
    private static void proteus$dialog(GuiContext ctx, int x, int y, int width, int height, int background, int border,
            CallbackInfo ci) {
        if (MalilibSkin.list(ctx, x, y, width, height)) {
            ci.cancel();
        } else if (width >= 100 && height >= 40 && MalilibSkin.widgetBox(ctx, x, y, width, height)) {
            ci.cancel();
        } else if (width >= 100 && height >= 40 && MalilibSkin.window(ctx, x, y, width, height)) {
            ci.cancel();
        }
    }
}
