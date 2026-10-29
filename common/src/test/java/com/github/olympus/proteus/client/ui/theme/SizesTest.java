package com.github.olympus.proteus.client.ui.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class SizesTest {
    private static Sizes sizes(String text) {
        return Sizes.parse(JsonParser.parseString(text).getAsJsonObject());
    }

    @Test
    void defaultsMatchCurrentCode() {
        Sizes sizes = Sizes.parse(new JsonObject());
        assertEquals(30, sizes.toggleWidth());
        assertEquals(16, sizes.toggleHeight());
        assertEquals(2, sizes.segmentPad());
        assertEquals(0, sizes.segmentGap());
        assertEquals(4, sizes.fieldPad());
        assertEquals(5, sizes.areaPad());
        assertEquals(5, sizes.tooltipPad());
        assertEquals(10, sizes.tooltipOffsetX());
        assertEquals(12, sizes.tooltipOffsetY());
        assertEquals(3, sizes.scrollbarWidth());
        assertEquals(8, sizes.toastMargin());
        assertEquals(8, sizes.toastPad());
        assertEquals(24, sizes.toastIcon());
        assertEquals(120, sizes.toastMinWidth());
        assertEquals(320, sizes.toastMaxWidth());
        assertEquals(4, sizes.toastMaxLines());
    }

    @Test
    void overridesNested() {
        Sizes sizes = sizes("{\"toggle\": {\"width\": 34, \"height\": 20}, \"toast\": {\"max_lines\": 6}}");
        assertEquals(34, sizes.toggleWidth());
        assertEquals(20, sizes.toggleHeight());
        assertEquals(6, sizes.toastMaxLines());
        assertEquals(24, sizes.toastIcon());
    }

    @Test
    void modKeyFallsBack() {
        Sizes sizes = sizes("{\"mneme:entry\": 30}");
        assertEquals(30, sizes.get("mneme:entry", 24));
        assertEquals(26, sizes.get("orpheus:song_row", 26));
    }
}
