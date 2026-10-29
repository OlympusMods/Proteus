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
        JsonElement value = section.getAsJsonObject().get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsInt() : fallback;
    }

    public int get(String key, int fallback) {
        JsonElement value = json.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsInt() : fallback;
    }

    public int toggleWidth() {
        return read("toggle", "width", 30);
    }

    public int toggleHeight() {
        return read("toggle", "height", 16);
    }

    public int segmentPad() {
        return read("segment", "pad", 2);
    }

    public int segmentGap() {
        return read("segment", "gap", 0);
    }

    public int fieldPad() {
        return read("field", "pad", 4);
    }

    public int areaPad() {
        return read("area", "pad", 5);
    }

    public int tooltipPad() {
        return read("tooltip", "pad", 5);
    }

    public int tooltipOffsetX() {
        return read("tooltip", "offset_x", 10);
    }

    public int tooltipOffsetY() {
        return read("tooltip", "offset_y", 12);
    }

    public int scrollbarWidth() {
        return read("scrollbar", "width", 3);
    }

    public int toastMargin() {
        return read("toast", "margin", 8);
    }

    public int toastPad() {
        return read("toast", "pad", 8);
    }

    public int toastIcon() {
        return read("toast", "icon", 24);
    }

    public int toastMinWidth() {
        return read("toast", "min_width", 120);
    }

    public int toastMaxWidth() {
        return read("toast", "max_width", 320);
    }

    public int toastMaxLines() {
        return read("toast", "max_lines", 4);
    }
}
