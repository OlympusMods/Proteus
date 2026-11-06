package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.EffectsHud;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class EffectsHudMixin {
    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void proteus$effects(GuiGraphics graphics, float tickDelta, CallbackInfo callback) {
        if (EffectsHud.hud(graphics)) {
            callback.cancel();
        }
    }
}
