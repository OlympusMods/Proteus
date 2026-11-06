package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.GuiGraphics;

@Mixin(value = ButtonGeneric.class, remap = false)
public abstract class ButtonGenericMixin {
    @Shadow
    protected boolean renderDefaultBackground;

    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void proteus$background(GuiGraphics ctx, CallbackInfo ci) {
        if (!renderDefaultBackground) {
            return;
        }
        ButtonGeneric button = (ButtonGeneric) (Object) this;
        ButtonBaseAccess state = (ButtonBaseAccess) button;
        if (MalilibSkin.button(ctx, button.getX(), button.getY(), button.getWidth(), button.getHeight(),
                state.proteus$hovered(), state.proteus$enabled())) {
            ci.cancel();
        }
    }

    @Redirect(method = "drawText", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/button/ButtonGeneric;drawCenteredStringWithShadow"))
    private void proteus$centered(ButtonGeneric button, GuiGraphics ctx, int x, int y, int color, String text) {
        if (!proteus$label(ctx, x, y, text, true)) {
            button.drawCenteredStringWithShadow(ctx, x, y, color, text);
        }
    }

    @Redirect(method = "drawText", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/button/ButtonGeneric;drawStringWithShadow"))
    private void proteus$left(ButtonGeneric button, GuiGraphics ctx, int x, int y, int color, String text) {
        if (!proteus$label(ctx, x, y, text, false)) {
            button.drawStringWithShadow(ctx, x, y, color, text);
        }
    }

    private boolean proteus$label(GuiGraphics ctx, int x, int y, String text, boolean centered) {
        if (!renderDefaultBackground) {
            return false;
        }
        ButtonBaseAccess state = (ButtonBaseAccess) this;
        return MalilibSkin.label(ctx, x, y, text, centered, state.proteus$hovered(), state.proteus$enabled());
    }
}
