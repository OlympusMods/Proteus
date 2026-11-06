package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractScrollArea.class)
public abstract class MenuScrollMixin {
    @Shadow
    protected abstract boolean scrollable();

    @Shadow
    protected abstract int scrollBarX();

    @Shadow
    protected abstract int scrollerHeight();

    @Shadow
    public abstract int scrollBarY();

    @Shadow
    public abstract int scrollbarWidth();

    @Shadow
    protected abstract boolean isOverScrollbar(double x, double y);

    @Inject(method = "extractScrollbar", at = @At("HEAD"), cancellable = true)
    private void proteus$scrollbar(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo callback) {
        if (!scrollable()) {
            return;
        }
        AbstractScrollArea self = (AbstractScrollArea) (Object) this;
        if (MenuHud.scrollbar(scrollBarX(), self.getY(), scrollbarWidth(), self.getHeight(), scrollBarY(),
                scrollerHeight(), isOverScrollbar(mouseX, mouseY))) {
            callback.cancel();
        }
    }
}
