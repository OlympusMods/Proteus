package com.github.olympus.proteus.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    @Test
    void recordsCurrentThemes() throws IOException {
        List<String> modern = record(new ModernTheme());
        List<String> classic = record(new ClassicTheme());
        if (!System.getProperty("proteus.golden.write", "").isEmpty()) {
            Files.write(golden("modern"), modern, StandardCharsets.UTF_8);
            Files.write(golden("classic"), classic, StandardCharsets.UTF_8);
        }
        compare("modern", modern);
        compare("classic", classic);
    }
}
