package com.github.olympus.proteus.mixin.malilib;

import com.github.olympus.proteus.client.malilib.MalilibSkin;
import fi.dy.masa.malilib.gui.GuiScrollBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiScrollBar.class, remap = false)
public abstract class GuiScrollBarMixin {
    @Shadow
    protected boolean renderScrollbarBackground;
    @Shadow
    protected boolean mouseOver;
    @Shadow
    protected int currentValue;
    @Shadow
    protected int maxValue;

    @Inject(method = "render(IIFIIIII)V", at = @At("TAIL"))
    private void proteus$skin(int mouseX, int mouseY, float partialTicks, int x, int y, int width, int height,
            int totalHeight, CallbackInfo ci) {
        if (!renderScrollbarBackground || totalHeight <= 0) {
            return;
        }
        int slide = height - 2;
        float relative = Math.min(1.0F, (float) slide / (float) totalHeight);
        int barHeight = (int) (relative * slide);
        int travel = slide - barHeight;
        int barY = y + 1 + (maxValue > 0 ? (int) ((currentValue / (float) maxValue) * travel) : 0);
        MalilibSkin.scrollbar(x, y, width, height, barY, barHeight, mouseOver);
    }
}
