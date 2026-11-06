package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Checkbox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Checkbox.class)
public abstract class MenuCheckboxMixin {
    @Shadow
    private boolean selected;

    @Inject(method = "extractContents", at = @At("HEAD"), cancellable = true)
    private void proteus$checkbox(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        int box = Checkbox.getBoxSize(Minecraft.getInstance().font);
        if (MenuHud.checkbox((Checkbox) (Object) this, selected, box)) {
            callback.cancel();
        }
    }
}
