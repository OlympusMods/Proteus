package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.EffectsHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectsInventoryMixin {
    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void proteus$effects(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo callback) {
        ContainerScreenAccess access = (ContainerScreenAccess) (Object) this;
        int left = access.proteus$left() + access.proteus$imageWidth() + 2;
        int available = ((Screen) (Object) this).width - left;
        if (available < 32) {
            return;
        }
        if (EffectsHud.inventory(graphics, left, access.proteus$top(), available, mouseX, mouseY)) {
            callback.cancel();
        }
    }
}
