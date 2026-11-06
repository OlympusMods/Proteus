package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import com.github.olympus.proteus.client.ui.Menus;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MenuTitleMixin {
    @Inject(method = "renderBackground", at = @At("TAIL"))
    private void proteus$titleBackdrop(CallbackInfo callback) {
        MenuHud.backdrop(Menus.Kind.TITLE);
    }
}
