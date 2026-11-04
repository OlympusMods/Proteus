package com.github.olympus.proteus.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import com.github.olympus.proteus.client.ui.theme.StyledTheme;
import com.github.olympus.proteus.client.ui.theme.ThemeSources;
import com.github.olympus.proteus.client.ui.theme.ThemeSpec;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GoldenTest {
    static Path golden(String name) {
        return Path.of(System.getProperty("proteus.golden.dir")).resolve(name + ".txt");
    }

    static List<String> record(Theme theme) {
        RecordingCanvas canvas = new RecordingCanvas();
        ThemeScript.run(theme, canvas);
        return canvas.calls();
    }

    static void compare(String name, List<String> actual) throws IOException {
        List<String> expected = Files.readAllLines(golden(name), StandardCharsets.UTF_8);
        assertFalse(expected.isEmpty());
        int count = Math.min(expected.size(), actual.size());
        for (int i = 0; i < count; i++) {
            assertEquals(expected.get(i), actual.get(i), name + " line " + (i + 1));
        }
        assertEquals(expected.size(), actual.size(), name + " line count");
    }

    static JsonObject bundled(String name) throws IOException {
        try (Reader reader = new InputStreamReader(
                GoldenTest.class.getResourceAsStream("/assets/proteus/proteus/themes/" + name + ".json"),
                StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    @Test
    void jsonThemesMatchGolden() throws IOException {
        List<String> warnings = new ArrayList<>();
        Map<String, JsonObject> raw = new HashMap<>();
        raw.put("proteus:modern", bundled("modern"));
        raw.put("proteus:classic", bundled("classic"));
        ThemeSources sources = new ThemeSources(raw, warnings::add);
        ThemeSpec modern = ThemeSpec.parse("proteus:modern", sources.resolve("proteus:modern"), null, warnings::add);
        ThemeSpec classic = ThemeSpec.parse("proteus:classic", sources.resolve("proteus:classic"), modern,
                warnings::add);
        compare("modern", record(new StyledTheme(modern)));
        compare("classic", record(new StyledTheme(classic)));
        assertEquals(List.of(), warnings);
    }

    @Test
    void liquidGlassDrawsGlassWithoutWarnings() throws IOException {
        List<String> warnings = new ArrayList<>();
        Map<String, JsonObject> raw = new HashMap<>();
        raw.put("proteus:modern", bundled("modern"));
        raw.put("proteus:liquid_glass", bundled("liquid_glass"));
        ThemeSources sources = new ThemeSources(raw, warnings::add);
        ThemeSpec modern = ThemeSpec.parse("proteus:modern", sources.resolve("proteus:modern"), null, warnings::add);
        ThemeSpec glass = ThemeSpec.parse("proteus:liquid_glass", sources.resolve("proteus:liquid_glass"), modern,
                warnings::add);
        List<String> calls = record(new StyledTheme(glass));
        assertEquals(List.of(), warnings);
        assertTrue(calls.stream().anyMatch(line -> line.startsWith("glass 10 20 300 200 r14")));
        assertTrue(calls.stream().anyMatch(line -> line.startsWith("glass 8 8 160 40 r12")));
    }

    @Test
    void aeroDrawsGlassFramesWithoutWarnings() throws IOException {
        List<String> warnings = new ArrayList<>();
        Map<String, JsonObject> raw = new HashMap<>();
        raw.put("proteus:modern", bundled("modern"));
        raw.put("proteus:aero", bundled("aero"));
        ThemeSources sources = new ThemeSources(raw, warnings::add);
        ThemeSpec modern = ThemeSpec.parse("proteus:modern", sources.resolve("proteus:modern"), null, warnings::add);
        ThemeSpec aero = ThemeSpec.parse("proteus:aero", sources.resolve("proteus:aero"), modern, warnings::add);
        List<String> calls = record(new StyledTheme(aero));
        assertEquals(List.of(), warnings);
        assertTrue(calls.stream().anyMatch(line -> line.startsWith("glass 10 20 300 200 r6")));
        assertTrue(calls.stream().anyMatch(line -> line.startsWith("glass 8 8 160 40 r6")));
    }
}
