package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ChatHooks;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class ChatGraphicsMixin {
    @Inject(method = "fill(IIIII)V", at = @At("HEAD"), cancellable = true)
    private void proteus$fill(int x0, int y0, int x1, int y1, int color, CallbackInfo callback) {
        if (ChatHooks.inChat() && ChatHooks.fill((GuiGraphics) (Object) this, x0, y0, x1, y1, color)) {
            callback.cancel();
        }
    }

    @Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)V",
            at = @At("HEAD"), cancellable = true)
    private void proteus$text(Font font, FormattedCharSequence line, int x, int y, int color,
            CallbackInfo callback) {
        if (ChatHooks.inChat() && ChatHooks.text((GuiGraphics) (Object) this, font, line, x, y, color)) {
            callback.cancel();
        }
    }
}
