package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ChatHooks;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.components.CommandSuggestions$SuggestionsList")
public abstract class ChatSuggestListMixin {
    @Shadow
    @Final
    private Rect2i rect;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void proteus$box(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo callback) {
        ChatHooks.suggestBox(graphics, rect.getX() - 1, rect.getY() - 1, rect.getX() + rect.getWidth() + 1,
                rect.getY() + rect.getHeight() + 1);
    }

    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private void proteus$row(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color) {
        ChatHooks.suggestRow(graphics, x0, y0, x1, y1, color);
    }
}
