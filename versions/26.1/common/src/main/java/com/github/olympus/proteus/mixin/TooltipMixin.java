package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.TooltipHooks;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TooltipRenderUtil.class)
public abstract class TooltipMixin {
    @Inject(method = "extractTooltipBackground", at = @At("HEAD"), cancellable = true)
    private static void proteus$background(GuiGraphicsExtractor graphics, int x, int y, int w, int h,
            Identifier style, CallbackInfo callback) {
        if (TooltipHooks.background(graphics, x, y, w, h)) {
            callback.cancel();
        }
    }
}
