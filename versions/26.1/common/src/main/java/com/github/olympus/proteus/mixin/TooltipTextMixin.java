package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ui.Tooltips;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientTextTooltip.class)
public abstract class TooltipTextMixin {
    @ModifyArg(method = "extractText", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V"),
            index = 1)
    private FormattedCharSequence proteus$sequence(FormattedCharSequence text) {
        return Tooltips.sequence(text);
    }

    @ModifyArg(method = "extractText", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V"),
            index = 4)
    private int proteus$color(int color) {
        return Tooltips.textColor(color);
    }

    @ModifyArg(method = "extractText", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V"),
            index = 5)
    private boolean proteus$shadow(boolean shadow) {
        return Tooltips.shadow(shadow);
    }
}
