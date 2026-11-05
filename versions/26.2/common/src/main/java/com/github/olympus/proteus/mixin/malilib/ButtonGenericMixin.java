package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.render.GuiContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ButtonGeneric.class, remap = false)
public abstract class ButtonGenericMixin {
    @Shadow
    protected boolean renderDefaultBackground;

    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void proteus$background(GuiContext ctx, CallbackInfo ci) {
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

    @ModifyArg(method = "drawText", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/button/ButtonGeneric;drawCenteredStringWithShadow(Lfi/dy/masa/malilib/render/GuiContext;IIILjava/lang/String;)V"), index = 3)
    private int proteus$centeredColor(int color) {
        return proteus$color(color);
    }

    @ModifyArg(method = "drawText", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/button/ButtonGeneric;drawStringWithShadow(Lfi/dy/masa/malilib/render/GuiContext;IIILjava/lang/String;)V"), index = 3)
    private int proteus$leftColor(int color) {
        return proteus$color(color);
    }

    private int proteus$color(int color) {
        if (!renderDefaultBackground) {
            return color;
        }
        ButtonBaseAccess state = (ButtonBaseAccess) this;
        return MalilibSkin.buttonText(color, state.proteus$hovered(), state.proteus$enabled());
    }
}
