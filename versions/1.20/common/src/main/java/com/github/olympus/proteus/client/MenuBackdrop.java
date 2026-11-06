package com.github.olympus.proteus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.PanoramaRenderer;

final class MenuBackdrop {
    private static PanoramaRenderer panorama;

    private MenuBackdrop() {
    }

    static boolean paintsAtStart() {
        return true;
    }

    static void below() {
        if (panorama == null) {
            panorama = new PanoramaRenderer(TitleScreen.CUBE_MAP);
        }
        panorama.render(Minecraft.getInstance().getDeltaFrameTime(), 1f);
    }
}
