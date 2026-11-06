package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSelectionList.class)
public abstract class MenuListMixin {
    @Shadow
    protected abstract boolean isSelectedItem(int index);

    @Inject(method = "renderItem", at = @At("HEAD"))
    private void proteus$row(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, int index, int left,
            int top, int width, int height, CallbackInfo callback) {
        AbstractSelectionList<?> list = (AbstractSelectionList<?>) (Object) this;
        if (!MenuHud.themedLists() || list instanceof ContainerObjectSelectionList<?>) {
            return;
        }
        int x = list.getX() + (list.getWidth() - width) / 2;
        int y = top - 2;
        int h = height + 4;
        boolean over = list.isMouseOver(mouseX, mouseY) && mouseX >= x && mouseX < x + width && mouseY >= y
                && mouseY < y + h;
        boolean chosen = isSelectedItem(index);
        if (over || chosen) {
            MenuHud.row(list.children().get(index), x, y, width, h, true, chosen);
        }
    }

    @Redirect(method = "renderSelection", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    private void proteus$outline(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        if (!MenuHud.themedLists()) {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }
}
