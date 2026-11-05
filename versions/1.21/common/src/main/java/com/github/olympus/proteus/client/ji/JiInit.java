package com.github.olympus.proteus.client.ji;

import com.github.olympus.proteus.client.ProteusScreen;
import net.minecraft.client.Minecraft;

final class JiInit {
    private JiInit() {
    }

    static void init(ProteusScreen view, Minecraft minecraft, int width, int height) {
        view.init(minecraft, width, height);
    }
}
