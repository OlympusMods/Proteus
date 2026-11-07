package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ui.Menus;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GuiGraphics.class)
public abstract class MenuTextMixin {
    @ModifyVariable(method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V", at = @At("HEAD"),
            argsOnly = true, ordinal = 0)
    private boolean proteus$string(boolean shadow) {
        return Menus.shadow(shadow);
    }

    @ModifyVariable(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean proteus$sequence(boolean shadow) {
        return Menus.shadow(shadow);
    }

    @ModifyVariable(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean proteus$component(boolean shadow) {
        return Menus.shadow(shadow);
    }
}
