package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ChatHooks;
import java.util.List;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatComponent.class)
public abstract class ChatLayoutMixin {
    @Shadow
    @Final
    private List<GuiMessage.Line> trimmedMessages;

    @Shadow
    private int chatScrollbarPos;

    @Shadow
    public abstract int getLinesPerPage();

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;III)V", at = @At("HEAD"), require = 0)
    private void proteus$layout(GuiGraphics graphics, int tick, int mouseX, int mouseY, CallbackInfo callback) {
        ChatHooks.begin(trimmedMessages, chatScrollbarPos, getLinesPerPage());
    }

    @Inject(method = "screenToChatY", at = @At("RETURN"), cancellable = true, require = 0)
    private void proteus$hit(double y, CallbackInfoReturnable<Double> callback) {
        callback.setReturnValue(ChatHooks.chatY(callback.getReturnValueD()));
    }
}
