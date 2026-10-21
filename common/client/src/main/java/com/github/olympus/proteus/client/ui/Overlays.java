package com.github.olympus.proteus.client.ui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Overlays {
    private static final Logger LOGGER = LoggerFactory.getLogger("proteus");
    private static final List<Overlay> ALL = new CopyOnWriteArrayList<>();

    private Overlays() {
    }

    public static void add(Overlay overlay) {
        ALL.add(overlay);
    }

    public static boolean any() {
        for (Overlay overlay : ALL) {
            if (overlay.visible()) {
                return true;
            }
        }
        return false;
    }

    public static void render(Canvas canvas) {
        for (Overlay overlay : ALL) {
            if (!overlay.visible()) {
                continue;
            }
            try {
                overlay.render(canvas);
            } catch (RuntimeException exception) {
                LOGGER.warn("Proteus: removing an overlay that failed to draw", exception);
                ALL.remove(overlay);
            }
        }
    }
}
