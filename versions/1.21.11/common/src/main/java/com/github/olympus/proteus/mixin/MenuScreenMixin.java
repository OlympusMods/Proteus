package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MenuScreenMixin {
    @Inject(method = "renderWithTooltipAndSubtitles", at = @At("HEAD"))
    private void proteus$enterMenu(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        MenuHud.begin((Screen) (Object) this, graphics);
    }

    @Inject(method = "renderWithTooltipAndSubtitles", at = @At("RETURN"))
    private void proteus$leaveMenu(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        MenuHud.end((Screen) (Object) this);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void proteus$backdrop(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        MenuHud.backdrop((Screen) (Object) this);
    }

    @Inject(method = "renderBlurredBackground", at = @At("HEAD"), cancellable = true)
    private void proteus$skipBlur(GuiGraphics graphics, CallbackInfo callback) {
        if (MenuHud.skipsBlur((Screen) (Object) this)) {
            callback.cancel();
        }
    }
}
