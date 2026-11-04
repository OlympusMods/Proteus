package com.github.olympus.proteus.client.ui.theme;

import com.github.olympus.proteus.client.ui.Canvas;
import com.github.olympus.proteus.client.ui.Icons;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;

public record IconStyle(boolean sprites, String pattern, String customPattern, Map<String, int[]> sizes,
        Map<String, Entry> map) {
    public record Entry(String sprite, int w, int h) {
    }

    static IconStyle parse(JsonElement element) {
        JsonObject json = element != null && element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
        boolean sprites = json.has("draw") && "sprite".equals(json.get("draw").getAsString());
        String pattern = json.has("sprite") ? json.get("sprite").getAsString() : "classic/icon/{name}";
        String customPattern = json.has("custom_sprite") ? json.get("custom_sprite").getAsString()
                : "{namespace}:classic/icon/{path}";
        Map<String, int[]> sizes = new HashMap<>();
        if (json.has("sizes") && json.get("sizes").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("sizes").entrySet()) {
                JsonArray pair = entry.getValue().getAsJsonArray();
                sizes.put(entry.getKey(), new int[] {pair.get(0).getAsInt(), pair.get(1).getAsInt()});
            }
        }
        Map<String, Entry> map = new HashMap<>();
        if (json.has("map") && json.get("map").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("map").entrySet()) {
                JsonObject icon = entry.getValue().getAsJsonObject();
                map.put(entry.getKey(), new Entry(icon.get("sprite").getAsString(),
                        icon.has("w") ? icon.get("w").getAsInt() : 8, icon.has("h") ? icon.get("h").getAsInt() : 8));
            }
        }
        return new IconStyle(sprites, pattern, customPattern, sizes, map);
    }

    void draw(Canvas c, String name, int cx, int cy, int color) {
        Entry mapped = map.get(name);
        if (mapped != null) {
            c.sprite(mapped.sprite(), cx - mapped.w() / 2, cy - mapped.h() / 2, mapped.w(), mapped.h());
            return;
        }
        if (sprites) {
            int[] size = sizes.get(name);
            if (size != null) {
                c.sprite(pattern.replace("{name}", name), cx - size[0] / 2, cy - size[1] / 2, size[0], size[1]);
                return;
            }
            Icons.Custom icon = Icons.custom(name);
            if (icon == null) {
                return;
            }
            int colon = name.indexOf(':');
            String sprite = customPattern.replace("{namespace}", name.substring(0, colon))
                    .replace("{path}", name.substring(colon + 1));
            c.sprite(sprite, cx - icon.classicWidth() / 2, cy - icon.classicHeight() / 2, icon.classicWidth(),
                    icon.classicHeight());
            return;
        }
        switch (name) {
            case "left" -> Icons.chevron(c, cx - 3, cy - 4, false, color);
            case "right" -> Icons.chevron(c, cx - 3, cy - 4, true, color);
            case "cross" -> Icons.cross(c, cx - 4, cy - 3, color);
            case "check" -> Icons.check(c, cx - 4, cy - 3, color);
            default -> {
                Icons.Custom icon = Icons.custom(name);
                if (icon != null) {
                    icon.painter().draw(c, cx, cy, color);
                }
            }
        }
    }
}
