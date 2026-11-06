package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.WidgetSprites;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteIconButton.class)
public abstract class MenuIconMixin {
    @Shadow
    @Final
    protected WidgetSprites sprite;
    @Shadow
    @Final
    protected int spriteWidth;
    @Shadow
    @Final
    protected int spriteHeight;

    @Inject(method = "extractSprite", at = @At("HEAD"), cancellable = true)
    private void proteus$icon(GuiGraphicsExtractor graphics, int x, int y, CallbackInfo callback) {
        SpriteIconButton self = (SpriteIconButton) (Object) this;
        if (self instanceof SpriteIconButton.TextAndIcon) {
            return;
        }
        String id = sprite.get(self.isActive(), self.isHoveredOrFocused()).toString();
        if (MenuHud.icon(self, id, spriteWidth, spriteHeight)) {
            callback.cancel();
        }
    }
}
