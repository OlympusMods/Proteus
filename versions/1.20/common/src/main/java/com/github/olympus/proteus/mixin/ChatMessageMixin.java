package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ChatHooks;
import java.util.List;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
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

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;"
            + "ILnet/minecraft/client/GuiMessageTag;Z)V", at = @At("TAIL"), require = 0)
    private void proteus$bornLines(Component component, MessageSignature signature, int tick,
            GuiMessageTag tag, boolean refresh, CallbackInfo callback) {
        ChatHooks.born(trimmedMessages, tick);
    }

    @Inject(method = "addMessageToDisplayQueue", at = @At("TAIL"), require = 0)
    private void proteus$bornQueued(GuiMessage message, CallbackInfo callback) {
        ChatHooks.born(trimmedMessages, message.addedTime());
    }
}
