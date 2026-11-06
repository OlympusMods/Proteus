package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.EffectsHud;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class EffectsTipMixin {
    @Inject(method = "renderDeferredElements", at = @At("TAIL"))
    private void proteus$effectTip(CallbackInfo callback) {
        EffectsHud.tip((GuiGraphics) (Object) this);
    }
}
