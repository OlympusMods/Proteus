package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Checkbox.class)
public abstract class MenuCheckboxMixin {
    private static final int BOX = 17;

    @Shadow
    private boolean selected;

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void proteus$checkbox(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        if (MenuHud.checkbox((Checkbox) (Object) this, selected, BOX, true)) {
            callback.cancel();
        }
    }
}
