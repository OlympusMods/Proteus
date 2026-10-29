package com.github.olympus.proteus.client.ui.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class Sizes {
    private final JsonObject json;

    private Sizes(JsonObject json) {
        this.json = json;
    }

    public static Sizes parse(JsonObject json) {
        return new Sizes(json.deepCopy());
    }

    private int read(String group, String key, int fallback) {
        JsonElement section = json.get(group);
        if (section == null || !section.isJsonObject()) {
            return fallback;
        }
        return number(section.getAsJsonObject().get(key), fallback);
    }

    private static int number(JsonElement value, int fallback) {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            return fallback;
        }
        return value.getAsInt();
    }

    public int get(String key, int fallback) {
        return number(json.get(key), fallback);
    }

    public int toggleWidth() {
        return Math.max(1, read("toggle", "width", 30));
    }

    public int toggleHeight() {
        return Math.max(1, read("toggle", "height", 16));
    }

    public int segmentPad() {
        return Math.max(0, read("segment", "pad", 2));
    }

    public int segmentGap() {
        return Math.max(0, read("segment", "gap", 0));
    }

    public int fieldPad() {
        return Math.max(0, read("field", "pad", 4));
    }

    public int areaPad() {
        return Math.max(0, read("area", "pad", 5));
    }

    public int tooltipPad() {
        return Math.max(0, read("tooltip", "pad", 5));
    }

    public int tooltipOffsetX() {
        return Math.max(0, read("tooltip", "offset_x", 10));
    }

    public int tooltipOffsetY() {
        return Math.max(0, read("tooltip", "offset_y", 12));
    }

    public int scrollbarWidth() {
        return Math.max(0, read("scrollbar", "width", 3));
    }

    public int toastMargin() {
        return Math.max(0, read("toast", "margin", 8));
    }

    public int toastPad() {
        return Math.max(0, read("toast", "pad", 8));
    }

    public int toastIcon() {
        return Math.max(0, read("toast", "icon", 24));
    }

    public int toastMinWidth() {
        return Math.max(1, read("toast", "min_width", 120));
    }

    public int toastMaxWidth() {
        return Math.max(toastMinWidth(), read("toast", "max_width", 320));
    }

    public int toastMaxLines() {
        return Math.max(1, read("toast", "max_lines", 4));
    }
}
