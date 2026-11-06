package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ChatHooks;
import java.util.List;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatMessageMixin {
    @Shadow
    @Final
    private List<GuiMessage.Line> trimmedMessages;

    @Inject(method = "addMessageToDisplayQueue", at = @At("TAIL"))
    private void proteus$bornLines(GuiMessage message, CallbackInfo callback) {
        ChatHooks.born(trimmedMessages, message);
    }
}
