package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MenuScreenMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void proteus$enterMenu(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        MenuHud.begin((Screen) (Object) this, graphics);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void proteus$leaveMenu(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        MenuHud.end();
    }

    @Inject(method = "extractBlurredBackground", at = @At("HEAD"), cancellable = true)
    private void proteus$skipBlur(GuiGraphicsExtractor graphics, CallbackInfo callback) {
        if (MenuHud.skipsBlur((Screen) (Object) this)) {
            callback.cancel();
        }
    }
}
