package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import fi.dy.masa.malilib.gui.widgets.WidgetDropDownList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WidgetDropDownList.class, remap = false)
public abstract class WidgetDropDownListMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void proteus$enter(CallbackInfo ci) {
        MalilibSkin.dropdown(true);
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE",
            target = "Lfi/dy/masa/malilib/gui/widgets/WidgetDropDownList;drawString", ordinal = 1), index = 3)
    private int proteus$text(int color) {
        return MalilibSkin.dropdownText(color);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void proteus$leave(CallbackInfo ci) {
        MalilibSkin.dropdown(false);
    }
}
