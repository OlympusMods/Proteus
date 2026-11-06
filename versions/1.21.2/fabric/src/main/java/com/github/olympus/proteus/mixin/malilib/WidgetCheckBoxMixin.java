package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.gui.widgets.WidgetCheckBox;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WidgetCheckBox.class, remap = false)
public abstract class WidgetCheckBoxMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/interfaces/IGuiIcon;renderAt(IIFZZLnet/minecraft/class_332;)V"))
    private void proteus$box(IGuiIcon icon, int x, int y, float z, boolean first, boolean second, GuiGraphics graphics) {
        boolean checked = ((WidgetCheckBox) (Object) this).isChecked();
        if (!MalilibSkin.checkbox(x, y, icon.getWidth(), checked)) {
            icon.renderAt(x, y, z, first, second, graphics);
        }
    }
}
