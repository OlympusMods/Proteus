package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ui.theme.MotionSpec;
import com.github.olympus.proteus.client.ui.theme.Sizes;
import com.google.gson.JsonObject;

public interface Theme {
    String name();

    default String description() {
        return "";
    }

    void accent(int argb);

    int accentColor();

    default int defaultAccent() {
        return accentColor();
    }

    default boolean accentLocked() {
        return false;
    }

    default Sizes sizes() {
        return Sizes.parse(new JsonObject());
    }

    default MotionSpec motion() {
        return MotionSpec.parse(new JsonObject(), message -> {
        });
    }

    default int danger() {
        return 0xFFE5484D;
    }

    default int effectGood() {
        return textPrimary();
    }

    default int effectBad() {
        return danger();
    }

    default void effect(Canvas c, int x, int y, int w, int h, boolean harmful, boolean ambient, boolean inventory,
            float warn) {
        card(c, x, y, w, h);
    }

    default int toastTitle() {
        return textSecondary();
    }

    default boolean vanillaBackdrop() {
        return true;
    }

    boolean shadowText();

    boolean centeredTitle();

    boolean slidingSegments();

    int textPrimary();

    int textSecondary();

    int textDisabled();

    int titleColor();

    void backdrop(Canvas c, int width, int height, boolean inWorld);

    void window(Canvas c, int x, int y, int w, int h);

    void card(Canvas c, int x, int y, int w, int h);

    void button(Canvas c, int x, int y, int w, int h, ButtonKind kind, float hover, float press, float selected,
            boolean enabled);

    int buttonText(ButtonKind kind, float hover, float selected, boolean enabled);

    void segmentTrack(Canvas c, int x, int y, int w, int h);

    void segmentThumb(Canvas c, int x, int y, int w, int h);

    int segmentText(float selected, float hover);

    default int toggleWidth() {
        return sizes().toggleWidth();
    }

    void toggle(Canvas c, int x, int y, int h, float on, float hover, boolean enabled, String onLabel,
            String offLabel);

    void checkbox(Canvas c, int x, int y, int size, float on, float hover, boolean enabled);

    void inset(Canvas c, int x, int y, int w, int h);

    void field(Canvas c, int x, int y, int w, int h, float focus, float hover);

    void row(Canvas c, int x, int y, int w, int h, float hover, boolean highlighted);

    void progress(Canvas c, int x, int y, int w, int h, float value, double time);

    void scrollbar(Canvas c, int x, int y, int w, int h, int thumbY, int thumbH, float hover);

    void tooltip(Canvas c, int x, int y, int w, int h);

    void icon(Canvas c, String name, int centerX, int centerY, int color);

    void toast(Canvas c, int x, int y, int w, int h);

    void swatch(Canvas c, int x, int y, int size, int color, float hover, float selected);

    default void chatLine(Canvas c, int x, int y, int w, int h, float alpha) {
        c.fill(x, y, w, h, Colors.alpha(0x80000000, alpha));
    }

    default void chatInput(Canvas c, int x, int y, int w, int h) {
        c.fill(x, y, w, h, 0x80000000);
    }

    default void chatScroll(Canvas c, int x, int y, int w, int h, boolean fresh, float alpha) {
        c.fill(x, y, w, h, Colors.alpha(fresh ? 0xFFCC5533 : 0xFF3333AA, alpha));
    }
}
