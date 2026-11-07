package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import fi.dy.masa.malilib.render.RenderUtils;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.GuiGraphics;

@Mixin(value = RenderUtils.class, remap = false)
public abstract class RenderUtilsMixin {
    @Inject(method = {"drawHoverText(Lnet/minecraft/class_332;IILjava/util/List;)V", "drawHoverText(Lnet/minecraft/client/gui/GuiGraphics;IILjava/util/List;)V"}, at = @At("HEAD"), cancellable = true)
    private static void proteus$tooltip(GuiGraphics ctx, int x, int y, List<String> lines, CallbackInfo ci) {
        if (MalilibSkin.tooltip(ctx, x, y, lines)) {
            ci.cancel();
        }
    }

    @Inject(method = {"drawRect(Lnet/minecraft/class_332;IIIII)V", "drawRect(Lnet/minecraft/client/gui/GuiGraphics;IIIII)V"}, at = @At("HEAD"), cancellable = true)
    private static void proteus$rect(GuiGraphics ctx, int x, int y, int width, int height, int color, CallbackInfo ci) {
        if (MalilibSkin.rect(ctx, x, y, width, height, color)) {
            ci.cancel();
        }
    }

    @Inject(method = {"drawOutlinedBox(Lnet/minecraft/class_332;IIIIII)V", "drawOutlinedBox(Lnet/minecraft/client/gui/GuiGraphics;IIIIII)V"}, at = @At("HEAD"), cancellable = true)
    private static void proteus$dialog(GuiGraphics ctx, int x, int y, int width, int height, int background, int border,
            CallbackInfo ci) {
        if (MalilibSkin.list(ctx, x, y, width, height)) {
            ci.cancel();
        } else if (width >= 100 && height >= 40 && MalilibSkin.window(ctx, x, y, width, height)) {
            ci.cancel();
        }
    }
}
