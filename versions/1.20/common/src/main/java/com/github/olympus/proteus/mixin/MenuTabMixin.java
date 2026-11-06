package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.TabButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TabButton.class)
public abstract class MenuTabMixin {
    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void proteus$tab(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        TabButton self = (TabButton) (Object) this;
        if (MenuHud.tab(self, self.isSelected())) {
            callback.cancel();
        }
    }
}
