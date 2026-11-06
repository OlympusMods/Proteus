package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.Tooltips;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class TooltipHooks {
    private static final GuiCanvas CANVAS = new GuiCanvas();

    private TooltipHooks() {
    }

    public static boolean background(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        if (!Tooltips.active()) {
            return false;
        }
        CANVAS.bind(graphics, Minecraft.getInstance().font, graphics.guiWidth(), graphics.guiHeight());
        return Tooltips.background(CANVAS, x, y, w, h);
    }
}
