package com.github.olympus.proteus.client.ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Icons {
    /** Draws a mod's icon in the Modern style, centered on cx and cy. */
    @FunctionalInterface
    public interface Painter {
        void draw(Canvas c, int cx, int cy, int color);
    }

    /** An icon a mod added: its Modern painter and the size of its Classic sprite. */
    public record Custom(Painter painter, int classicWidth, int classicHeight) {
    }

    private static final Map<String, Custom> CUSTOM = new ConcurrentHashMap<>();

    private Icons() {
    }

    /**
     * Adds an icon under a name with the mod id in front, such as "orpheus:play". The Classic style
     * draws the sprite modid:classic/icon/name, so the mod ships its own sprite and resource packs can restyle it.
     */
    public static void register(String name, int classicWidth, int classicHeight, Painter painter) {
        if (name.indexOf(':') <= 0) {
            throw new IllegalArgumentException("Icon names start with the mod id: " + name);
        }
        CUSTOM.put(name, new Custom(painter, classicWidth, classicHeight));
    }

    public static Custom custom(String name) {
        return CUSTOM.get(name);
    }

    public static void check(Canvas c, int x, int y, int color) {
        int[] tops = {3, 4, 5, 4, 3, 2, 1, 0};
        for (int i = 0; i < tops.length; i++) {
            c.fill(x + i, y + tops[i], 1, 2, color);
        }
    }

    public static void cross(Canvas c, int x, int y, int color) {
        for (int i = 0; i < 7; i++) {
            c.fill(x + i, y + i, 2, 1, color);
            c.fill(x + 5 - i, y + i, 2, 1, color);
        }
    }

    public static void chevron(Canvas c, int x, int y, boolean right, int color) {
        for (int i = 0; i < 9; i++) {
            int d = Math.abs(i - 4);
            c.fill(x + (right ? 4 - d : d), y + i, 2, 1, color);
        }
    }
}
