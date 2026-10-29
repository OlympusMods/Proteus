package com.github.olympus.proteus.client.ui.theme;

import com.github.olympus.proteus.client.ui.Motion;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Consumer;

public record Moment(boolean spring, float rate, float duration, float delay, Easing easing, Effects effects) {
    static final float MIN_RATE = 1f;

    public static Moment spring(float rate) {
        return new Moment(true, rate, 0f, 0f, Easing.LINEAR, Effects.NONE);
    }

    public float advance(float raw, float target, float dt, float speed) {
        if (spring) {
            return Motion.approach(raw, target, dt, rate * speed);
        }
        float total = duration + delay;
        if (total <= 0f) {
            return target;
        }
        float step = dt * speed / total;
        return raw < target ? Math.min(target, raw + step) : Math.max(target, raw - step);
    }

    public float value(float raw) {
        if (spring) {
            return raw;
        }
        float t = raw;
        if (delay > 0f && duration > 0f) {
            t = (raw * (duration + delay) - delay) / duration;
        }
        return easing.apply(Math.max(0f, Math.min(1f, t)));
    }

    public float alphaAfter(float seconds, float speed) {
        float total = duration + delay;
        if (total <= 0f) {
            return 1f;
        }
        float raw = Math.max(0f, Math.min(1f, seconds * speed / total));
        return effects.alpha(raw, value(raw));
    }

    static float number(JsonObject json, String key, float fallback) {
        if (!json.has(key)) {
            return fallback;
        }
        JsonElement value = json.get(key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new ExprException(key + " must be a number");
        }
        return value.getAsFloat();
    }

    static Moment parse(JsonObject json, Moment fallback, Consumer<String> warn) {
        String name = json.has("easing") ? json.get("easing").getAsString() : fallback.spring() ? "spring" : null;
        Effects effects = json.has("effects") ? Effects.parse(json.get("effects"), warn) : fallback.effects();
        float delay = Math.max(0f, number(json, "delay", fallback.delay()));
        if (name != null && !name.equals("spring")) {
            Easing easing = Easing.named(name, json, warn);
            if (easing != null) {
                float duration = number(json, "duration", fallback.spring() ? 0.25f : fallback.duration());
                return new Moment(false, 0f, Math.max(0f, duration), delay, easing, effects);
            }
        }
        if (name == null) {
            float duration = number(json, "duration", fallback.duration());
            return new Moment(false, 0f, Math.max(0f, duration), delay, fallback.easing(), effects);
        }
        float rate = number(json, "rate", fallback.spring() ? fallback.rate() : 16f);
        if (rate < MIN_RATE) {
            warn.accept("rate must be at least " + MIN_RATE);
            rate = MIN_RATE;
        }
        return new Moment(true, rate, 0f, delay, Easing.LINEAR, effects);
    }
}
