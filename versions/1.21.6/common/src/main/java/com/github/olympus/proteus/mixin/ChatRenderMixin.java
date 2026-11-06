package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ChatHooks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}
