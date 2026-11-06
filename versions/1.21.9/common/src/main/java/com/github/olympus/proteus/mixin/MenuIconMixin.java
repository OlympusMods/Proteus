package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SpriteIconButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteIconButton.CenteredIcon.class)
public abstract class MenuIconMixin {
    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void proteus$icon(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        SpriteIconButton self = (SpriteIconButton) (Object) this;
        SpriteIconAccess access = (SpriteIconAccess) self;
        if (MenuHud.button(self, false)) {
            String id = access.proteus$sprite().get(self.isActive(), self.isHoveredOrFocused()).toString();
            MenuHud.sprite(self, id, access.proteus$spriteWidth(), access.proteus$spriteHeight());
            callback.cancel();
        }
    }
}
