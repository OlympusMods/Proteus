package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ChatHooks;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$DrawingBackgroundGraphicsAccess")
public abstract class ChatBackgroundMixin {
    @Redirect(method = "fill", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    private void proteus$fill(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        ChatHooks.fill(graphics, x0, y0, x1, y1, color);
    }

    @Redirect(method = "handleMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ActiveTextCollector;"
            + "accept(Lnet/minecraft/client/gui/TextAlignment;IILnet/minecraft/client/gui/ActiveTextCollector$Parameters;"
            + "Lnet/minecraft/util/FormattedCharSequence;)V"))
    private void proteus$text(ActiveTextCollector collector, TextAlignment alignment, int x, int y,
            ActiveTextCollector.Parameters parameters, FormattedCharSequence line) {
        ChatHooks.text(collector, alignment, x, y, parameters, line, true);
    }
}
