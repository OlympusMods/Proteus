package com.github.olympus.proteus.client.ui.theme;

import com.google.gson.JsonObject;
import java.util.function.Consumer;

@FunctionalInterface
public interface Easing {
    Easing LINEAR = t -> t;
    Easing EASE_IN = t -> t * t * t;
    Easing EASE_OUT = t -> {
        float k = 1f - t;
        return 1f - k * k * k;
    };
    Easing EASE_IN_OUT = t -> {
        if (t < 0.5f) {
            return 4f * t * t * t;
        }
        float k = -2f * t + 2f;
        return 1f - k * k * k / 2f;
    };
    Easing BOUNCE = t -> {
        float n = 7.5625f;
        float d = 2.75f;
        if (t < 1f / d) {
            return n * t * t;
        }
        if (t < 2f / d) {
            float u = t - 1.5f / d;
            return n * u * u + 0.75f;
        }
        if (t < 2.5f / d) {
            float u = t - 2.25f / d;
            return n * u * u + 0.9375f;
        }
        float u = t - 2.625f / d;
        return n * u * u + 0.984375f;
    };

    float apply(float t);

    static Easing back(float overshoot) {
        return t -> {
            float u = t - 1f;
            return 1f + (overshoot + 1f) * u * u * u + overshoot * u * u;
        };
    }

    static Easing elastic(float period) {
        return t -> {
            if (t <= 0f) {
                return 0f;
            }
            if (t >= 1f) {
                return 1f;
            }
            return (float) (Math.pow(2.0, -10.0 * t) * Math.sin((t - period / 4.0) * (2.0 * Math.PI) / period) + 1.0);
        };
    }

    static Easing named(String name, JsonObject params, Consumer<String> warn) {
        return switch (name) {
            case "linear" -> LINEAR;
            case "ease_in" -> EASE_IN;
            case "ease_out" -> EASE_OUT;
            case "ease_in_out" -> EASE_IN_OUT;
            case "bounce" -> BOUNCE;
            case "back" -> back(params.has("overshoot") ? params.get("overshoot").getAsFloat() : 1.4f);
            case "elastic" -> elastic(params.has("period") ? params.get("period").getAsFloat() : 0.3f);
            default -> {
                warn.accept("unknown easing '" + name + "'");
                yield null;
            }
        };
    }
}
