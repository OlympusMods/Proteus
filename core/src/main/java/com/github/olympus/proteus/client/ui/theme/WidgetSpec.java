package com.github.olympus.proteus.client.ui.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record WidgetSpec(Map<String, Expr> vars, List<Layer> layers, Expr textColor) {
    static WidgetSpec parse(JsonObject json) {
        Map<String, Expr> vars = new LinkedHashMap<>();
        if (json.has("vars") && json.get("vars").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("vars").entrySet()) {
                vars.put(entry.getKey(), Layer.value(entry.getValue()));
            }
        }
        List<Layer> layers = json.has("layers") && json.get("layers").isJsonArray()
                ? Layer.list(json.getAsJsonArray("layers"))
                : List.of();
        return new WidgetSpec(vars, layers, Layer.value(json.get("text_color")));
    }
}
