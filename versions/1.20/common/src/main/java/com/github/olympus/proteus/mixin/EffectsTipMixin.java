package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.TooltipHooks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class EffectsTipMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void proteus$effectTip(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        TooltipHooks.tip(graphics);
    }
}
