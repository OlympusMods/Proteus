package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import com.github.olympus.proteus.client.ui.Menus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MenuScreenMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void proteus$enterMenu(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        MenuHud.begin((Screen) (Object) this, graphics);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void proteus$leaveMenu(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        MenuHud.end();
    }

    @Inject(method = "renderBackground", at = @At("TAIL"))
    private void proteus$pauseBackdrop(CallbackInfo callback) {
        MenuHud.backdrop(Menus.Kind.PAUSE);
    }
}
