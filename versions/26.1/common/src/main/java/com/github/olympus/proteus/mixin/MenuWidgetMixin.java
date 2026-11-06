package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractWidget.class)
public abstract class MenuWidgetMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void proteus$open(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        MenuHud.widgetBegin((AbstractWidget) (Object) this);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void proteus$opened(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        MenuHud.widgetEnd((AbstractWidget) (Object) this);
    }
}
