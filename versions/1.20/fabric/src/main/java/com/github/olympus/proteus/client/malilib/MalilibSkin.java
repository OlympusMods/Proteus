package com.github.olympus.proteus.client.malilib;

import com.github.olympus.proteus.client.GuiCanvas;
import com.github.olympus.proteus.client.ProteusClient;
import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.Canvas;
import com.github.olympus.proteus.client.ui.Mods;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.Themes;
import fi.dy.masa.malilib.gui.GuiBase;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class MalilibSkin {
    private static final GuiCanvas CANVAS = new GuiCanvas();

    private static GuiGraphics frame;
    private static boolean broken;
    private static boolean dropdown;

    private MalilibSkin() {
    }

    public static void beginFrame(GuiGraphics graphics) {
        frame = graphics;
    }

    public static void endFrame() {
        frame = null;
    }

    public static boolean active() {
        return !broken && frame != null && MalilibScreens.current() instanceof GuiBase && Mods.active(Mods.MALILIB);
    }

    private static Canvas canvas() {
        CANVAS.bind(frame, Minecraft.getInstance().font, frame.guiWidth(), frame.guiHeight());
        return CANVAS;
    }

    private static boolean fail(RuntimeException exception) {
        broken = true;
        ProteusClient.LOGGER.warn("Proteus: could not draw a MaLiLib screen, using its own look", exception);
        return false;
    }

    public static boolean backdrop(int width, int height) {
        if (!active()) {
            return false;
        }
        try {
            Themes.current().backdrop(canvas(), width, height, Minecraft.getInstance().level != null);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static void dropdown(boolean open) {
        dropdown = open;
    }

    public static int dropdownText(int color) {
        return dropdown && active() ? Themes.current().textPrimary() : color;
    }

    public static boolean rect(int x, int y, int w, int h, int color) {
        if (!dropdown || !active()) {
            return false;
        }
        if (color >>> 24 == 0x60) {
            try {
                Themes.current().row(canvas(), x, y, w, h, 1f, false);
            } catch (RuntimeException exception) {
                return fail(exception);
            }
        }
        return true;
    }

    public static boolean list(int x, int y, int w, int h) {
        if (!dropdown || h <= 24) {
            return false;
        }
        return inset(x, y, w, h);
    }

    public static boolean window(int x, int y, int w, int h) {
        if (!active()) {
            return false;
        }
        try {
            Themes.current().window(canvas(), x, y, w, h);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static boolean button(int x, int y, int w, int h, boolean hovered, boolean enabled) {
        if (!active()) {
            return false;
        }
        try {
            Themes.current().button(canvas(), x, y, w, h, ButtonKind.NORMAL, hovered ? 1f : 0f, 0f, 0f, enabled);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static boolean label(int x, int y, String text, boolean centered, boolean hovered, boolean enabled) {
        if (!active()) {
            return false;
        }
        try {
            Theme theme = Themes.current();
            Canvas c = canvas();
            int color = theme.buttonText(ButtonKind.NORMAL, hovered ? 1f : 0f, 0f, enabled);
            int left = centered ? x - c.textWidth(text) / 2 : x;
            c.text(text, left, y, color, theme.shadowText());
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static boolean checkbox(int x, int y, int size, boolean checked) {
        if (!active()) {
            return false;
        }
        try {
            Themes.current().checkbox(canvas(), x, y, size, checked ? 1f : 0f, 0f, true);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static boolean inset(int x, int y, int w, int h) {
        if (!active()) {
            return false;
        }
        try {
            Themes.current().inset(canvas(), x, y, w, h);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static boolean scrollbar(int x, int y, int w, int h, int thumbY, int thumbH, boolean hovered) {
        if (!active()) {
            return false;
        }
        if (dropdown && thumbH >= h - 2) {
            return true;
        }
        try {
            Themes.current().scrollbar(canvas(), x, y, w, h, thumbY, thumbH, hovered ? 1f : 0f);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static boolean tooltip(int mouseX, int mouseY, List<String> lines) {
        if (!active() || lines.isEmpty()) {
            return false;
        }
        try {
            Theme theme = Themes.current();
            Canvas c = canvas();
            int width = 0;
            for (String line : lines) {
                width = Math.max(width, c.textWidth(line));
            }
            int step = c.lineHeight() + 1;
            int w = width + 10;
            int h = lines.size() * step + 7;
            int x = Math.max(2, Math.min(mouseX + 8, c.width() - w - 2));
            int y = Math.max(2, Math.min(mouseY - h - 2, c.height() - h - 2));
            theme.tooltip(c, x, y, w, h);
            int ty = y + 4;
            for (String line : lines) {
                c.text(line, x + 5, ty, theme.textPrimary(), theme.shadowText());
                ty += step;
            }
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }
}
