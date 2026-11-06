package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.SpriteIconButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractButton.class)
public abstract class MenuButtonMixin {
    @Inject(method = "renderDefaultSprite", at = @At("HEAD"), cancellable = true)
    private void proteus$sprite(GuiGraphics graphics, CallbackInfo callback) {
        AbstractButton self = (AbstractButton) (Object) this;
        if (MenuHud.button(self, !(self instanceof SpriteIconButton))) {
            callback.cancel();
        }
    }

    @Inject(method = "renderDefaultLabel", at = @At("HEAD"), cancellable = true)
    private void proteus$label(ActiveTextCollector output, CallbackInfo callback) {
        if (MenuHud.drew((AbstractButton) (Object) this)) {
            callback.cancel();
        }
    }
}
