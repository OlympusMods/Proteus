package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ui.theme.Effects;
import com.github.olympus.proteus.client.ui.theme.Moment;
import com.github.olympus.proteus.client.ui.theme.MotionSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class ChatSkin {
    public static final String MESSAGE_MOTION = "chat_message";
    public static final String OPEN_MOTION = "chat_open";
    public static final String OPACITY = "chat.opacity";
    public static final String ANIMATE = "chat.animate";
    public static final String COMPACT = "chat.compact";
    public static final String PANEL = "chat.panel";
    public static final int BUBBLE_EDGE = -4;

    private static final int SCROLL_IDLE = 0x3333AA;
    private static final int SCROLL_FRESH = 0xCC5533;
    private static final int SCROLL_EDGE = 0xCCCCCC;
    private static final int PAD_X = 4;
    private static final int PANEL_PAD = 2;
    private static final long REOPEN_NANOS = 250_000_000L;

    public record Box(int x, int y, int w, int h) {
    }

    private record Drawn(Box box, float alpha) {
    }

    public record Spacing(int padY, int gap) {
        public int step() {
            return 2 * padY + gap;
        }
    }

    public record Sample(float alpha, float dx, float dy) {
        public static final Sample NONE = new Sample(1f, 0f, 0f);
    }

    private static final Map<Object, Long> BORN = Collections.synchronizedMap(new WeakHashMap<>());
    private static final FadeCanvas FADE = new FadeCanvas();
    private static final List<Drawn> DRAWN = new ArrayList<>();
    private static List<Drawn> previous = new ArrayList<>();
    private static boolean panelDrawn;
    private static volatile boolean broken;
    private static long lastInput;
    private static long openedAt;

    private ChatSkin() {
    }

    public static boolean active() {
        return !broken && UiPrefs.get().surface(Surface.CHAT);
    }

    public static void fail() {
        broken = true;
    }

    public static void reset() {
        broken = false;
        BORN.clear();
        DRAWN.clear();
        previous = new ArrayList<>();
        panelDrawn = false;
        lastInput = 0L;
        openedAt = 0L;
    }

    public static void born(Object line) {
        if (line != null) {
            BORN.putIfAbsent(line, System.nanoTime());
        }
    }

    public static boolean panel() {
        return UiPrefs.get().flag(PANEL, false);
    }

    public static Spacing spacing() {
        if (panel()) {
            return new Spacing(0, 0);
        }
        return UiPrefs.get().flag(COMPACT, false) ? new Spacing(1, 1) : new Spacing(2, 2);
    }

    public static int lift(int groupsBelow, Spacing spacing) {
        return -(groupsBelow * spacing.step() + spacing.padY());
    }

    public static Box bubble(int bottomIndex, int lines, int rowHeight, int groupsBelow, int textWidth,
            Spacing spacing) {
        if (bottomIndex == 0 && groupsBelow == 0) {
            previous = new ArrayList<>(DRAWN);
            DRAWN.clear();
            panelDrawn = false;
        }
        int y = -(bottomIndex + lines) * rowHeight + lift(groupsBelow, spacing) - spacing.padY();
        return new Box(-PAD_X, y, textWidth + 2 * PAD_X, lines * rowHeight + 2 * spacing.padY());
    }

    public static boolean fits(Box box, int height) {
        return -box.y() <= height;
    }

    public static float bubbleAlpha(float fade, double backgroundOpacity, boolean game) {
        int color = Math.round(Motion.clamp(fade * (float) backgroundOpacity, 0f, 1f) * 255f) << 24;
        return lineAlpha(color, backgroundOpacity, game);
    }

    public static boolean animating() {
        UiPrefs prefs = UiPrefs.get();
        return prefs.animations() && prefs.flag(ANIMATE, true) && Themes.current().motion().enabled();
    }

    public static Sample message(Object line, float width, float height) {
        if (line == null || !animating()) {
            return Sample.NONE;
        }
        Long born = BORN.get(line);
        if (born == null) {
            return Sample.NONE;
        }
        MotionSpec motion = Themes.current().motion();
        double seconds = (System.nanoTime() - born) / 1_000_000_000.0;
        return sample(motion.get(MESSAGE_MOTION), seconds, motion.speed(), width, height);
    }

    public static Sample sample(Moment moment, double seconds, float speed, float width, float height) {
        Effects effects = moment.effects();
        if (effects.fade() == null && effects.slide() == null) {
            return Sample.NONE;
        }
        float raw;
        if (moment.spring()) {
            raw = 1f - (float) Math.exp(-moment.rate() * speed * seconds);
            if (raw > 0.995f) {
                return Sample.NONE;
            }
        } else {
            float total = moment.duration() + moment.delay();
            raw = total <= 0f ? 1f : (float) Math.min(1.0, seconds * speed / total);
            if (raw >= 1f) {
                return Sample.NONE;
            }
        }
        float eased = moment.value(raw);
        return new Sample(effects.alpha(raw, eased), effects.dx(raw, eased, width), effects.dy(raw, eased, height));
    }

    public static float lineAlpha(int color, double backgroundOpacity, boolean game) {
        float raw = (color >>> 24) / 255f;
        if (game) {
            return Motion.clamp(raw * 2f, 0f, 1f);
        }
        if (backgroundOpacity < 0.02) {
            return raw > 0f ? 1f : 0f;
        }
        return Motion.clamp((float) (raw / backgroundOpacity), 0f, 1f);
    }

    public static void bubble(Canvas c, Box box, float fade, double backgroundOpacity, Sample sample, boolean slide) {
        boolean game = UiPrefs.get().option(OPACITY, 0) == 1;
        draw(c, box, bubbleAlpha(fade, backgroundOpacity, game), sample, slide);
    }

    public static void bubble(Canvas c, Box box, int color, double backgroundOpacity, Sample sample, boolean slide) {
        boolean game = UiPrefs.get().option(OPACITY, 0) == 1;
        draw(c, box, lineAlpha(color, backgroundOpacity, game), sample, slide);
    }

    private static void panel(Canvas c, Box box, float alpha, float fade) {
        DRAWN.add(new Drawn(box, alpha));
        if (panelDrawn) {
            return;
        }
        panelDrawn = true;
        if (previous.isEmpty()) {
            return;
        }
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;
        float strongest = 0f;
        for (Drawn drawn : previous) {
            Box b = drawn.box();
            left = Math.min(left, b.x());
            top = Math.min(top, b.y());
            right = Math.max(right, b.x() + b.w());
            bottom = Math.max(bottom, b.y() + b.h());
            strongest = Math.max(strongest, drawn.alpha());
        }
        FADE.bind(c, fade);
        try {
            Themes.current().chatBubble(FADE, left, top - PANEL_PAD, right - left, bottom - top + 2 * PANEL_PAD,
                    strongest);
        } finally {
            FADE.unbind();
        }
    }

    private static void draw(Canvas c, Box box, float alpha, Sample sample, boolean slide) {
        if (panel()) {
            panel(c, box, alpha, 1f);
            return;
        }
        int dx = slide ? Math.round(sample.dx()) : 0;
        int dy = slide ? Math.round(sample.dy()) : 0;
        FADE.bind(c, sample.alpha());
        try {
            Themes.current().chatBubble(FADE, box.x() + dx, box.y() + dy, box.w(), box.h(), alpha);
        } finally {
            FADE.unbind();
        }
    }

    public static void suggest(Canvas c, int x1, int y1, int x2, int y2) {
        Themes.current().tooltip(c, Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    public static boolean fill(Canvas c, int x1, int y1, int x2, int y2, int color, double backgroundOpacity) {
        int x = Math.min(x1, x2);
        int y = Math.min(y1, y2);
        int w = Math.abs(x2 - x1);
        int h = Math.abs(y2 - y1);
        int rgb = color & 0xFFFFFF;
        Theme theme = Themes.current();
        if (rgb == 0 && w > 2) {
            boolean game = UiPrefs.get().option(OPACITY, 0) == 1;
            theme.chatLine(c, x, y, w, h, lineAlpha(color, backgroundOpacity, game));
            return true;
        }
        if (w == 2 && (rgb == SCROLL_IDLE || rgb == SCROLL_FRESH)) {
            theme.chatScroll(c, x, y, w, h, rgb == SCROLL_FRESH, (color >>> 24) / 255f);
            return true;
        }
        return w == 1 && rgb == SCROLL_EDGE;
    }

    public static void input(Canvas c, int x1, int y1, int x2, int y2) {
        long now = System.nanoTime();
        if (now - lastInput > REOPEN_NANOS) {
            openedAt = now;
        }
        lastInput = now;
        float alpha = 1f;
        if (animating()) {
            MotionSpec motion = Themes.current().motion();
            alpha = sample(motion.get(OPEN_MOTION), (now - openedAt) / 1_000_000_000.0, motion.speed(), 0f, 0f)
                    .alpha();
        }
        FADE.bind(c, alpha);
        try {
            Themes.current().chatInput(FADE, Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1),
                    Math.abs(y2 - y1));
        } finally {
            FADE.unbind();
        }
    }
}
