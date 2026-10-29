package com.github.olympus.proteus.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.olympus.proteus.client.ui.theme.StyledTheme;
import com.github.olympus.proteus.client.ui.theme.ThemeSpec;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ThemeRegistryTest {
    private final List<String> warnings = new ArrayList<>();
    private final ThemeRegistry registry = new ThemeRegistry();

    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    private void reload(String... pairs) {
        Map<String, JsonObject> files = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            files.put(pairs[i], json(pairs[i + 1]));
        }
        registry.reload(files, warnings::add);
    }

    private static List<String> names(List<Theme> themes) {
        List<String> names = new ArrayList<>();
        for (Theme theme : themes) {
            names.add(theme.name());
        }
        return names;
    }

    @Test
    void builtInsComeFirstWithoutPackFiles() {
        reload("z:alpha", "{\"name\": \"Alpha\"}");
        assertEquals(List.of("Modern", "Classic", "Alpha"), names(registry.all()));
        assertTrue(warnings.isEmpty(), warnings.toString());
    }

    @Test
    void packAddsTheme() {
        reload("sunset:sunset", "{\"name\": \"Sunset\", \"colors\": {\"text\": \"#FF000001\"}}");
        Theme theme = registry.get("sunset:sunset");
        assertEquals(0xFF000001, theme.textPrimary());
        assertEquals(0xFF9AA3B2, theme.textSecondary());
        assertEquals("sunset:sunset", registry.id(theme));
    }

    @Test
    void packOverridesBuiltIn() {
        reload("proteus:modern", "{\"name\": \"Modern\", \"colors\": {\"text\": \"#FF000002\"}}");
        assertEquals(0xFF000002, registry.get("proteus:modern").textPrimary());
    }

    @Test
    void packOverridesStyledModTheme() {
        registry.registerStyled("mod:neon", json("{\"name\": \"Neon\", \"colors\": {\"text\": \"#FF00FF00\", \"title\": \"#FF0000FF\"}}"),
                StyledTheme::new);
        reload("mod:neon", "{\"colors\": {\"text\": \"#FF111111\"}}");
        Theme theme = registry.get("mod:neon");
        assertEquals(0xFF111111, theme.textPrimary());
        assertEquals(0xFF0000FF, theme.titleColor());
        assertEquals("Neon", theme.name());
    }

    @Test
    void styledFactoryIsUsed() {
        List<String> made = new ArrayList<>();
        registry.registerStyled("mod:own", json("{\"name\": \"Own\"}"), spec -> {
            made.add(spec.id());
            return new StyledTheme(spec);
        });
        reload();
        assertEquals(List.of("mod:own"), made);
    }

    @Test
    void packReplacesScratchTheme() {
        Theme scratch = new StyledTheme(ThemeSpec.parse("x", json("{\"name\": \"Scratch\"}"), null, warnings::add));
        registry.register("mod:scratch", () -> scratch);
        reload();
        assertSame(scratch, registry.get("mod:scratch"));
        reload("mod:scratch", "{\"name\": \"Pack\"}");
        assertEquals("Pack", registry.get("mod:scratch").name());
    }

    @Test
    void scratchThemeIsNotAParent() {
        registry.register("mod:scratch", () -> new StyledTheme(ThemeSpec.parse("mod:scratch", json("{}"), null, warnings::add)));
        reload("a:child", "{\"parent\": \"mod:scratch\"}");
        assertEquals(0xFFECEFF4, registry.get("a:child").textPrimary());
        assertEquals(1, warnings.size());
    }

    @Test
    void malformedFileIsSkipped() {
        Map<String, JsonObject> parsed = ThemeRegistry.parseAll(Map.of(
                "a:good", "{\"name\": \"Good\"}",
                "a:bad", "{\"name\": "), warnings::add);
        assertEquals(List.of("a:good"), new ArrayList<>(parsed.keySet()));
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).startsWith("a:bad"), warnings.get(0));
    }

    @Test
    void missingSelectionFallsBackWithoutForgetting() {
        reload();
        Theme theme = registry.current("x:gone", "theme", 0xFF123456);
        assertEquals("proteus:modern", registry.id(theme));
    }

    @Test
    void currentAppliesPlayerAccent() {
        reload();
        assertEquals(0xFF123456, registry.current("proteus:modern", "blue", 0xFF123456).accentColor());
        assertEquals(0xFF8B5CF6, registry.current("proteus:modern", "theme", 0xFF123456).accentColor());
        assertEquals(0xFF55FF55, registry.current("proteus:classic", "blue", 0xFF123456).accentColor());
    }
}
