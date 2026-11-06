package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.Tooltips;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class TooltipHooks {
    private static final float DEPTH = 400f;
    private static final GuiCanvas CANVAS = new GuiCanvas();

    private TooltipHooks() {
    }

    public static boolean background(GuiGraphics graphics, int x, int y, int w, int h) {
        if (!Tooltips.active()) {
            return false;
        }
        CANVAS.bind(graphics, Minecraft.getInstance().font, graphics.guiWidth(), graphics.guiHeight());
        graphics.pose().translate(0f, 0f, DEPTH);
        try {
            return Tooltips.background(CANVAS, x, y, w, h);
        } finally {
            graphics.pose().translate(0f, 0f, -DEPTH);
        }
    }

    public static void tip(GuiGraphics graphics) {
        graphics.pose().pushPose();
        graphics.pose().translate(0f, 0f, DEPTH);
        try {
            EffectsHud.tip(graphics);
        } finally {
            graphics.pose().popPose();
        }
    }
}
