package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.layouts.LayoutElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSelectionList.class)
public abstract class MenuListMixin {
    @Shadow
    protected abstract boolean entriesCanBeSelected();

    @Inject(method = "extractListItems", at = @At("HEAD"))
    private void proteus$rows(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        if (!MenuHud.themedLists() || !entriesCanBeSelected()) {
            return;
        }
        AbstractSelectionList<?> list = (AbstractSelectionList<?>) (Object) this;
        Object selected = list.getSelected();
        boolean inside = list.isMouseOver(mouseX, mouseY);
        for (Object child : list.children()) {
            LayoutElement entry = (LayoutElement) child;
            if (entry.getY() + entry.getHeight() < list.getY() || entry.getY() > list.getBottom()) {
                continue;
            }
            boolean chosen = child == selected;
            boolean over = inside && mouseX >= entry.getX() && mouseX < entry.getX() + entry.getWidth()
                    && mouseY >= entry.getY() && mouseY < entry.getY() + entry.getHeight();
            if (chosen || over) {
                MenuHud.row(child, entry.getX(), entry.getY(), entry.getWidth(), entry.getHeight(), over || chosen,
                        chosen);
            }
        }
    }

    @Redirect(method = "extractSelection", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private void proteus$outline(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color) {
        if (!MenuHud.themedLists()) {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }
}
