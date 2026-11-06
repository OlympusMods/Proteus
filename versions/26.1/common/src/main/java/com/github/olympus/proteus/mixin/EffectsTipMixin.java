package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.EffectsHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class EffectsTipMixin {
    @Inject(method = "extractDeferredElements", at = @At("TAIL"))
    private void proteus$effectTip(int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
        EffectsHud.tip((GuiGraphicsExtractor) (Object) this);
    }
}
