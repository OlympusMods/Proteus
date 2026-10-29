package com.github.olympus.proteus.client.ui.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Consumer;

public record Effects(Effect fade, Effect slide, Effect scale, Effect spin) {
    public static final Effects NONE = new Effects(null, null, null, null);

    public record Effect(float x, float y, float amount, boolean relative, Easing easing, float speed) {
        float progress(float raw, float eased) {
            if (easing == null) {
                return eased;
            }
            return easing.apply(Math.max(0f, Math.min(1f, raw * speed)));
        }
    }

    public float alpha(float raw, float eased) {
        return fade == null ? 1f : Math.max(0f, Math.min(1f, fade.progress(raw, eased)));
    }

    public float dx(float raw, float eased, float size) {
        if (slide == null) {
            return 0f;
        }
        return (1f - slide.progress(raw, eased)) * (slide.relative() ? slide.x() * size : slide.x());
    }

    public float dy(float raw, float eased, float size) {
        if (slide == null) {
            return 0f;
        }
        return (1f - slide.progress(raw, eased)) * (slide.relative() ? slide.y() * size : slide.y());
    }

    public float scale(float raw, float eased) {
        if (scale == null) {
            return 1f;
        }
        return scale.amount() + (1f - scale.amount()) * scale.progress(raw, eased);
    }

    public float degrees(float raw, float eased) {
        if (spin == null) {
            return 0f;
        }
        return (1f - spin.progress(raw, eased)) * spin.amount();
    }

    public boolean transforms() {
        return scale != null || spin != null;
    }

    static Effects parse(JsonElement element, Consumer<String> warn) {
        if (element == null || !element.isJsonObject()) {
            return NONE;
        }
        JsonObject json = element.getAsJsonObject();
        return new Effects(
                effect(json.get("fade"), "amount", 0f, warn),
                effect(json.get("slide"), "amount", 0f, warn),
                effect(json.get("scale"), "from", 1f, warn),
                effect(json.get("spin"), "degrees", 0f, warn));
    }

    private static Effect effect(JsonElement element, String amountKey, float amountDefault, Consumer<String> warn) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            return element.getAsBoolean() ? new Effect(0f, 0f, amountDefault, false, null, 1f) : null;
        }
        JsonObject json = element.getAsJsonObject();
        Easing easing = json.has("easing") ? Easing.named(json.get("easing").getAsString(), json, warn) : null;
        return new Effect(
                number(json, "x", 0f),
                number(json, "y", 0f),
                number(json, amountKey, amountDefault),
                json.has("relative") && json.get("relative").getAsBoolean(),
                easing,
                number(json, "speed", 1f));
    }

    private static float number(JsonObject json, String key, float fallback) {
        return json.has(key) ? json.get(key).getAsFloat() : fallback;
    }
}
