package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ui.Mods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Minecraft.class)
public abstract class GuiScreenMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen proteus$replace(Screen screen) {
        return Mods.replace(screen);
    }
}
