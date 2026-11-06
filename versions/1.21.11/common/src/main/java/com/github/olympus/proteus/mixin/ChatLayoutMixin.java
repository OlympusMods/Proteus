package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ChatHooks;
import java.util.List;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatLayoutMixin {
    @Shadow
    @Final
    private List<GuiMessage.Line> trimmedMessages;

    @Shadow
    private int chatScrollbarPos;

    @Shadow
    public abstract int getLinesPerPage();

    @Inject(method = "render(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IIZ)V",
            at = @At("HEAD"))
    private void proteus$layout(ChatComponent.ChatGraphicsAccess access, int first, int second, boolean focused,
            CallbackInfo callback) {
        ChatHooks.begin(trimmedMessages, chatScrollbarPos, getLinesPerPage());
    }

    @ModifyArg(method = "forEachLine", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/"
            + "ChatComponent$LineConsumer;accept(Lnet/minecraft/client/GuiMessage$Line;IF)V"), index = 0)
    private GuiMessage.Line proteus$line(GuiMessage.Line line) {
        ChatHooks.line(line);
        return line;
    }
}
