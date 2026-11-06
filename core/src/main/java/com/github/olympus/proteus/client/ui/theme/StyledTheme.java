package com.github.olympus.proteus.client.ui.theme;

import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.Canvas;
import com.github.olympus.proteus.client.ui.Theme;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StyledTheme implements Theme {
    private static final int MAX_DEPTH = 8;
    private static final int MAX_WIDGET_DEPTH = 8;

    private static final class WidgetCycle extends RuntimeException {
        WidgetCycle(String name) {
            super("widget '" + name + "' draws itself");
        }
    }

    private final ThemeSpec spec;
    private final StyledTheme fallback;
    private final Set<String> failed = new HashSet<>();
    private int accent;
    private int depth;
    private int renderDepth;

    public StyledTheme(ThemeSpec spec) {
        this.spec = spec;
        this.fallback = spec.fallback() == null ? null : new StyledTheme(spec.fallback());
        this.accent = spec.accent();
        if (fallback != null) {
            fallback.accent = accent;
        }
    }

    public ThemeSpec spec() {
        return spec;
    }

    @Override
    public String name() {
        return spec.name();
    }

    @Override
    public String description() {
        return spec.description();
    }

    @Override
    public Sizes sizes() {
        return spec.sizes();
    }

    @Override
    public MotionSpec motion() {
        return spec.motion();
    }

    @Override
    public int accentColor() {
        return accent;
    }

    @Override
    public int defaultAccent() {
        return spec.accent();
    }

    @Override
    public boolean accentLocked() {
        return spec.accentLocked();
    }

    @Override
    public int danger() {
        return color("danger", 0xFFE5484D);
    }

    @Override
    public int toastTitle() {
        return color("toast_title", textSecondary());
    }

    @Override
    public int effectGood() {
        return color("effect_good", textPrimary());
    }

    @Override
    public int effectBad() {
        return color("effect_bad", danger());
    }

    @Override
    public void effect(Canvas c, int x, int y, int w, int h, boolean harmful, boolean ambient, boolean inventory,
            float warn) {
        render("effect", c, x, y, w, h, state("harmful", harmful, "ambient", ambient, "inventory", inventory,
                "warn", warn));
    }

    @Override
    public boolean vanillaBackdrop() {
        return spec.vanillaBlur();
    }

    Object colorValue(String name) {
        if (name.equals("accent")) {
            return new Expr.Color(accent);
        }
        Expr expr = spec.color(name);
        if (expr == null) {
            return fallback != null ? fallback.colorValue(name) : null;
        }
        if (depth > MAX_DEPTH) {
            throw new ExprException("colors refer to each other in a loop at '" + name + "'");
        }
        depth++;
        try {
            return expr.eval(this::colorValue);
        } finally {
            depth--;
        }
    }

    private int color(String name, int fallbackColor) {
        try {
            Object value = colorValue(name);
            if (value instanceof Expr.Color color) {
                return color.argb();
            }
        } catch (RuntimeException exception) {
            warnOnce("colors." + name, exception);
        }
        return fallbackColor;
    }

    private void warnOnce(String key, RuntimeException exception) {
        if (failed.add(key)) {
            spec.warn().accept(spec.id() + ": " + key + ": " + exception.getMessage());
        }
    }

    private static Map<String, Object> state(Object... pairs) {
        Map<String, Object> state = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            Object value = pairs[i + 1];
            if (value instanceof Boolean flag) {
                value = flag ? 1f : 0f;
            } else if (value instanceof Integer number) {
                value = (float) number;
            } else if (value instanceof Double number) {
                value = (float) (double) number;
            }
            state.put((String) pairs[i], value);
        }
        return state;
    }

    private static String variant(ButtonKind kind) {
        return kind == ButtonKind.NORMAL ? "button" : "button." + kind.name().toLowerCase(Locale.ROOT);
    }

    public void render(String name, Canvas c, int x, int y, int w, int h, Map<String, Object> state) {
        WidgetSpec widget = failed.contains("widgets." + name) || spec.broken(name) ? null : spec.widget(name);
        if (widget == null) {
            if (fallback != null) {
                fallback.render(name, c, x, y, w, h, state);
            }
            return;
        }
        if (renderDepth >= MAX_WIDGET_DEPTH) {
            throw new WidgetCycle(name);
        }
        DrawScope scope = new DrawScope(this, c, state);
        renderDepth++;
        try {
            scope.values.put("w", (float) w);
            scope.values.put("h", (float) h);
            for (Map.Entry<String, Expr> entry : widget.vars().entrySet()) {
                scope.values.put(entry.getKey(), entry.getValue().eval(scope));
            }
            for (Layer layer : widget.layers()) {
                layer.draw(scope, x, y, w, h);
            }
        } catch (RuntimeException exception) {
            if (exception instanceof WidgetCycle && renderDepth > 1) {
                throw exception;
            }
            warnOnce("widgets." + name, exception);
            if (fallback != null) {
                fallback.render(name, c, x, y, w, h, state);
            }
        } finally {
            renderDepth--;
        }
    }

    private int textColor(String name, Map<String, Object> state) {
        WidgetSpec widget = failed.contains("text." + name) || spec.broken(name) ? null : spec.widget(name);
        if (widget == null || widget.textColor() == null) {
            return fallback != null ? fallback.textColor(name, state) : textPrimary();
        }
        DrawScope scope = new DrawScope(this, null, state);
        try {
            scope.values.put("w", 0f);
            scope.values.put("h", 0f);
            for (Map.Entry<String, Expr> entry : widget.vars().entrySet()) {
                scope.values.put(entry.getKey(), entry.getValue().eval(scope));
            }
            return widget.textColor().color(scope);
        } catch (RuntimeException exception) {
            warnOnce("text." + name, exception);
            return fallback != null ? fallback.textColor(name, state) : textPrimary();
        }
    }

    @Override
    public void accent(int argb) {
        if (spec.accentLocked()) {
            return;
        }
        accent = argb;
        if (fallback != null) {
            fallback.accent = argb;
        }
    }

    @Override
    public boolean shadowText() {
        return spec.textShadow();
    }

    @Override
    public boolean centeredTitle() {
        return "center".equals(spec.titleAlign());
    }

    @Override
    public boolean slidingSegments() {
        return spec.slidingSegments();
    }

    @Override
    public int textPrimary() {
        return color("text", 0xFFFFFFFF);
    }

    @Override
    public int textSecondary() {
        return color("text_secondary", 0xFFA0A0A0);
    }

    @Override
    public int textDisabled() {
        return color("text_disabled", 0xFF707070);
    }

    @Override
    public int titleColor() {
        return color("title", 0xFFFFFFFF);
    }

    @Override
    public void backdrop(Canvas c, int width, int height, boolean inWorld) {
        render("backdrop", c, 0, 0, width, height, state("in_world", inWorld));
    }

    @Override
    public void window(Canvas c, int x, int y, int w, int h) {
        render("window", c, x, y, w, h, state());
    }

    @Override
    public void card(Canvas c, int x, int y, int w, int h) {
        render("card", c, x, y, w, h, state());
    }

    @Override
    public void button(Canvas c, int x, int y, int w, int h, ButtonKind kind, float hover, float press,
            float selected, boolean enabled) {
        render(variant(kind), c, x, y, w, h,
                state("hover", hover, "press", press, "selected", selected, "enabled", enabled));
    }

    @Override
    public int buttonText(ButtonKind kind, float hover, float selected, boolean enabled) {
        return textColor(variant(kind), state("hover", hover, "press", 0f, "selected", selected, "enabled", enabled));
    }

    @Override
    public void segmentTrack(Canvas c, int x, int y, int w, int h) {
        render("segment_track", c, x, y, w, h, state());
    }

    @Override
    public void segmentThumb(Canvas c, int x, int y, int w, int h) {
        render("segment_thumb", c, x, y, w, h, state());
    }

    @Override
    public int segmentText(float selected, float hover) {
        return textColor("segment_track", state("selected", selected, "hover", hover));
    }

    @Override
    public int toggleWidth() {
        return spec.sizes().toggleWidth();
    }

    @Override
    public void toggle(Canvas c, int x, int y, int h, float on, float hover, boolean enabled, String onLabel,
            String offLabel) {
        render("toggle", c, x, y, toggleWidth(), h, state("on", on, "hover", hover, "enabled", enabled,
                "on_label", onLabel, "off_label", offLabel));
    }

    @Override
    public void checkbox(Canvas c, int x, int y, int size, float on, float hover, boolean enabled) {
        render("checkbox", c, x, y, size, size, state("on", on, "hover", hover, "enabled", enabled));
    }

    @Override
    public void inset(Canvas c, int x, int y, int w, int h) {
        render("inset", c, x, y, w, h, state());
    }

    @Override
    public void field(Canvas c, int x, int y, int w, int h, float focus, float hover) {
        render("field", c, x, y, w, h, state("focus", focus, "hover", hover));
    }

    @Override
    public void row(Canvas c, int x, int y, int w, int h, float hover, boolean highlighted) {
        render("row", c, x, y, w, h, state("hover", hover, "highlighted", highlighted));
    }

    @Override
    public void progress(Canvas c, int x, int y, int w, int h, float value, double time) {
        render("progress", c, x, y, w, h, state("value", value, "time", time));
    }

    @Override
    public void scrollbar(Canvas c, int x, int y, int w, int h, int thumbY, int thumbH, float hover) {
        render("scrollbar", c, x, y, w, h, state("thumb_y", thumbY - y, "thumb_h", thumbH, "hover", hover));
    }

    @Override
    public void tooltip(Canvas c, int x, int y, int w, int h) {
        render("tooltip", c, x, y, w, h, state());
    }

    @Override
    public void icon(Canvas c, String name, int centerX, int centerY, int color) {
        spec.icons().draw(c, name, centerX, centerY, color);
    }

    @Override
    public void toast(Canvas c, int x, int y, int w, int h) {
        render("toast", c, x, y, w, h, state());
    }

    @Override
    public void swatch(Canvas c, int x, int y, int size, int color, float hover, float selected) {
        render("swatch", c, x, y, size, size, state("color", new Expr.Color(color), "hover", hover,
                "selected", selected));
    }
}
