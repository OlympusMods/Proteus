package com.github.olympus.proteus.client.ui.theme;

import com.github.olympus.proteus.client.ui.Motion;
import com.google.gson.JsonObject;
import java.util.function.Consumer;

public record Moment(boolean spring, float rate, float duration, float delay, Easing easing, Effects effects) {
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

    static Moment parse(JsonObject json, Moment fallback, Consumer<String> warn) {
        String name = json.has("easing") ? json.get("easing").getAsString() : fallback.spring() ? "spring" : null;
        Effects effects = json.has("effects") ? Effects.parse(json.get("effects"), warn) : fallback.effects();
        float delay = json.has("delay") ? json.get("delay").getAsFloat() : fallback.delay();
        if (name != null && !name.equals("spring")) {
            Easing easing = Easing.named(name, json, warn);
            if (easing != null) {
                float duration = json.has("duration") ? json.get("duration").getAsFloat()
                        : fallback.spring() ? 0.25f : fallback.duration();
                return new Moment(false, 0f, duration, delay, easing, effects);
            }
        }
        if (name == null) {
            float duration = json.has("duration") ? json.get("duration").getAsFloat() : fallback.duration();
            return new Moment(false, 0f, duration, delay, fallback.easing(), effects);
        }
        float rate = json.has("rate") ? json.get("rate").getAsFloat() : fallback.spring() ? fallback.rate() : 16f;
        return new Moment(true, rate, 0f, delay, Easing.LINEAR, effects);
    }
}
