package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ui.Tooltips;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientTextTooltip.class)
public abstract class TooltipTextMixin {
    private static final String DRAW = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I";

    @ModifyArg(method = "renderText", at = @At(value = "INVOKE", target = DRAW), index = 0)
    private FormattedCharSequence proteus$sequence(FormattedCharSequence text) {
        return Tooltips.sequence(text);
    }

    @ModifyArg(method = "renderText", at = @At(value = "INVOKE", target = DRAW), index = 3)
    private int proteus$color(int color) {
        return Tooltips.textColor(color);
    }

    @ModifyArg(method = "renderText", at = @At(value = "INVOKE", target = DRAW), index = 4)
    private boolean proteus$shadow(boolean shadow) {
        return Tooltips.shadow(shadow);
    }
}
