package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ui.theme.Effects;
import com.github.olympus.proteus.client.ui.theme.Moment;
import com.github.olympus.proteus.client.ui.theme.MotionSpec;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class ChatSkin {
    public static final String MESSAGE_MOTION = "chat_message";
    public static final String OPEN_MOTION = "chat_open";
    public static final String OPACITY = "chat.opacity";
    public static final String ANIMATE = "chat.animate";

    private static final int SCROLL_IDLE = 0x3333AA;
    private static final int SCROLL_FRESH = 0xCC5533;
    private static final int SCROLL_EDGE = 0xCCCCCC;
    private static final long REOPEN_NANOS = 250_000_000L;

    public record Sample(float alpha, float dx, float dy) {
        public static final Sample NONE = new Sample(1f, 0f, 0f);
    }

    private static final Map<Object, Long> BORN = Collections.synchronizedMap(new WeakHashMap<>());
    private static final FadeCanvas FADE = new FadeCanvas();
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
        lastInput = 0L;
        openedAt = 0L;
    }

    public static void born(Object line) {
        if (line != null) {
            BORN.put(line, System.nanoTime());
        }
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
