package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSliderButton.class)
public abstract class MenuSliderMixin {
    @Shadow
    protected double value;

    @Shadow
    protected abstract void handleCursor(GuiGraphicsExtractor graphics);

    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"), cancellable = true)
    private void proteus$slider(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        if (MenuHud.slider((AbstractSliderButton) (Object) this, value)) {
            handleCursor(graphics);
            callback.cancel();
        }
    }
}
