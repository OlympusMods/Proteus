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
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class MalilibSkin {
    private static final GuiCanvas CANVAS = new GuiCanvas();

    private static boolean broken;

    private MalilibSkin() {
    }

    public static boolean active() {
        return !broken && Minecraft.getInstance().gui.screen() instanceof GuiBase && Mods.active(Mods.MALILIB);
    }

    private static Canvas canvas(GuiGraphicsExtractor graphics) {
        CANVAS.bind(graphics, Minecraft.getInstance().font, graphics.guiWidth(), graphics.guiHeight());
        return CANVAS;
    }

    private static boolean fail(RuntimeException exception) {
        broken = true;
        ProteusClient.LOGGER.warn("Proteus: could not draw a MaLiLib screen, using its own look", exception);
        return false;
    }

    public static boolean backdrop(GuiGraphicsExtractor graphics, int width, int height) {
        if (!active()) {
            return false;
        }
        try {
            Themes.current().backdrop(canvas(graphics), width, height, Minecraft.getInstance().level != null);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static boolean window(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        if (!active()) {
            return false;
        }
        try {
            Themes.current().window(canvas(graphics), x, y, w, h);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static boolean button(GuiGraphicsExtractor graphics, int x, int y, int w, int h, boolean hovered,
            boolean enabled) {
        if (!active()) {
            return false;
        }
        try {
            Themes.current().button(canvas(graphics), x, y, w, h, ButtonKind.NORMAL, hovered ? 1f : 0f, 0f, 0f,
                    enabled);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static int buttonText(int original, boolean hovered, boolean enabled) {
        if (!active()) {
            return original;
        }
        try {
            return Themes.current().buttonText(ButtonKind.NORMAL, hovered ? 1f : 0f, 0f, enabled);
        } catch (RuntimeException exception) {
            fail(exception);
            return original;
        }
    }

    public static boolean checkbox(GuiGraphicsExtractor graphics, int x, int y, int size, boolean checked) {
        if (!active()) {
            return false;
        }
        try {
            Themes.current().checkbox(canvas(graphics), x, y, size, checked ? 1f : 0f, 0f, true);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static boolean inset(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        if (!active()) {
            return false;
        }
        try {
            Themes.current().inset(canvas(graphics), x, y, w, h);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static boolean scrollbar(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int thumbY, int thumbH,
            boolean hovered) {
        if (!active()) {
            return false;
        }
        try {
            Themes.current().scrollbar(canvas(graphics), x, y, w, h, thumbY, thumbH, hovered ? 1f : 0f);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static boolean tooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, List<String> lines) {
        if (!active() || lines.isEmpty()) {
            return false;
        }
        try {
            Theme theme = Themes.current();
            Canvas c = canvas(graphics);
            int width = 0;
            for (String line : lines) {
                width = Math.max(width, c.textWidth(line));
            }
            int step = c.lineHeight() + 1;
            int w = width + 10;
            int h = lines.size() * step + 7;
            int x = Math.max(2, Math.min(mouseX + 8, c.width() - w - 2));
            int y = Math.max(2, Math.min(mouseY - h - 2, c.height() - h - 2));
            graphics.nextStratum();
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
