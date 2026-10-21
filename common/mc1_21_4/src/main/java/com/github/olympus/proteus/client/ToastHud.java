package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.ToastOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class ToastHud {
    private static final GuiCanvas CANVAS = new GuiCanvas();

    private ToastHud() {
    }

    public static void render(GuiGraphics graphics) {
        if (!ToastOverlay.active()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) {
            return;
        }
        try {
            CANVAS.bind(graphics, minecraft.font, graphics.guiWidth(), graphics.guiHeight());
            ToastOverlay.render(CANVAS);
        } catch (RuntimeException exception) {
            ProteusClient.LOGGER.warn("Proteus: could not draw the toast", exception);
            ToastOverlay.dismiss();
        }
    }
}
