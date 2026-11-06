package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.TooltipHooks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TooltipRenderUtil.class)
public abstract class TooltipMixin {
    @Inject(method = "renderTooltipBackground(Lnet/minecraft/client/gui/GuiGraphics;IIIII)V", at = @At("HEAD"), cancellable = true)
    private static void proteus$background(GuiGraphics graphics, int x, int y, int w, int h, int z,
            CallbackInfo callback) {
        if (TooltipHooks.background(graphics, x, y, w, h)) {
            callback.cancel();
        }
    }
}
