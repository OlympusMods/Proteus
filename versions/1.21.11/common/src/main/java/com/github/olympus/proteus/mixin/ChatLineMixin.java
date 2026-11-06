package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ChatHooks;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$1")
public abstract class ChatLineMixin {
    @Shadow
    @Final
    ChatComponent.ChatGraphicsAccess val$graphics;

    @Inject(method = "accept(Lnet/minecraft/client/GuiMessage$Line;IF)V", at = @At("HEAD"), cancellable = true)
    private void proteus$lineStart(GuiMessage.Line line, int index, float alpha, CallbackInfo callback) {
        if (ChatHooks.lineStart(val$graphics, line)) {
            callback.cancel();
        }
    }

    @Inject(method = "accept(Lnet/minecraft/client/GuiMessage$Line;IF)V", at = @At("RETURN"))
    private void proteus$lineEnd(GuiMessage.Line line, int index, float alpha, CallbackInfo callback) {
        ChatHooks.lineEnd(val$graphics, line);
    }
}
