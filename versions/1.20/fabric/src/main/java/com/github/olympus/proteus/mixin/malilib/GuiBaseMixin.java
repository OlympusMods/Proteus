package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import fi.dy.masa.malilib.gui.GuiBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiBase.class, remap = false)
public abstract class GuiBaseMixin {
    @Inject(method = {"method_25394", "render"}, at = @At("HEAD"))
    private void proteus$begin(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        MalilibSkin.beginFrame(graphics);
    }

    @Inject(method = {"method_25394", "render"}, at = @At("TAIL"))
    private void proteus$end(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        MalilibSkin.endFrame();
    }

    @Inject(method = "drawScreenBackground(II)V", at = @At("HEAD"), cancellable = true)
    private void proteus$background(int mouseX, int mouseY, CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        if (MalilibSkin.backdrop(screen.width, screen.height)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawScreenBackground(Lnet/minecraft/class_332;II)V", at = @At("HEAD"), cancellable = true)
    private void proteus$backgroundWith(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        proteus$background(mouseX, mouseY, ci);
    }

    @Inject(method = "drawTexturedBG(Lnet/minecraft/class_332;IIIIZ)V", at = @At("HEAD"), cancellable = true)
    private void proteus$window(GuiGraphics graphics, int x, int y, int width, int height, boolean blur, CallbackInfo ci) {
        if (MalilibSkin.window(x, y, width, height)) {
            ci.cancel();
        }
    }
}
