package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.EffectsHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectsInInventory.class)
public abstract class EffectsInventoryMixin {
    @Shadow
    @Final
    private AbstractContainerScreen<?> screen;

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void proteus$effects(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo callback) {
        ContainerScreenAccess access = (ContainerScreenAccess) screen;
        int left = access.proteus$left() + access.proteus$imageWidth() + 2;
        int available = screen.width - left;
        if (available < 32) {
            return;
        }
        if (EffectsHud.inventory(graphics, left, access.proteus$top(), available, mouseX, mouseY)) {
            callback.cancel();
        }
    }
}
