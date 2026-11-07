package com.github.olympus.proteus.client.malilib;

import com.github.olympus.proteus.client.GuiCanvas;
import com.github.olympus.proteus.client.ProteusClient;
import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.Canvas;
import com.github.olympus.proteus.client.ui.Mods;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.Themes;
import com.github.olympus.proteus.client.ui.UiPrefs;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.render.GuiContext;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public final class MalilibSkin {
    private static final GuiCanvas CANVAS = new GuiCanvas();

    private record Label(int x, int y, String text, int color, boolean shadow, ScreenRectangle clip) {
    }

    private static final List<Label> LABELS = new ArrayList<>();

    private static boolean broken;
    private static boolean armed;
    private static boolean dropdown;

    private MalilibSkin() {
    }

    public static boolean active() {
        return !broken && MalilibScreens.current() instanceof GuiBase && Mods.active(Mods.MALILIB);
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
            Minecraft minecraft = Minecraft.getInstance();
            Theme theme = Themes.current();
            boolean inWorld = minecraft.level != null;
            if (theme.vanillaBackdrop() && (UiPrefs.get().blur() || !inWorld)
                    && minecraft.options.getMenuBackgroundBlurriness() >= 1.0F) {
                graphics.blurBeforeThisStratum();
            }
            theme.backdrop(canvas(graphics), width, height, inWorld);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static void dropdown(boolean open) {
        dropdown = open;
    }

    public static boolean list(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        if (!dropdown) {
            return false;
        }
        return inset(graphics, x, y, w, h);
    }

    private static void cover(int x, int y, int w, int h) {
        LABELS.removeIf(label -> label.x() >= x && label.x() < x + w && label.y() >= y && label.y() < y + h);
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
            if (armed) {
                cover(x, y, w, h);
            }
            Themes.current().button(canvas(graphics), x, y, w, h, ButtonKind.NORMAL, hovered ? 1f : 0f, 0f, 0f,
                    enabled);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static boolean label(GuiContext ctx, int x, int y, String text, boolean centered, boolean hovered,
            boolean enabled) {
        if (!armed || !active()) {
            return false;
        }
        try {
            Theme theme = Themes.current();
            Canvas c = canvas(ctx);
            int color = theme.buttonText(ButtonKind.NORMAL, hovered ? 1f : 0f, 0f, enabled);
            int left = centered ? x - c.textWidth(text) / 2 : x;
            if (LABELS.size() > 4096) {
                LABELS.clear();
                armed = false;
                return false;
            }
            int right = left + c.textWidth(text);
            int bottom = y + c.lineHeight();
            LABELS.removeIf(other -> other.x() < right && other.x() + c.textWidth(other.text()) > left
                    && other.y() < bottom && other.y() + c.lineHeight() > y);
            LABELS.add(new Label(left, y, text, color, theme.shadowText(), ctx.peekLastScissor()));
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static void beginFrame() {
        LABELS.clear();
        armed = true;
    }

    public static void endFrame() {
        LABELS.clear();
        armed = false;
    }

    public static void flushLabels(GuiContext ctx) {
        if (LABELS.isEmpty()) {
            return;
        }
        try {
            Canvas c = canvas(ctx);
            ctx.nextStratum();
            for (Label label : LABELS) {
                ScreenRectangle clip = label.clip();
                if (clip != null) {
                    c.clip(clip.left(), clip.top(), clip.width(), clip.height());
                }
                c.text(label.text(), label.x(), label.y(), label.color(), label.shadow());
                if (clip != null) {
                    c.unclip();
                }
            }
        } catch (RuntimeException exception) {
            fail(exception);
        } finally {
            LABELS.clear();
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
