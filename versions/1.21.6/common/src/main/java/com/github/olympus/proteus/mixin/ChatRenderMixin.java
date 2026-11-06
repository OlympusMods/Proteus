package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ChatHooks;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatRenderMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void proteus$enter(GuiGraphics graphics, int tick, int mouseX, int mouseY, boolean focused,
            CallbackInfo callback) {
        ChatHooks.enter();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void proteus$leave(GuiGraphics graphics, int tick, int mouseX, int mouseY, boolean focused,
            CallbackInfo callback) {
        ChatHooks.leave();
    }

    @Inject(method = "drawTagIcon", at = @At("HEAD"), cancellable = true)
    private void proteus$hideTag(GuiGraphics graphics, int x, int y, GuiMessageTag.Icon icon, CallbackInfo callback) {
        if (ChatHooks.tagHidden()) {
            callback.cancel();
        }
    }

    @ModifyVariable(method = "drawTagIcon", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int proteus$liftTag(int y) {
        return y + ChatHooks.tagLift();
    }

    @ModifyArg(method = "forEachLine", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/"
            + "ChatComponent$LineConsumer;accept(IIILnet/minecraft/client/GuiMessage$Line;IF)V"), index = 3)
    private GuiMessage.Line proteus$line(GuiMessage.Line line) {
        ChatHooks.line(line);
        return line;
    }
}
