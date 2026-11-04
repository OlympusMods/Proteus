package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.VanillaToasts;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastComponent.class)
public abstract class ToastAddMixin {
    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    private void proteus$replaceToast(Toast toast, CallbackInfo callback) {
        if (VanillaToasts.replace(toast)) {
            callback.cancel();
        }
    }
}
