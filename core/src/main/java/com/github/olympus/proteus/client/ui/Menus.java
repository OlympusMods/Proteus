package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.PauseInfo;
import com.github.olympus.proteus.client.ProteusScreen;
import com.github.olympus.proteus.client.ui.theme.Effects;
import com.github.olympus.proteus.client.ui.theme.MotionSpec;
import com.github.olympus.proteus.client.ui.theme.Moment;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Menus {
    public static final String TITLE_FLAG = "menus.title";
    public static final String PAUSE_FLAG = "menus.pause";
    public static final String SCREENS_FLAG = "menus.screens";
    public static final String ROUND_ICONS_FLAG = "menus.round_icons";

    private static final Logger LOGGER = LoggerFactory.getLogger("proteus");
    private static final Set<String> GLYPHS = Set.of("narrator.button.language", "narrator.button.accessibility",
            "modmenu.title");
    private static final Set<String> PRIMARY = Set.of("menu.singleplayer", "menu.returnToGame");
    private static final List<String> VANILLA_ONLY = List.of("net.minecraft.client.gui.screens.inventory.",
            "net.minecraft.client.gui.screens.advancements.", "net.minecraft.client.gui.screens.recipebook.");
    private static final int SLOTS = 8;
    private static final float STAGGER = 0.035f;
    private static final float ICON_SHARE = 0.6f;
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
        PAUSE,
        OTHER
    }

    public record Open(float alpha, float dx, float dy, float scale) {
    }

    private static final FadeCanvas FADE = new FadeCanvas();

    private static Gui gui = new Gui(HOST);
    private static Screen lastScreen;
    private static long lastFrame;
    private static Kind scope = Kind.NONE;
    private static Screen scopeScreen;
    private static float clock;
    private static float openRaw = 1f;
    private static final float[] SLOT_RAW = new float[SLOTS + 1];
    private static int widgetIndex;
    private static int trackX;
    private static int trackY;
    private static int trackW;
    private static int trackH;
    private static boolean broken;
    private static AbstractWidget drawn;
    private static int labelColor;

    private Menus() {
    }

    public static Kind kindOf(Screen screen) {
        if (broken) {
            return Kind.NONE;
        }
        boolean pause = screen instanceof PauseScreen shown && PauseInfo.shown(shown);
        boolean other = !(screen instanceof TitleScreen) && !(screen instanceof PauseScreen) && eligible(screen);
        return kind(screen instanceof TitleScreen, pause, other, UiPrefs.get());
    }

    static boolean eligible(Screen screen) {
        if (screen instanceof ProteusScreen || screen instanceof ChatScreen
                || screen instanceof AbstractContainerScreen<?>) {
            return false;
        }
        String name = screen.getClass().getName();
        for (String prefix : VANILLA_ONLY) {
            if (name.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    static Kind kind(boolean title, boolean pause, boolean other, UiPrefs prefs) {
        if (!prefs.surface(Surface.MENUS)) {
            return Kind.NONE;
        }
        if (title && prefs.flag(TITLE_FLAG, true)) {
            return Kind.TITLE;
        }
        if (pause && prefs.flag(PAUSE_FLAG, true)) {
            return Kind.PAUSE;
        }
        if (other && prefs.flag(SCREENS_FLAG, true)) {
            return Kind.OTHER;
        }
        return Kind.NONE;
    }

    public static boolean enter(Screen screen) {
        scope = Kind.NONE;
        scopeScreen = null;
        Kind kind = kindOf(screen);
        if (kind == Kind.NONE) {
            return false;
        }
        try {
            long now = System.nanoTime();
            Theme theme = Themes.current();
            MotionSpec motion = theme.motion();
            boolean snap = !(motion.enabled() && UiPrefs.get().animations());
            boolean fresh = screen != lastScreen;
            if (fresh) {
                lastScreen = screen;
                gui = new Gui(HOST);
                lastFrame = now;
                clock = 0f;
            }
            float dt = Math.min(0.1f, (now - lastFrame) / 1_000_000_000f);
            lastFrame = now;
            gui.begin(null, theme, 0, 0, dt, now / 1_000_000_000d, snap);
            advance(fresh, kind, motion, snap, dt);
        } catch (RuntimeException exception) {
            fail(exception);
            return false;
        }
        scope = kind;
        scopeScreen = screen;
        return true;
    }

    private static void advance(boolean fresh, Kind kind, MotionSpec motion, boolean snap, float dt) {
        boolean opens = (kind == Kind.TITLE || kind == Kind.PAUSE) && !snap;
        if (!opens) {
            openRaw = 1f;
            Arrays.fill(SLOT_RAW, 1f);
            widgetIndex = 0;
            return;
        }
        if (fresh) {
            openRaw = 0f;
            Arrays.fill(SLOT_RAW, 0f);
        }
        clock += dt;
        openRaw = motion.get("window").advance(openRaw, 1f, dt, motion.speed());
        Moment content = motion.get("content");
        for (int i = 0; i <= SLOTS; i++) {
            SLOT_RAW[i] = content.advance(SLOT_RAW[i], clock >= i * STAGGER ? 1f : 0f, dt, motion.speed());
        }
        widgetIndex = 0;
    }

    public static Open open() {
        if (scope != Kind.TITLE && scope != Kind.PAUSE) {
            return null;
        }
        int slot = Math.min(widgetIndex++, SLOTS);
        float raw = SLOT_RAW[slot];
        if (raw >= 0.999f) {
            return null;
        }
        Moment content = Themes.current().motion().get("content");
        Effects effects = content.effects();
        float shown = content.value(raw);
        return new Open(effects.alpha(raw, shown), effects.dx(raw, shown, 0f), effects.dy(raw, shown, 0f),
                effects.scale(raw, shown));
    }

    public static void exit(Screen screen) {
        if (screen != scopeScreen) {
            return;
        }
        scope = Kind.NONE;
        scopeScreen = null;
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
        scopeScreen = null;
    }

    public static boolean inWorld() {
        return Minecraft.getInstance().level != null;
    }

    public static boolean backdropAtStart() {
        return scope == Kind.TITLE || scope == Kind.PAUSE || scope == Kind.OTHER && inWorld();
    }

    public static boolean skipsBlur(Kind kind) {
        return (kind == Kind.PAUSE || kind == Kind.OTHER && inWorld()) && !keepsVanillaBlur();
    }

    public static void backdrop(Canvas canvas) {
        Theme theme = Themes.current();
        Moment window = theme.motion().get("window");
        float alpha = window.effects().alpha(openRaw, window.value(openRaw));
        FADE.bind(canvas, alpha);
        try {
            theme.backdrop(FADE, canvas.width(), canvas.height(), true);
        } finally {
            FADE.unbind();
        }
    }

    public static void backdropRegion(Canvas canvas, int x, int y, int w, int h) {
        Theme theme = Themes.current();
        canvas.clip(x, y, w, h);
        try {
            theme.backdrop(canvas, canvas.width(), canvas.height(), inWorld());
        } finally {
            canvas.unclip();
        }
    }

    public static boolean darkChrome() {
        int color = Themes.current().textPrimary();
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        return 0.299f * red + 0.587f * green + 0.114f * blue >= 128f;
    }

    public static void listBackground(Canvas canvas, int x, int y, int w, int h) {
        canvas.fill(x, y, w, h, inWorld() ? 0x59000000 : 0x66000000);
    }

    public static void separator(Canvas canvas, int x, int y, int w, int h) {
        canvas.fill(x, y + h - 1, w, 1, 0x26FFFFFF);
    }

    public static void row(Canvas canvas, Object key, int x, int y, int w, int h, boolean hovered, boolean selected,
            float alpha) {
        Theme theme = Themes.current();
        float hover = gui.anim("r" + System.identityHashCode(key) + "#h", hovered ? 1f : 0f,
                theme.motion().get("row"));
        FADE.bind(canvas, alpha);
        try {
            theme.row(FADE, x, y, w, h, hover, selected);
        } finally {
            FADE.unbind();
        }
    }

    public static void scrollbar(Canvas canvas, int x, int y, int w, int h, int thumbY, int thumbH, boolean hovered) {
        Theme theme = Themes.current();
        float hover = gui.anim("sb" + x + "," + y + "#h", hovered ? 1f : 0f, theme.motion().get("hover"));
        theme.scrollbar(canvas, x, y, w, h, thumbY, thumbH, hover);
    }

    public static void slider(Canvas canvas, AbstractWidget widget, double value, float alpha) {
        Theme theme = Themes.current();
        int x = widget.getX();
        int y = widget.getY();
        int w = widget.getWidth();
        int h = widget.getHeight();
        String id = "w" + System.identityHashCode(widget);
        boolean enabled = widget.active;
        float hover = gui.anim(id + "#h", enabled && widget.isHoveredOrFocused() ? 1f : 0f,
                theme.motion().get("hover"));
        FADE.bind(canvas, alpha);
        try {
            theme.inset(FADE, x, y, w, h);
            theme.button(FADE, x + (int) Math.round(value * (w - 8)), y, 8, h, ButtonKind.NORMAL, hover, 0f, 0f,
                    enabled);
            String text = Draw.fit(FADE, widget.getMessage().getString(), w - 16);
            FADE.text(text, x + (w - FADE.textWidth(text)) / 2, y + (h - FADE.lineHeight()) / 2 + 1,
                    theme.buttonText(ButtonKind.NORMAL, hover, 0f, enabled), theme.shadowText());
        } finally {
            FADE.unbind();
        }
    }

    public static void checkbox(Canvas canvas, AbstractWidget widget, boolean selected, int box, boolean label,
            float alpha) {
        Theme theme = Themes.current();
        int x = widget.getX();
        int y = widget.getY() + (widget.getHeight() - box) / 2;
        String id = "w" + System.identityHashCode(widget);
        boolean enabled = widget.active;
        float hover = gui.anim(id + "#h", enabled && widget.isHoveredOrFocused() ? 1f : 0f,
                theme.motion().get("hover"));
        float on = gui.anim(id + "#o", selected ? 1f : 0f, theme.motion().get("checkbox"));
        FADE.bind(canvas, alpha);
        try {
            theme.checkbox(FADE, x, y, box, on, hover, enabled);
            if (label) {
                List<String> lines = Draw.wrap(FADE, widget.getMessage().getString(), widget.getWidth() - box - 4);
                int count = Math.min(2, lines.size());
                int textY = y + box / 2 - count * FADE.lineHeight() / 2;
                int color = enabled ? theme.textPrimary() : theme.textDisabled();
                for (int i = 0; i < count; i++) {
                    FADE.text(lines.get(i), x + box + 4, textY + i * FADE.lineHeight(), color, theme.shadowText());
                }
            }
        } finally {
            FADE.unbind();
        }
    }

    public static void field(Canvas canvas, AbstractWidget widget, boolean focused, float alpha) {
        Theme theme = Themes.current();
        String id = "w" + System.identityHashCode(widget);
        float hover = gui.anim(id + "#h", widget.isHovered() ? 1f : 0f, theme.motion().get("hover"));
        float focus = gui.anim(id + "#f", focused ? 1f : 0f, theme.motion().get("focus"));
        FADE.bind(canvas, alpha);
        try {
            theme.field(FADE, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), focus, hover);
        } finally {
            FADE.unbind();
        }
    }

    public static void tab(Canvas canvas, AbstractWidget widget, boolean selected, float alpha) {
        Theme theme = Themes.current();
        int x = widget.getX();
        int y = widget.getY();
        int w = widget.getWidth();
        int h = widget.getHeight();
        String id = "w" + System.identityHashCode(widget);
        boolean enabled = widget.active;
        float hover = gui.anim(id + "#h", enabled && widget.isHoveredOrFocused() ? 1f : 0f,
                theme.motion().get("hover"));
        float chosen = gui.anim(id + "#s", selected ? 1f : 0f, theme.motion().get("select"));
        ButtonKind kind = selected && theme.slidingSegments() ? ButtonKind.PRIMARY : ButtonKind.NORMAL;
        FADE.bind(canvas, alpha);
        try {
            theme.button(FADE, x, y, w, h, kind, hover, 0f, chosen, enabled);
            String text = Draw.fit(FADE, widget.getMessage().getString(), w - 6);
            FADE.text(text, x + (w - FADE.textWidth(text)) / 2, y + (h - FADE.lineHeight()) / 2 + 1,
                    theme.buttonText(kind, hover, chosen, enabled), theme.shadowText());
        } finally {
            FADE.unbind();
        }
    }

    public static boolean scroller(Canvas canvas, String path, int x, int y, int w, int h) {
        if (scope != Kind.OTHER) {
            return false;
        }
        if (path.equals("widget/scroller_background")) {
            trackX = x;
            trackY = y;
            trackW = w;
            trackH = h;
            return true;
        }
        if (path.equals("widget/scroller")) {
            boolean tracked = trackW > 0 && x == trackX;
            scrollbar(canvas, tracked ? trackX : x, tracked ? trackY : y, tracked ? trackW : w, tracked ? trackH : h,
                    y, h, false);
            return true;
        }
        return false;
    }

    public static boolean roundIcons() {
        return scope == Kind.OTHER && UiPrefs.get().flag(ROUND_ICONS_FLAG, true);
    }

    public static boolean themedLists() {
        return scope == Kind.OTHER && darkChrome();
    }

    public static boolean scopedTo(Screen screen) {
        return scope != Kind.NONE && scopeScreen == screen;
    }

    public static boolean texture(Canvas canvas, String path, int x, int y, int w, int h) {
        if (scope != Kind.OTHER) {
            return false;
        }
        if (path.equals("textures/gui/menu_background.png")) {
            backdropRegion(canvas, x, y, w, h);
            return true;
        }
        if (path.equals("textures/gui/inworld_menu_background.png")) {
            return true;
        }
        if (!darkChrome()) {
            return false;
        }
        if (path.endsWith("menu_list_background.png")) {
            listBackground(canvas, x, y, w, h);
            return true;
        }
        if (path.endsWith("header_separator.png") || path.endsWith("footer_separator.png")) {
            separator(canvas, x, y, w, h);
            return true;
        }
        return false;
    }

    public static int[] fitIcon(int x, int y, int w, int h, int spriteW, int spriteH) {
        int box = Math.max(1, Math.round(ICON_SHARE * Math.min(w, h)));
        float scale = box / (float) Math.max(1, Math.max(spriteW, spriteH));
        int dw = Math.max(1, Math.round(spriteW * scale));
        int dh = Math.max(1, Math.round(spriteH * scale));
        return new int[] {x + (w - dw) / 2, y + (h - dh) / 2, dw, dh};
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
