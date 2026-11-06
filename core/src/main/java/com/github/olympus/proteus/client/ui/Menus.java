package com.github.olympus.proteus.client.ui;

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

    private Menus() {
    }

    public static Kind kindOf(Screen screen) {
        if (broken) {
            return Kind.NONE;
        }
        return kind(screen instanceof TitleScreen, screen instanceof PauseScreen, UiPrefs.get());
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
        theme.backdrop(canvas, canvas.width(), canvas.height(), scope == Kind.PAUSE);
    }

    public static void button(Canvas canvas, AbstractWidget widget, boolean label) {
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
        FADE.bind(canvas, widget.getAlpha());
        try {
            theme.button(FADE, x, y, w, h, kind, hover, press, 0f, enabled);
            if (label) {
                String text = Draw.fit(FADE, widget.getMessage().getString(), w - 6);
                int color = theme.buttonText(kind, hover, 0f, enabled);
                int textY = y + (h - FADE.lineHeight()) / 2 + 1 + (press > 0.5f ? 1 : 0);
                FADE.text(text, x + (w - FADE.textWidth(text)) / 2, textY, color, theme.shadowText());
            }
        } finally {
            FADE.unbind();
        }
    }

    private static boolean isPrimary(AbstractWidget widget) {
        ComponentContents contents = widget.getMessage().getContents();
        return contents instanceof TranslatableContents translatable && PRIMARY.contains(translatable.getKey());
    }
}
