package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.gui.widgets.WidgetCheckBox;
import fi.dy.masa.malilib.render.GuiContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WidgetCheckBox.class, remap = false)
public abstract class WidgetCheckBoxMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/interfaces/IGuiIcon;renderAt(Lfi/dy/masa/malilib/render/GuiContext;IIFZZ)V"))
    private void proteus$box(IGuiIcon icon, GuiContext ctx, int x, int y, float z, boolean first, boolean second) {
        boolean checked = ((WidgetCheckBox) (Object) this).isChecked();
        if (!MalilibSkin.checkbox(ctx, x, y, icon.getWidth(), checked)) {
            icon.renderAt(ctx, x, y, z, first, second);
        }
    }
}
