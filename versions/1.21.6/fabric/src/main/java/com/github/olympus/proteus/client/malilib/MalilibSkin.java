package com.github.olympus.proteus.client.malilib;

import com.github.olympus.proteus.client.GuiCanvas;
import com.github.olympus.proteus.client.ProteusClient;
import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.Canvas;
import com.github.olympus.proteus.client.ui.Mods;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.Themes;
import com.github.olympus.proteus.client.ui.UiPrefs;
import com.github.olympus.proteus.mixin.malilib.GuiGraphicsAccess;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.render.RenderUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public final class MalilibSkin {
    private static final GuiCanvas CANVAS = new GuiCanvas();

    private record Label(int x, int y, String text, int color, boolean shadow, ScreenRectangle clip) {
    }

    private static final List<Label> LABELS = new ArrayList<>();

    private static boolean broken;
    private static boolean armed;
    private static boolean dropdown;

    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private MalilibSkin() {
    }

    public static boolean active() {
        return !broken && MalilibScreens.current() instanceof GuiBase && Mods.active(Mods.MALILIB);
    }

    private static Canvas canvas(GuiGraphics graphics) {
        CANVAS.bind(graphics, Minecraft.getInstance().font, graphics.guiWidth(), graphics.guiHeight());
        return CANVAS;
    }

    private static boolean fail(RuntimeException exception) {
        broken = true;
        ProteusClient.LOGGER.warn("Proteus: could not draw a MaLiLib screen, using its own look", exception);
        return false;
    }

    public static boolean backdrop(GuiGraphics graphics, int width, int height) {
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

    public static int dropdownText(int color) {
        return dropdown && active() ? Themes.current().textPrimary() : color;
    }

    public static boolean rect(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        if (!dropdown || !active()) {
            return false;
        }
        if (color >>> 24 == 0x60) {
            try {
                Themes.current().row(canvas(graphics), x, y, w, h, 1f, false);
            } catch (RuntimeException exception) {
                return fail(exception);
            }
        }
        return true;
    }

    public static boolean list(GuiGraphics graphics, int x, int y, int w, int h) {
        if (!dropdown || h <= 24) {
            return false;
        }
        return inset(graphics, x, y, w, h);
    }

    private static void cover(int x, int y, int w, int h) {
        LABELS.removeIf(label -> label.x() >= x && label.x() < x + w && label.y() >= y && label.y() < y + h);
    }

    private static boolean fromWidget() {
        return WALKER.walk(frames -> frames.map(StackWalker.StackFrame::getDeclaringClass)
                .filter(type -> type != RenderUtils.class && !type.getName().startsWith("com.github.olympus"))
                .findFirst().filter(WidgetBase.class::isAssignableFrom).isPresent());
    }

    public static boolean widgetBox(GuiGraphics graphics, int x, int y, int w, int h) {
        if (!active() || !fromWidget()) {
            return false;
        }
        try {
            Themes.current().card(canvas(graphics), x - 2, y - 4, w + 4, h + 6);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static boolean window(GuiGraphics graphics, int x, int y, int w, int h) {
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

    public static boolean button(GuiGraphics graphics, int x, int y, int w, int h, boolean hovered,
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

    public static boolean label(GuiGraphics ctx, int x, int y, String text, boolean centered, boolean hovered,
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
            LABELS.add(new Label(left, y, text, color, theme.shadowText(), ((GuiGraphicsAccess) ctx).proteus$scissors().peek()));
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

    public static void flushLabels(GuiGraphics ctx) {
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

    public static boolean checkbox(GuiGraphics graphics, int x, int y, int size, boolean checked) {
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

    public static boolean inset(GuiGraphics graphics, int x, int y, int w, int h) {
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

    public static boolean scrollbar(GuiGraphics graphics, int x, int y, int w, int h, int thumbY, int thumbH,
            boolean hovered) {
        if (!active()) {
            return false;
        }
        if (dropdown && thumbH >= h - 2) {
            return true;
        }
        try {
            Themes.current().scrollbar(canvas(graphics), x, y, w, h, thumbY, thumbH, hovered ? 1f : 0f);
            return true;
        } catch (RuntimeException exception) {
            return fail(exception);
        }
    }

    public static boolean tooltip(GuiGraphics graphics, int mouseX, int mouseY, List<String> lines) {
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
