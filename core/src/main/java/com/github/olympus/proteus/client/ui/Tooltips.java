package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ProteusClient;
import com.github.olympus.proteus.client.ui.theme.Effects;
import com.github.olympus.proteus.client.ui.theme.Moment;
import com.github.olympus.proteus.client.ui.theme.MotionSpec;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

public final class Tooltips {
    private static final String MOTION = "tooltip";
    private static final long GAP = 120_000_000L;

    private static final FadeCanvas FADE = new FadeCanvas();
    private static long start;
    private static long last;
    private static boolean failed;

    private Tooltips() {
    }

    public static boolean active() {
        return !failed && UiPrefs.get().surface(Surface.TOOLTIPS);
    }

    public static boolean shadow(boolean original) {
        return original && !(active() && !Themes.current().shadowText());
    }

    public static int textColor(int original) {
        if (original == -1 && active() && !Menus.darkChrome()) {
            return Themes.current().textPrimary();
        }
        return original;
    }

    public static FormattedCharSequence sequence(FormattedCharSequence original) {
        if (!active() || Menus.darkChrome()) {
            return original;
        }
        return sink -> original.accept((index, style, point) -> sink.accept(index, readable(style), point));
    }

    private static Style readable(Style style) {
        int base = Themes.current().textPrimary() & 0xFFFFFF;
        TextColor color = style.getColor();
        if (color == null) {
            return style.withColor(base);
        }
        int rgb = color.getValue();
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        if (Math.min(red, Math.min(green, blue)) >= 0xE0) {
            return style.withColor(base);
        }
        float luma = (0.299f * red + 0.587f * green + 0.114f * blue) / 255f;
        if (luma <= 0.4f) {
            return style;
        }
        float scale = 0.4f / luma;
        return style.withColor(Math.round(red * scale) << 16 | Math.round(green * scale) << 8
                | Math.round(blue * scale));
    }

    public static boolean background(Canvas canvas, int x, int y, int w, int h) {
        if (!active()) {
            return false;
        }
        try {
            draw(canvas, x, y, w, h);
            return true;
        } catch (RuntimeException exception) {
            failed = true;
            ProteusClient.LOGGER.warn("Proteus: could not draw a tooltip, using the vanilla ones", exception);
            return false;
        }
    }

    private static void draw(Canvas canvas, int x, int y, int w, int h) {
        Theme theme = Themes.current();
        int pad = theme.sizes().tooltipPad();
        int bx = x - pad;
        int by = y - pad;
        int bw = w + 2 * pad;
        int bh = h + 2 * pad;
        float alpha = 1f;
        MotionSpec motion = theme.motion();
        long now = System.nanoTime();
        if (now - last > GAP) {
            start = now;
        }
        last = now;
        if (motion.enabled() && UiPrefs.get().animations() && motion.has(MOTION)) {
            Moment moment = motion.get(MOTION);
            float speed = motion.speed();
            float seconds = (now - start) / 1e9f + moment.delay() / speed;
            float raw = moment.raw(seconds, speed);
            float eased = moment.value(raw);
            Effects effects = moment.effects();
            alpha = effects.alpha(raw, eased);
            if (effects.transforms()) {
                float scale = effects.scale(raw, eased);
                float cx = bx + bw / 2f;
                float cy = by + bh / 2f;
                canvas.translate(cx, cy);
                canvas.scale(scale, scale);
                canvas.translate(-cx, -cy);
            }
        }
        FADE.bind(canvas, alpha);
        try {
            theme.tooltip(FADE, bx, by, bw, bh);
        } finally {
            FADE.unbind();
        }
    }
}
