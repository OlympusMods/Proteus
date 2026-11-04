package com.github.olympus.proteus.client.ui.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ThemeSourcesTest {
    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    private final List<String> warnings = new ArrayList<>();

    private ThemeSources sources(String... pairs) {
        Map<String, JsonObject> raw = new HashMap<>();
        raw.put("proteus:modern", json("""
                {"name": "Modern", "colors": {"text": "#FFFFFF", "accent": "#8B5CF6"},
                 "widgets": {"button": {"vars": {"radius": 3, "fill": "#111111"}, "layers": [{"type": "round"}]},
                             "button.primary": {"vars": {"fill": "accent"}}}}
                """));
        for (int i = 0; i < pairs.length; i += 2) {
            raw.put(pairs[i], json(pairs[i + 1]));
        }
        return new ThemeSources(raw, warnings::add);
    }

    @Test
    void childOverridesLeafOnly() {
        JsonObject resolved = sources("a:b", "{\"parent\": \"proteus:modern\", \"colors\": {\"text\": \"#000000\"}}")
                .resolve("a:b");
        assertEquals("#000000", resolved.getAsJsonObject("colors").get("text").getAsString());
        assertEquals("#8B5CF6", resolved.getAsJsonObject("colors").get("accent").getAsString());
        assertFalse(resolved.has("parent"));
    }

    @Test
    void arraysReplace() {
        JsonObject resolved = sources("a:b", "{\"widgets\": {\"button\": {\"layers\": []}}}").resolve("a:b");
        assertEquals(0, resolved.getAsJsonObject("widgets").getAsJsonObject("button").getAsJsonArray("layers").size());
    }

    @Test
    void nullRemoves() {
        JsonObject resolved = sources("a:b", "{\"colors\": {\"accent\": null}}").resolve("a:b");
        assertFalse(resolved.getAsJsonObject("colors").has("accent"));
    }

    @Test
    void variantInheritsBaseWidget() {
        JsonObject resolved = sources("a:b", "{\"widgets\": {\"button\": {\"vars\": {\"radius\": 6}}}}").resolve("a:b");
        JsonObject primary = resolved.getAsJsonObject("widgets").getAsJsonObject("button.primary");
        assertEquals(6, primary.getAsJsonObject("vars").get("radius").getAsInt());
        assertEquals("accent", primary.getAsJsonObject("vars").get("fill").getAsString());
        assertEquals(1, primary.getAsJsonArray("layers").size());
    }

    @Test
    void rootVariantInheritsItsOwnBase() {
        JsonObject primary = sources().resolve("proteus:modern").getAsJsonObject("widgets")
                .getAsJsonObject("button.primary");
        assertEquals(3, primary.getAsJsonObject("vars").get("radius").getAsInt());
        assertEquals("accent", primary.getAsJsonObject("vars").get("fill").getAsString());
    }

    @Test
    void grandchildInherits() {
        JsonObject resolved = sources(
                "a:one", "{\"colors\": {\"text\": \"#000001\"}}",
                "a:two", "{\"parent\": \"a:one\", \"name\": \"Two\"}").resolve("a:two");
        assertEquals("#000001", resolved.getAsJsonObject("colors").get("text").getAsString());
        assertEquals("Two", resolved.get("name").getAsString());
    }

    @Test
    void missingParentFallsBackToModern() {
        JsonObject resolved = sources("a:b", "{\"parent\": \"x:gone\"}").resolve("a:b");
        assertEquals("#FFFFFF", resolved.getAsJsonObject("colors").get("text").getAsString());
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("x:gone"));
    }

    @Test
    void cycleFallsBackToModern() {
        ThemeSources sources = sources(
                "a:one", "{\"parent\": \"a:two\"}",
                "a:two", "{\"parent\": \"a:one\"}");
        JsonObject resolved = sources.resolve("a:one");
        assertEquals("#FFFFFF", resolved.getAsJsonObject("colors").get("text").getAsString());
        assertFalse(warnings.isEmpty());
    }
}
