package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ToastHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public abstract class ToastOverlayMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void proteus$renderToast(GuiGraphicsExtractor graphics, CallbackInfo callback) {
        ToastHud.render(graphics);
    }
}
