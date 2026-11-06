package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ChatHooks;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatRenderMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"), require = 0)
    private void proteus$fill(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        ChatHooks.fill(graphics, x0, y0, x1, y1, color);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIIII)V"), require = 0)
    private void proteus$fillDepth(GuiGraphics graphics, int x0, int y0, int x1, int y1, int z, int color) {
        ChatHooks.fill(graphics, x0, y0, x1, y1, z, color);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;"
            + "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"),
            require = 0)
    private int proteus$text(GuiGraphics graphics, Font font, FormattedCharSequence line, int x, int y, int color) {
        return ChatHooks.text(graphics, font, line, x, y, color);
    }

    @Inject(method = "drawTagIcon", at = @At("HEAD"), cancellable = true, require = 0)
    private void proteus$hideTag(GuiGraphics graphics, int x, int y, GuiMessageTag.Icon icon, CallbackInfo callback) {
        if (ChatHooks.tagHidden()) {
            callback.cancel();
        }
    }

    @ModifyVariable(method = "drawTagIcon", at = @At("HEAD"), argsOnly = true, ordinal = 1, require = 0)
    private int proteus$liftTag(int y) {
        return y + ChatHooks.tagLift();
    }
}
