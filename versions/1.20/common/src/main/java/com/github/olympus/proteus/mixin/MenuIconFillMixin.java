package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ui.IconShape;
import com.github.olympus.proteus.client.ui.Menus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ServerSelectionList.OnlineServerEntry.class, WorldSelectionList.WorldListEntry.class})
public abstract class MenuIconFillMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    private void proteus$round(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        int size = x1 - x0;
        int radius = size == y1 - y0 ? Menus.iconRadius(size) : 0;
        if (radius <= 0) {
            graphics.fill(x0, y0, x1, y1, color);
            return;
        }
        for (int[] run : IconShape.runs(size, radius)) {
            graphics.fill(x0 + run[0], y0 + run[1], x0 + run[0] + run[2], y0 + run[1] + run[3], color);
        }
    }
}
