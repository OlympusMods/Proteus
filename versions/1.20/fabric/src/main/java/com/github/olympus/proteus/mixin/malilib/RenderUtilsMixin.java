package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import fi.dy.masa.malilib.render.RenderUtils;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderUtils.class, remap = false)
public abstract class RenderUtilsMixin {
    @Inject(method = "drawHoverText(IILjava/util/List;Lnet/minecraft/class_332;)V", at = @At("HEAD"), cancellable = true)
    private static void proteus$tooltip(int x, int y, List<String> lines, GuiGraphics graphics, CallbackInfo ci) {
        if (MalilibSkin.tooltip(x, y, lines)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawRect(IIIII)V", at = @At("HEAD"), cancellable = true)
    private static void proteus$rect(int x, int y, int width, int height, int color, CallbackInfo ci) {
        if (MalilibSkin.rect(x, y, width, height, color)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawOutlinedBox(IIIIII)V", at = @At("HEAD"), cancellable = true)
    private static void proteus$dialog(int x, int y, int width, int height, int background, int border, CallbackInfo ci) {
        if (MalilibSkin.list(x, y, width, height)) {
            ci.cancel();
        } else if (width >= 100 && height >= 40 && MalilibSkin.window(x, y, width, height)) {
            ci.cancel();
        }
    }
}
