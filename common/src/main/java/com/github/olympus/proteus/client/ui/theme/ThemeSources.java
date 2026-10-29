package com.github.olympus.proteus.client.ui.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class ThemeSources {
    public static final String MODERN = "proteus:modern";

    private final Map<String, JsonObject> raw;
    private final Consumer<String> warn;
    private final Map<String, JsonObject> resolved = new HashMap<>();
    private final Set<String> resolving = new LinkedHashSet<>();

    public ThemeSources(Map<String, JsonObject> raw, Consumer<String> warn) {
        this.raw = raw;
        this.warn = warn;
    }

    public Set<String> ids() {
        return raw.keySet();
    }

    public boolean has(String id) {
        return raw.containsKey(id);
    }

    public JsonObject resolve(String id) {
        JsonObject done = resolved.get(id);
        if (done != null) {
            return done;
        }
        JsonObject own = raw.get(id);
        if (own == null) {
            own = new JsonObject();
        }
        JsonObject base = new JsonObject();
        if (!id.equals(MODERN)) {
            String parent = own.has("parent") && own.get("parent").isJsonPrimitive()
                    ? own.get("parent").getAsString()
                    : MODERN;
            if (!raw.containsKey(parent)) {
                warn.accept(id + ": parent " + parent + " is not a theme, using " + MODERN);
                parent = MODERN;
            } else if (resolving.contains(parent) || parent.equals(id)) {
                warn.accept(id + ": parent " + parent + " leads back to itself, using " + MODERN);
                parent = MODERN;
            }
            resolving.add(id);
            try {
                base = resolve(parent);
            } finally {
                resolving.remove(id);
            }
        }
        JsonObject result = inherit(base, own);
        resolved.put(id, result);
        return result;
    }

    private static JsonObject inherit(JsonObject base, JsonObject own) {
        JsonObject child = own.deepCopy();
        child.remove("parent");
        JsonObject childWidgets = child.has("widgets") && child.get("widgets").isJsonObject()
                ? child.getAsJsonObject("widgets")
                : new JsonObject();
        child.remove("widgets");
        JsonObject result = merge(base, child);
        JsonObject baseWidgets = base.has("widgets") ? base.getAsJsonObject("widgets") : new JsonObject();
        JsonObject widgets = new JsonObject();
        Set<String> keys = new LinkedHashSet<>(baseWidgets.keySet());
        keys.addAll(childWidgets.keySet());
        for (String key : keys) {
            int dot = key.indexOf('.');
            if (dot < 0) {
                widgets.add(key, merge(object(baseWidgets, key), object(childWidgets, key)));
                continue;
            }
            String baseKey = key.substring(0, dot);
            JsonObject inherited = baseWidgets.has(key) ? object(baseWidgets, key) : object(baseWidgets, baseKey);
            widgets.add(key, merge(inherited, merge(object(childWidgets, baseKey), object(childWidgets, key))));
        }
        result.add("widgets", widgets);
        return result;
    }

    private static JsonObject object(JsonObject parent, String key) {
        JsonElement element = parent.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
    }

    public static JsonObject merge(JsonObject parent, JsonObject child) {
        JsonObject result = parent.deepCopy();
        for (Map.Entry<String, JsonElement> entry : child.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonNull()) {
                result.remove(key);
            } else if (value.isJsonObject() && result.has(key) && result.get(key).isJsonObject()) {
                result.add(key, merge(result.getAsJsonObject(key), value.getAsJsonObject()));
            } else {
                result.add(key, stripNulls(value));
            }
        }
        return result;
    }

    private static JsonElement stripNulls(JsonElement value) {
        if (!value.isJsonObject()) {
            return value.deepCopy();
        }
        JsonObject copy = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
            if (!entry.getValue().isJsonNull()) {
                copy.add(entry.getKey(), stripNulls(entry.getValue()));
            }
        }
        return copy;
    }
}
