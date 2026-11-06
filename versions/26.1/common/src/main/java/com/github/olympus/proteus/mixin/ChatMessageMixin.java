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

    @Shadow
    private int chatScrollbarPos;

    @Shadow
    public abstract int getLinesPerPage();

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/"
            + "minecraft/client/gui/components/ChatComponent$DisplayMode;)V", at = @At("HEAD"))
    private void proteus$layout(ChatComponent.ChatGraphicsAccess access, int bottom, int tick,
            ChatComponent.DisplayMode mode, CallbackInfo callback) {
        ChatHooks.begin(trimmedMessages, chatScrollbarPos, getLinesPerPage());
    }

    @Inject(method = "lambda$extractRenderState$1", at = @At("HEAD"))
    private static void proteus$bubble(int chatBottom, int entryHeight, ChatComponent.ChatGraphicsAccess access,
            int width, float backgroundOpacity, GuiMessage.Line line, int index, float alpha, CallbackInfo callback) {
        ChatHooks.background(chatBottom, line, alpha, backgroundOpacity);
    }

    @Inject(method = "addMessageToDisplayQueue", at = @At("TAIL"))
    private void proteus$bornLines(GuiMessage message, CallbackInfo callback) {
        ChatHooks.born(trimmedMessages, message);
    }
}
