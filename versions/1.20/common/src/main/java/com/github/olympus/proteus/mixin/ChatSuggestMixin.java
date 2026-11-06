package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ChatHooks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandSuggestions.class)
public abstract class ChatSuggestMixin {
    @Redirect(method = "renderUsage", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"), require = 0)
    private void proteus$usage(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        ChatHooks.suggest(graphics, x0, y0, x1, y1, color);
    }
}
