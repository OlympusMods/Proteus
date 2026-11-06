package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.render.GuiContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiBase.class, remap = false)
public abstract class GuiBaseMixin {
    @Inject(method = "drawScreenBackground", at = @At("HEAD"), cancellable = true)
    private void proteus$background(GuiContext ctx, int mouseX, int mouseY, CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        if (MalilibSkin.backdrop(ctx, screen.width, screen.height)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawTexturedBG", at = @At("HEAD"), cancellable = true)
    private void proteus$window(GuiContext ctx, int x, int y, int width, int height, boolean blur, CallbackInfo ci) {
        if (MalilibSkin.window(ctx, x, y, width, height)) {
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void proteus$begin(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        MalilibSkin.beginFrame();
    }

    @Inject(method = "drawHoveredWidget", at = @At("HEAD"))
    private void proteus$labels(GuiContext ctx, int mouseX, int mouseY, CallbackInfo ci) {
        MalilibSkin.flushLabels(ctx);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void proteus$end(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        MalilibSkin.endFrame();
    }
}
