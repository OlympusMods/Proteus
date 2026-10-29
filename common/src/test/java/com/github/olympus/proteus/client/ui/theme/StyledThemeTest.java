package com.github.olympus.proteus.client.ui.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.Canvas;
import com.github.olympus.proteus.client.ui.Draw;
import com.github.olympus.proteus.client.ui.RecordingCanvas;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class StyledThemeTest {
    private final List<String> warnings = new ArrayList<>();

    private ThemeSpec spec(String text, ThemeSpec fallback) {
        JsonObject json = JsonParser.parseString(text).getAsJsonObject();
        return ThemeSpec.parse("test:theme", json, fallback, warnings::add);
    }

    private StyledTheme theme(String widgets) {
        return new StyledTheme(spec("{\"widgets\": " + widgets + "}", null));
    }

    @Test
    void roundLayerDrawsLikeDraw() {
        RecordingCanvas expected = new RecordingCanvas();
        Draw.round(expected, 10, 20, 50, 30, 4, 0xFF336699);
        RecordingCanvas actual = new RecordingCanvas();
        theme("{\"card\": {\"layers\": [{\"type\": \"round\", \"radius\": 4, \"color\": \"#FF336699\"}]}}")
                .card(actual, 10, 20, 50, 30);
        assertEquals(expected.calls(), actual.calls());
    }

    @Test
    void varsFeedLayers() {
        RecordingCanvas actual = new RecordingCanvas();
        theme("""
                {"card": {"vars": {"tint": "#FF102030", "half": "floor(w / 2)"},
                          "layers": [{"type": "fill", "w": "half", "color": "tint"}]}}
                """).card(actual, 0, 0, 21, 4);
        assertEquals(List.of("fill 0 0 10 4 #FF102030"), actual.calls());
    }

    @Test
    void whenFalseSkips() {
        RecordingCanvas actual = new RecordingCanvas();
        theme("{\"row\": {\"layers\": [{\"type\": \"fill\", \"color\": \"#FFFFFFFF\", \"when\": \"hover > 0.5\"}]}}")
                .row(actual, 0, 0, 10, 10, 0.2f, false);
        assertTrue(actual.calls().isEmpty());
    }

    @Test
    void insetShrinks() {
        RecordingCanvas actual = new RecordingCanvas();
        theme("{\"inset\": {\"layers\": [{\"type\": \"fill\", \"inset\": 2, \"color\": \"#FF000000\"}]}}")
                .inset(actual, 10, 10, 20, 10);
        assertEquals(List.of("fill 12 12 16 6 #FF000000"), actual.calls());
    }

    @Test
    void transformsAreBalanced() {
        RecordingCanvas actual = new RecordingCanvas();
        theme("{\"card\": {\"layers\": [{\"type\": \"fill\", \"rotate\": 45, \"color\": \"#FF000000\"}]}}")
                .card(actual, 0, 0, 10, 10);
        assertEquals(List.of("push", "translate 5.0 5.0", "rotate 45.0", "translate -5.0 -5.0",
                "fill 0 0 10 10 #FF000000", "pop"), actual.calls());
    }

    @Test
    void missingTextureUsesFallback() {
        RecordingCanvas actual = new RecordingCanvas();
        theme("""
                {"backdrop": {"layers": [{"type": "tiled", "texture": "a:textures/x.png",
                                          "fallback": [{"type": "fill", "color": "#FF000000"}]}]}}
                """).backdrop(actual, 4, 4, false);
        assertEquals(List.of("fill 0 0 4 4 #FF000000"), actual.calls());
        actual.calls().clear();
        actual.existing.add("a:textures/x.png");
        theme("""
                {"backdrop": {"layers": [{"type": "tiled", "texture": "a:textures/x.png",
                                          "fallback": [{"type": "fill", "color": "#FF000000"}]}]}}
                """).backdrop(actual, 4, 4, false);
        assertEquals(List.of("tiled a:textures/x.png 0 0 4 4"), actual.calls());
    }

    @Test
    void stringPropertiesTakeExpressionsAfterEquals() {
        RecordingCanvas actual = new RecordingCanvas();
        theme("{\"field\": {\"layers\": [{\"type\": \"sprite\", \"sprite\": \"=if(focus > 0.5, \\\"a\\\", \\\"b\\\")\"}]}}")
                .field(actual, 0, 0, 5, 5, 1f, 0f);
        assertEquals(List.of("sprite a 0 0 5 5"), actual.calls());
    }

    @Test
    void widgetLayerReusesAnotherWidget() {
        RecordingCanvas actual = new RecordingCanvas();
        theme("""
                {"button": {"layers": [{"type": "fill", "color": "if(press > 0.5, #FF111111, #FF222222)"}]},
                 "toggle": {"layers": [{"type": "widget", "widget": "button", "vars": {"press": 0}}]}}
                """).toggle(actual, 0, 0, 16, 1f, 0f, true, "ON", "OFF");
        assertEquals(List.of("fill 0 0 30 16 #FF222222"), actual.calls());
    }

    @Test
    void brokenLayerFallsBackToParent() {
        ThemeSpec parent = spec("{\"widgets\": {\"card\": {\"layers\": [{\"type\": \"fill\", \"color\": \"#FF0000FF\"}]}}}",
                null);
        ThemeSpec child = spec("{\"widgets\": {\"card\": {\"layers\": [{\"type\": \"fill\", \"color\": \"mix(1,\"}]}}}",
                parent);
        RecordingCanvas actual = new RecordingCanvas();
        new StyledTheme(child).card(actual, 0, 0, 2, 2);
        assertEquals(List.of("fill 0 0 2 2 #FF0000FF"), actual.calls());
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("widgets.card"), warnings.get(0));
    }

    @Test
    void runtimeErrorWarnsOnceAndFallsBack() {
        ThemeSpec parent = spec("{\"widgets\": {\"card\": {\"layers\": [{\"type\": \"fill\", \"color\": \"#FF0000FF\"}]}}}",
                null);
        ThemeSpec child = spec("{\"widgets\": {\"card\": {\"layers\": [{\"type\": \"fill\", \"color\": \"nothing\"}]}}}",
                parent);
        StyledTheme theme = new StyledTheme(child);
        RecordingCanvas actual = new RecordingCanvas();
        theme.card(actual, 0, 0, 2, 2);
        theme.card(actual, 0, 0, 2, 2);
        assertEquals(List.of("fill 0 0 2 2 #FF0000FF", "fill 0 0 2 2 #FF0000FF"), actual.calls());
        assertEquals(1, warnings.size());
    }

    @Test
    void textColorUsesVariant() {
        StyledTheme theme = new StyledTheme(spec("""
                {"colors": {"text": "#FF010101"},
                 "widgets": {"button": {"text_color": "text"}, "button.primary": {"text_color": "#FFFFFFFF"}}}
                """, null));
        assertEquals(0xFF010101, theme.buttonText(ButtonKind.NORMAL, 0f, 0f, true));
        assertEquals(0xFFFFFFFF, theme.buttonText(ButtonKind.PRIMARY, 0f, 0f, true));
    }

    @Test
    void lockedAccentIgnoresPlayer() {
        StyledTheme theme = new StyledTheme(spec("{\"accent\": {\"color\": \"#FF55FF55\", \"locked\": true}}", null));
        theme.accent(0xFF000000);
        assertEquals(0xFF55FF55, theme.accentColor());
        assertTrue(theme.accentLocked());
    }

    @Test
    void subclassOverrideWins() {
        List<String> seen = new ArrayList<>();
        StyledTheme theme = new StyledTheme(spec("{}", null)) {
            @Override
            public void button(Canvas c, int x, int y, int w, int h, ButtonKind kind, float hover, float press,
                    float selected, boolean enabled) {
                seen.add("button");
            }
        };
        theme.button(new RecordingCanvas(), 0, 0, 1, 1, ButtonKind.NORMAL, 0f, 0f, 0f, true);
        assertEquals(List.of("button"), seen);
    }
}
