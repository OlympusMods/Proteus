package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.Menus;
import com.github.olympus.proteus.mixin.WidgetAlphaAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

public final class MenuHud {
    private static final GuiCanvas CANVAS = new GuiCanvas();

    private static int depth;
    private static AbstractWidget animated;
    private static float savedAlpha;

    private MenuHud() {
    }

    private interface Painter {
        void paint();
    }

    private static boolean paint(Painter painter) {
        if (!Menus.active()) {
            return false;
        }
        try {
            painter.paint();
            return true;
        } catch (RuntimeException | LinkageError error) {
            Menus.fail(error);
            return false;
        }
    }

    private static float alpha(AbstractWidget widget) {
        return ((WidgetAlphaAccess) widget).proteus$alpha();
    }

    public static void begin(Screen screen, GuiGraphics graphics) {
        if (ScreenAccess.current() != screen) {
            return;
        }
        try {
            depth = 0;
            animated = null;
            if (Menus.enter(screen)) {
                CANVAS.bind(graphics, Minecraft.getInstance().font, screen.width, screen.height);
            }
        } catch (RuntimeException | LinkageError error) {
            Menus.fail(error);
        }
    }

    public static void end(Screen screen) {
        Menus.exit(screen);
    }

    public static void backdrop(Screen screen) {
        if (Menus.scopedTo(screen) && Menus.backdropAtStart()) {
            paint(() -> Menus.backdrop(CANVAS));
        }
    }

    public static void backdrop(Menus.Kind kind) {
        if (Menus.scope() == kind) {
            paint(() -> Menus.backdrop(CANVAS));
        }
    }

    public static void backdropInWorld() {
        if (Menus.scope() == Menus.Kind.PAUSE || Menus.scope() == Menus.Kind.OTHER && Menus.inWorld()) {
            paint(() -> Menus.backdrop(CANVAS));
        }
    }

    public static boolean drew(AbstractWidget widget) {
        return Menus.drew(widget);
    }

    public static void widgetBegin(AbstractWidget widget) {
        depth++;
        if (depth != 1 || !Menus.active()) {
            return;
        }
        try {
            Menus.Open open = Menus.open();
            if (open == null) {
                return;
            }
            animated = widget;
            savedAlpha = alpha(widget);
            widget.setAlpha(savedAlpha * open.alpha());
            CANVAS.push();
            CANVAS.translate(open.dx(), open.dy());
            if (open.scale() != 1f) {
                float cx = widget.getX() + widget.getWidth() / 2f;
                float cy = widget.getY() + widget.getHeight() / 2f;
                CANVAS.translate(cx, cy);
                CANVAS.scale(open.scale(), open.scale());
                CANVAS.translate(-cx, -cy);
            }
        } catch (RuntimeException | LinkageError error) {
            animated = null;
            Menus.fail(error);
        }
    }

    public static void widgetEnd(AbstractWidget widget) {
        depth = Math.max(0, depth - 1);
        if (depth == 0 && animated == widget) {
            CANVAS.pop();
            widget.setAlpha(savedAlpha);
            animated = null;
        }
    }

    public static boolean button(AbstractButton button) {
        return button(button, ButtonShape.labelled(button));
    }

    public static boolean button(AbstractWidget widget, boolean label) {
        return paint(() -> Menus.button(CANVAS, widget, label, alpha(widget)));
    }

    public static boolean icon(AbstractWidget widget) {
        boolean[] drawn = new boolean[1];
        paint(() -> drawn[0] = Menus.icon(CANVAS, widget, alpha(widget)));
        return drawn[0];
    }

    public static boolean sprite(AbstractWidget widget, String sprite, int spriteW, int spriteH) {
        return paint(() -> {
            int[] box = Menus.fitIcon(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), spriteW,
                    spriteH);
            CANVAS.tinted(sprite, box[0], box[1], box[2], box[3], alpha(widget));
        });
    }

    public static boolean slider(AbstractWidget widget, double value) {
        return paint(() -> Menus.slider(CANVAS, widget, value, alpha(widget)));
    }

    public static boolean checkbox(AbstractWidget widget, boolean selected, int box, boolean label) {
        return paint(() -> Menus.checkbox(CANVAS, widget, selected, box, label, alpha(widget)));
    }

    public static boolean field(AbstractWidget widget, boolean focused) {
        return paint(() -> Menus.field(CANVAS, widget, focused, alpha(widget)));
    }

    public static boolean tab(AbstractWidget widget, boolean selected) {
        return paint(() -> Menus.tab(CANVAS, widget, selected, alpha(widget)));
    }

    public static boolean themedLists() {
        return Menus.themedLists();
    }

    public static void row(Object key, int x, int y, int w, int h, boolean hovered, boolean selected) {
        paint(() -> Menus.row(CANVAS, key, x, y, w, h, hovered, selected, 1f));
    }

    public static boolean scrollbar(int x, int y, int w, int h, int thumbY, int thumbH, boolean hovered) {
        return paint(() -> Menus.scrollbar(CANVAS, x, y, w, h, thumbY, thumbH, hovered));
    }

    public static boolean texture(String path, int x, int y, int w, int h) {
        if (!Menus.active()) {
            return false;
        }
        boolean[] handled = new boolean[1];
        paint(() -> handled[0] = Menus.texture(CANVAS, path, x, y, w, h));
        return handled[0];
    }

    public static boolean scroller(String path, int x, int y, int w, int h) {
        if (!Menus.active()) {
            return false;
        }
        boolean[] handled = new boolean[1];
        paint(() -> handled[0] = Menus.scroller(CANVAS, path, x, y, w, h));
        return handled[0];
    }

    public static boolean skipsBlur(Screen screen) {
        return Menus.skipsBlur(Menus.kindOf(screen));
    }
}
