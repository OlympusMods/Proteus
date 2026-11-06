package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.EffectsHud;
import com.github.olympus.proteus.client.TooltipHooks;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class TooltipMixin {
    @Redirect(method = "tooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/TooltipRenderUtil;extractTooltipBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIILnet/minecraft/resources/Identifier;)V"))
    private void proteus$background(GuiGraphicsExtractor graphics, int x, int y, int w, int h, Identifier style) {
        if (!TooltipHooks.background(graphics, x, y, w, h)) {
            TooltipRenderUtil.extractTooltipBackground(graphics, x, y, w, h, style);
        }
    }

    @Inject(method = "extractDeferredElements", at = @At("TAIL"))
    private void proteus$effectTip(int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
        EffectsHud.tip((GuiGraphicsExtractor) (Object) this);
    }
}
