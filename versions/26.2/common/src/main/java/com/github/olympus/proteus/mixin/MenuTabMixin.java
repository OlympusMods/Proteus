package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.tabs.MenuTabBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MenuTabBar.MenuTabButton.class)
public abstract class MenuTabMixin {
    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"), cancellable = true)
    private void proteus$tab(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        TabButton self = (TabButton) (Object) this;
        if (MenuHud.tab(self, self.isSelected())) {
            callback.cancel();
        }
    }
}
