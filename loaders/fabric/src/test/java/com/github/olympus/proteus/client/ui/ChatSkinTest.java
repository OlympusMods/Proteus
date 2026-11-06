package com.github.olympus.proteus.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.olympus.proteus.client.ui.theme.Moment;
import com.github.olympus.proteus.client.ui.theme.MotionSpec;
import com.github.olympus.proteus.client.ui.theme.StyledTheme;
import com.github.olympus.proteus.client.ui.theme.ThemeSpec;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChatSkinTest {
    private static MotionSpec motion(String text) {
        JsonObject json = JsonParser.parseString(text).getAsJsonObject();
        return MotionSpec.parse(json, message -> {
        });
    }

    private static Moment message(String text) {
        return motion(text).get(ChatSkin.MESSAGE_MOTION);
    }

    @Test
    void missingEntryHasNoAnimation() {
        assertSame(ChatSkin.Sample.NONE, ChatSkin.sample(message("{}"), 0.0, 1f, 100f, 9f));
    }

    @Test
    void entryWithoutFadeOrSlideHasNoAnimation() {
        Moment moment = message("{\"chat_message\": {\"easing\": \"ease_out\", \"duration\": 0.3}}");
        assertSame(ChatSkin.Sample.NONE, ChatSkin.sample(moment, 0.0, 1f, 100f, 9f));
    }

    @Test
    void curveStartsHiddenAndFinishes() {
        Moment moment = message("""
                {"chat_message": {"easing": "linear", "duration": 0.4,
                                  "effects": {"fade": true, "slide": {"x": -10}}}}
                """);
        ChatSkin.Sample start = ChatSkin.sample(moment, 0.0, 1f, 100f, 9f);
        assertEquals(0f, start.alpha(), 0.001f);
        assertEquals(-10f, start.dx(), 0.001f);
        ChatSkin.Sample half = ChatSkin.sample(moment, 0.2, 1f, 100f, 9f);
        assertEquals(0.5f, half.alpha(), 0.001f);
        assertEquals(-5f, half.dx(), 0.001f);
        assertSame(ChatSkin.Sample.NONE, ChatSkin.sample(moment, 0.4, 1f, 100f, 9f));
    }

    @Test
    void speedShortensTheCurve() {
        Moment moment = message("""
                {"chat_message": {"easing": "linear", "duration": 0.4, "effects": {"fade": true}}}
                """);
        assertSame(ChatSkin.Sample.NONE, ChatSkin.sample(moment, 0.2, 2f, 100f, 9f));
    }

    @Test
    void relativeSlideUsesTheGivenSize() {
        Moment moment = message("""
                {"chat_message": {"easing": "linear", "duration": 1,
                                  "effects": {"slide": {"y": 1, "relative": true}}}}
                """);
        assertEquals(9f, ChatSkin.sample(moment, 0.0, 1f, 100f, 9f).dy(), 0.001f);
    }

    @Test
    void springSettles() {
        Moment moment = message("""
                {"chat_message": {"easing": "spring", "rate": 20, "effects": {"fade": true}}}
                """);
        assertTrue(ChatSkin.sample(moment, 0.01, 1f, 100f, 9f).alpha() < 0.5f);
        assertSame(ChatSkin.Sample.NONE, ChatSkin.sample(moment, 1.0, 1f, 100f, 9f));
    }

    @Test
    void themeOpacityNormalisesTheFade() {
        assertEquals(1f, ChatSkin.lineAlpha(0x80000000, 0.5, false), 0.01f);
        assertEquals(0.5f, ChatSkin.lineAlpha(0x40000000, 0.5, false), 0.01f);
        assertEquals(0f, ChatSkin.lineAlpha(0x00000000, 0.5, false), 0.01f);
        assertEquals(1f, ChatSkin.lineAlpha(0x01000000, 0.0, false), 0.01f);
    }

    @Test
    void gameOpacityScalesWithTheSetting() {
        assertEquals(1f, ChatSkin.lineAlpha(0x80000000, 0.5, true), 0.01f);
        assertEquals(0.5f, ChatSkin.lineAlpha(0x40000000, 0.5, true), 0.01f);
        assertEquals(1f, ChatSkin.lineAlpha(0xFF000000, 1.0, true), 0.01f);
    }

    @Test
    void themeWithoutChatWidgetsGetsPlainFills() {
        ThemeSpec spec = ThemeSpec.parse("test:theme", new JsonObject(), null, message -> {
        });
        StyledTheme theme = new StyledTheme(spec);
        RecordingCanvas canvas = new RecordingCanvas();
        theme.chatLine(canvas, 0, 0, 100, 9, 1f);
        theme.chatInput(canvas, 0, 0, 100, 12);
        theme.chatScroll(canvas, 0, 0, 2, 20, true, 1f);
        assertEquals(List.of("fill 0 0 100 9 #80000000", "fill 0 0 100 12 #80000000", "fill 0 0 2 20 #FFCC5533"),
                canvas.calls());
    }

    @Test
    void chatWidgetsReceiveTheirState() {
        String widgets = """
                {"widgets": {
                  "chat_line": {"layers": [{"type": "fill", "color": "alpha(#FF102030, opacity)"}]},
                  "chat_scroll": {"layers": [{"type": "fill", "color": "if(fresh > 0.5, #FFFF0000, #FF0000FF)"}]}
                }}
                """;
        ThemeSpec spec = ThemeSpec.parse("test:theme", JsonParser.parseString(widgets).getAsJsonObject(), null,
                message -> {
                });
        StyledTheme theme = new StyledTheme(spec);
        RecordingCanvas canvas = new RecordingCanvas();
        theme.chatLine(canvas, 1, 2, 30, 9, 0.5f);
        theme.chatScroll(canvas, 0, 0, 2, 10, true, 1f);
        theme.chatScroll(canvas, 0, 0, 2, 10, false, 1f);
        assertEquals(List.of("fill 1 2 30 9 #80102030", "fill 0 0 2 10 #FFFF0000", "fill 0 0 2 10 #FF0000FF"),
                canvas.calls());
    }
}
