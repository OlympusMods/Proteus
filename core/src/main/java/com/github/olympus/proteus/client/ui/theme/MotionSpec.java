package com.github.olympus.proteus.client.ui.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class MotionSpec {
    private static final String DEFAULTS = """
            {
              "window": {"easing": "spring", "rate": 14, "effects": {"fade": true, "slide": {"y": 10}}},
              "content": {"easing": "spring", "rate": 12,
                          "effects": {"fade": {"easing": "ease_out"}, "slide": {"y": 8, "easing": "ease_out"}}},
              "card_bar": {"easing": "spring", "rate": 14, "effects": {"fade": true, "slide": {"y": -6}}},
              "toast_in": {"easing": "back", "overshoot": 1.4, "duration": 0.42,
                           "effects": {"fade": {"easing": "linear", "speed": 1.8}, "slide": {"x": 1, "relative": true}}},
              "toast_out": {"easing": "ease_in", "duration": 0.3, "effects": {"fade": {"easing": "linear"}}},
              "toast_resize": {"easing": "spring", "rate": 14},
              "toast_move": {"easing": "spring", "rate": 14},
              "hover": {"easing": "spring", "rate": 16},
              "press": {"easing": "spring", "rate": 30},
              "select": {"easing": "spring", "rate": 14},
              "toggle": {"easing": "spring", "rate": 16},
              "checkbox": {"easing": "spring", "rate": 18},
              "row": {"easing": "spring", "rate": 18},
              "segment": {"easing": "spring", "rate": 16},
              "focus": {"easing": "spring", "rate": 16},
              "knob": {"easing": "spring", "rate": 16},
              "scroll": {"easing": "spring", "rate": 18},
              "tooltip": {"easing": "linear", "duration": 0.12, "delay": 0.35, "effects": {"fade": true}}
            }
            """;
    private static final float MIN_SPEED = 0.1f;
    private static final Map<String, Moment> BUILT_IN = new HashMap<>();

    static {
        JsonObject json = JsonParser.parseString(DEFAULTS).getAsJsonObject();
        for (String key : json.keySet()) {
            BUILT_IN.put(key, Moment.parse(json.getAsJsonObject(key), Moment.spring(16f), message -> {
            }));
        }
    }

    private final boolean enabled;
    private final float speed;
    private final Map<String, Moment> moments;

    private MotionSpec(boolean enabled, float speed, Map<String, Moment> moments) {
        this.enabled = enabled;
        this.speed = speed;
        this.moments = moments;
    }

    public static MotionSpec parse(JsonObject json, Consumer<String> warn) {
        boolean enabled = true;
        if (json.has("enabled")) {
            JsonElement value = json.get("enabled");
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
                enabled = value.getAsBoolean();
            } else {
                warn.accept("motion.enabled: expected true or false");
            }
        }
        float speed = 1f;
        try {
            speed = Moment.number(json, "speed", 1f);
        } catch (RuntimeException exception) {
            warn.accept("motion." + exception.getMessage());
        }
        if (speed < MIN_SPEED) {
            warn.accept("motion.speed must be at least " + MIN_SPEED);
            speed = MIN_SPEED;
        }
        Map<String, Moment> moments = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }
            String key = entry.getKey();
            Moment fallback = BUILT_IN.getOrDefault(key, Moment.spring(16f));
            try {
                moments.put(key, Moment.parse(entry.getValue().getAsJsonObject(), fallback,
                        message -> warn.accept("motion." + key + ": " + message)));
            } catch (RuntimeException exception) {
                warn.accept("motion." + key + ": " + exception.getMessage());
            }
        }
        return new MotionSpec(enabled, speed, moments);
    }

    public boolean enabled() {
        return enabled;
    }

    public float speed() {
        return speed;
    }

    public Moment get(String key) {
        Moment moment = moments.get(key);
        if (moment != null) {
            return moment;
        }
        return BUILT_IN.getOrDefault(key, Moment.spring(16f));
    }

    public Moment get(String key, float fallbackRate) {
        Moment moment = moments.get(key);
        return moment != null ? moment : BUILT_IN.getOrDefault(key, Moment.spring(fallbackRate));
    }
}
