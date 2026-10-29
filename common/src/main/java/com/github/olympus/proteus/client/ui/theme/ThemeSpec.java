package com.github.olympus.proteus.client.ui.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class ThemeSpec {
    private final String id;
    private final String name;
    private final String description;
    private final int accent;
    private final boolean accentLocked;
    private final Map<String, Expr> colors;
    private final boolean textShadow;
    private final String titleAlign;
    private final boolean slidingSegments;
    private final boolean vanillaBlur;
    private final Map<String, WidgetSpec> widgets;
    private final Set<String> broken;
    private final Sizes sizes;
    private final MotionSpec motion;
    private final IconStyle icons;
    private final ThemeSpec fallback;
    private final Consumer<String> warn;

    private ThemeSpec(String id, JsonObject json, ThemeSpec fallback, Consumer<String> warn) {
        this.id = id;
        this.fallback = fallback;
        this.warn = warn;
        this.name = string(json, "name", id);
        this.description = string(json, "description", "");
        JsonObject accentJson = object(json, "accent");
        int accentColor = fallback != null ? fallback.accent : 0xFF8B5CF6;
        if (accentJson.has("color")) {
            try {
                accentColor = Layer.value(accentJson.get("color")).color(Scope.of(Map.of()));
            } catch (RuntimeException exception) {
                warn.accept(id + ": accent.color: " + exception.getMessage());
            }
        }
        this.accent = accentColor;
        this.accentLocked = accentJson.has("locked") && accentJson.get("locked").getAsBoolean();
        this.colors = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : object(json, "colors").entrySet()) {
            try {
                Expr expr = Layer.value(entry.getValue());
                if (expr != null) {
                    colors.put(entry.getKey(), expr);
                }
            } catch (RuntimeException exception) {
                warn.accept(id + ": colors." + entry.getKey() + ": " + exception.getMessage());
            }
        }
        JsonObject text = object(json, "text");
        this.textShadow = text.has("shadow") && text.get("shadow").getAsBoolean();
        this.titleAlign = string(text, "title_align", "left");
        JsonObject segments = object(json, "segments");
        this.slidingSegments = !segments.has("sliding") || segments.get("sliding").getAsBoolean();
        JsonObject backdrop = object(json, "backdrop");
        this.vanillaBlur = !backdrop.has("vanilla_blur") || backdrop.get("vanilla_blur").getAsBoolean();
        this.widgets = new HashMap<>();
        this.broken = new HashSet<>();
        for (Map.Entry<String, JsonElement> entry : object(json, "widgets").entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }
            try {
                widgets.put(entry.getKey(), WidgetSpec.parse(entry.getValue().getAsJsonObject()));
            } catch (RuntimeException exception) {
                broken.add(entry.getKey());
                warn.accept(id + ": widgets." + entry.getKey() + ": " + exception.getMessage());
            }
        }
        this.sizes = Sizes.parse(object(json, "sizes"));
        this.motion = MotionSpec.parse(object(json, "motion"), message -> warn.accept(id + ": " + message));
        IconStyle iconStyle;
        try {
            iconStyle = IconStyle.parse(json.get("icons"));
        } catch (RuntimeException exception) {
            warn.accept(id + ": icons: " + exception.getMessage());
            iconStyle = fallback != null ? fallback.icons : IconStyle.parse(null);
        }
        this.icons = iconStyle;
    }

    public static ThemeSpec parse(String id, JsonObject resolved, ThemeSpec fallback, Consumer<String> warn) {
        return new ThemeSpec(id, resolved, fallback, warn);
    }

    private static JsonObject object(JsonObject json, String key) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
    }

    private static String string(JsonObject json, String key, String fallback) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : fallback;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public int accent() {
        return accent;
    }

    public boolean accentLocked() {
        return accentLocked;
    }

    public Expr color(String key) {
        return colors.get(key);
    }

    public boolean textShadow() {
        return textShadow;
    }

    public String titleAlign() {
        return titleAlign;
    }

    public boolean slidingSegments() {
        return slidingSegments;
    }

    public boolean vanillaBlur() {
        return vanillaBlur;
    }

    public boolean broken(String widget) {
        return broken.contains(widget);
    }

    public WidgetSpec widget(String widget) {
        WidgetSpec spec = widgets.get(widget);
        if (spec != null || broken.contains(widget)) {
            return spec;
        }
        int dot = widget.indexOf('.');
        if (dot < 0) {
            return null;
        }
        String base = widget.substring(0, dot);
        return broken.contains(base) ? null : widgets.get(base);
    }

    public Sizes sizes() {
        return sizes;
    }

    public MotionSpec motion() {
        return motion;
    }

    public IconStyle icons() {
        return icons;
    }

    public ThemeSpec fallback() {
        return fallback;
    }

    public Consumer<String> warn() {
        return warn;
    }
}
