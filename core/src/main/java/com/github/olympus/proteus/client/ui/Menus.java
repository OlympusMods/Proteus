package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.PauseInfo;
import com.github.olympus.proteus.client.ui.theme.MotionSpec;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Menus {
    public static final String TITLE_FLAG = "menus.title";
    public static final String PAUSE_FLAG = "menus.pause";

    private static final Logger LOGGER = LoggerFactory.getLogger("proteus");
    private static final Set<String> GLYPHS = Set.of("narrator.button.language", "narrator.button.accessibility",
            "modmenu.title");
    private static final Set<String> PRIMARY = Set.of("menu.singleplayer", "menu.returnToGame");
    private static final UiHost HOST = new UiHost() {
        @Override
        public void close() {
        }

        @Override
        public void playClick() {
        }

        @Override
        public boolean inWorld() {
            return false;
        }
    };

    public enum Kind {
        NONE,
        TITLE,
        PAUSE
    }

    private static final FadeCanvas FADE = new FadeCanvas();

    private static Gui gui = new Gui(HOST);
    private static Screen lastScreen;
    private static long lastFrame;
    private static Kind scope = Kind.NONE;
    private static boolean broken;
    private static AbstractWidget drawn;
    private static int labelColor;

    private Menus() {
    }

    public static Kind kindOf(Screen screen) {
        if (broken) {
            return Kind.NONE;
        }
        return kind(screen instanceof TitleScreen, screen instanceof PauseScreen pause && PauseInfo.shown(pause),
                UiPrefs.get());
    }

    static Kind kind(boolean title, boolean pause, UiPrefs prefs) {
        if (!prefs.surface(Surface.MENUS)) {
            return Kind.NONE;
        }
        if (title && prefs.flag(TITLE_FLAG, true)) {
            return Kind.TITLE;
        }
        if (pause && prefs.flag(PAUSE_FLAG, true)) {
            return Kind.PAUSE;
        }
        return Kind.NONE;
    }

    public static boolean enter(Screen screen) {
        scope = Kind.NONE;
        Kind kind = kindOf(screen);
        if (kind == Kind.NONE) {
            return false;
        }
        try {
            long now = System.nanoTime();
            if (screen != lastScreen) {
                lastScreen = screen;
                gui = new Gui(HOST);
                lastFrame = now;
            }
            float dt = Math.min(0.1f, (now - lastFrame) / 1_000_000_000f);
            lastFrame = now;
            Theme theme = Themes.current();
            MotionSpec motion = theme.motion();
            gui.begin(null, theme, 0, 0, dt, now / 1_000_000_000d, !(motion.enabled() && UiPrefs.get().animations()));
        } catch (RuntimeException exception) {
            fail(exception);
            return false;
        }
        scope = kind;
        return true;
    }

    public static void exit() {
        scope = Kind.NONE;
        drawn = null;
    }

    public static boolean drew(AbstractWidget widget) {
        return scope != Kind.NONE && drawn == widget;
    }

    public static int labelColor() {
        return labelColor;
    }

    public static Kind scope() {
        return scope;
    }

    public static boolean active() {
        return scope != Kind.NONE;
    }

    public static boolean keepsVanillaBlur() {
        Theme theme = Themes.current();
        return theme.vanillaBackdrop() && UiPrefs.get().blur();
    }

    public static void fail(Throwable throwable) {
        if (!broken) {
            LOGGER.warn("Proteus: could not draw the menus, falling back to vanilla", throwable);
        }
        broken = true;
        scope = Kind.NONE;
    }

    public static void backdrop(Canvas canvas) {
        Theme theme = Themes.current();
        theme.backdrop(canvas, canvas.width(), canvas.height(), true);
    }

    public static void button(Canvas canvas, AbstractWidget widget, boolean label, float alpha) {
        Theme theme = Themes.current();
        int x = widget.getX();
        int y = widget.getY();
        int w = widget.getWidth();
        int h = widget.getHeight();
        String id = "w" + System.identityHashCode(widget);
        boolean enabled = widget.active;
        boolean hot = enabled && widget.isHoveredOrFocused();
        boolean down = hot && Minecraft.getInstance().mouseHandler.isLeftPressed();
        boolean primary = isPrimary(widget);
        float hover = gui.anim(id + "#h", hot ? 1f : 0f, theme.motion().get("hover"));
        float press = gui.anim(id + "#p", down ? 1f : 0f, theme.motion().get("press"));
        ButtonKind kind = primary ? ButtonKind.PRIMARY : ButtonKind.NORMAL;
        drawn = widget;
        FADE.bind(canvas, alpha);
        try {
            theme.button(FADE, x, y, w, h, kind, hover, press, 0f, enabled);
            int color = theme.buttonText(kind, hover, 0f, enabled);
            labelColor = Colors.alpha(color, alpha);
            if (label) {
                String text = Draw.fit(FADE, widget.getMessage().getString(), w - 6);
                int textY = y + (h - FADE.lineHeight()) / 2 + 1 + (press > 0.5f ? 1 : 0);
                FADE.text(text, x + (w - FADE.textWidth(text)) / 2, textY, color, theme.shadowText());
            }
        } finally {
            FADE.unbind();
        }
    }

    public static boolean icon(Canvas canvas, AbstractWidget widget, float alpha) {
        String glyph = glyphKey(widget);
        if (glyph == null) {
            return false;
        }
        Theme theme = Themes.current();
        button(canvas, widget, false, alpha);
        FADE.bind(canvas, alpha);
        try {
            int cx = widget.getX() + widget.getWidth() / 2;
            int cy = widget.getY() + widget.getHeight() / 2;
            paint(FADE, glyph, cx, cy, labelColorRaw(theme, widget));
        } finally {
            FADE.unbind();
        }
        return true;
    }

    private static int labelColorRaw(Theme theme, AbstractWidget widget) {
        return theme.buttonText(ButtonKind.NORMAL, widget.isHoveredOrFocused() ? 1f : 0f, 0f, widget.active);
    }

    private static String glyphKey(AbstractWidget widget) {
        ComponentContents contents = widget.getMessage().getContents();
        if (contents instanceof TranslatableContents translatable) {
            String key = translatable.getKey();
            if (GLYPHS.contains(key)) {
                return key;
            }
        }
        return null;
    }

    static void paint(Canvas c, String key, int cx, int cy, int color) {
        switch (key) {
            case "narrator.button.language" -> globe(c, cx, cy, color);
            case "narrator.button.accessibility" -> person(c, cx, cy, color);
            default -> list(c, cx, cy, color);
        }
    }

    private static void globe(Canvas c, int cx, int cy, int color) {
        for (int dy = -6; dy <= 6; dy++) {
            for (int dx = -6; dx <= 6; dx++) {
                double d = Math.sqrt(dx * dx + dy * dy);
                boolean ring = d >= 5.0 && d <= 6.3;
                boolean axis = d <= 6.3 && (dx == 0 || dy == 0);
                boolean arc = Math.abs(dx) == (int) Math.round(3.0 * Math.sqrt(Math.max(0.0, 1.0 - dy * dy / 36.0)))
                        && d <= 6.3;
                if (ring || axis || arc) {
                    c.fill(cx + dx, cy + dy, 1, 1, color);
                }
            }
        }
    }

    private static void person(Canvas c, int cx, int cy, int color) {
        c.fill(cx - 1, cy - 6, 3, 3, color);
        c.fill(cx - 5, cy - 2, 11, 2, color);
        c.fill(cx - 1, cy, 3, 3, color);
        c.fill(cx - 3, cy + 3, 2, 4, color);
        c.fill(cx + 1, cy + 3, 2, 4, color);
    }

    private static void list(Canvas c, int cx, int cy, int color) {
        for (int row = 0; row < 3; row++) {
            int y = cy - 5 + row * 4;
            c.fill(cx - 5, y, 2, 2, color);
            c.fill(cx - 1, y, 7, 2, color);
        }
    }

    private static boolean isPrimary(AbstractWidget widget) {
        ComponentContents contents = widget.getMessage().getContents();
        return contents instanceof TranslatableContents translatable && PRIMARY.contains(translatable.getKey());
    }
}
