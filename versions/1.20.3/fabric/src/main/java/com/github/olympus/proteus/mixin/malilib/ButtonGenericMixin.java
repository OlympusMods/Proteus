package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ButtonGeneric.class, remap = false)
public abstract class ButtonGenericMixin {
    @Shadow
    protected boolean renderDefaultBackground;

    private boolean proteus$background() {
        ButtonGeneric button = (ButtonGeneric) (Object) this;
        ButtonBaseAccess state = (ButtonBaseAccess) button;
        return renderDefaultBackground && MalilibSkin.button(button.getX(), button.getY(), button.getWidth(),
                button.getHeight(), state.proteus$hovered(), state.proteus$enabled());
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_52706(Lnet/minecraft/class_2960;IIII)V", ordinal = 0))
    private void proteus$sprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int w, int h) {
        if (!proteus$background()) {
            graphics.blitSprite(sprite, x, y, w, h);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/button/ButtonGeneric;drawCenteredStringWithShadow(IIILjava/lang/String;Lnet/minecraft/class_332;)V"))
    private void proteus$centered(ButtonGeneric button, int x, int y, int color, String text, GuiGraphics graphics) {
        if (!proteus$label(x, y, text, true)) {
            button.drawCenteredStringWithShadow(x, y, color, text, graphics);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/button/ButtonGeneric;drawStringWithShadow(IIILjava/lang/String;Lnet/minecraft/class_332;)V"))
    private void proteus$left(ButtonGeneric button, int x, int y, int color, String text, GuiGraphics graphics) {
        if (!proteus$label(x, y, text, false)) {
            button.drawStringWithShadow(x, y, color, text, graphics);
        }
    }

    private boolean proteus$label(int x, int y, String text, boolean centered) {
        if (!renderDefaultBackground) {
            return false;
        }
        ButtonBaseAccess state = (ButtonBaseAccess) this;
        return MalilibSkin.label(x, y, text, centered, state.proteus$hovered(), state.proteus$enabled());
    }
}
