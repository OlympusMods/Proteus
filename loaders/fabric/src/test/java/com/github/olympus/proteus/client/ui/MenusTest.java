package com.github.olympus.proteus.client.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class MenusTest {
    private static UiPrefs prefs(String text) {
        return UiPrefs.fromJson(JsonParser.parseString(text).getAsJsonObject());
    }

    @Test
    void offByDefault() {
        UiPrefs prefs = prefs("{}");
        assertEquals(Menus.Kind.NONE, Menus.kind(true, false, false, prefs));
        assertEquals(Menus.Kind.NONE, Menus.kind(false, true, false, prefs));
        assertEquals(Menus.Kind.NONE, Menus.kind(false, false, true, prefs));
    }

    @Test
    void everyScreenFollowsTheSwitch() {
        UiPrefs prefs = prefs("{\"surfaces\": {\"menus\": true}}");
        assertEquals(Menus.Kind.TITLE, Menus.kind(true, false, false, prefs));
        assertEquals(Menus.Kind.PAUSE, Menus.kind(false, true, false, prefs));
        assertEquals(Menus.Kind.OTHER, Menus.kind(false, false, true, prefs));
        assertEquals(Menus.Kind.NONE, Menus.kind(false, false, false, prefs));
    }

    @Test
    void eachScreenHasItsOwnFlag() {
        UiPrefs prefs = prefs(
                "{\"surfaces\": {\"menus\": true}, \"options\": {\"menus.title\": 0, \"menus.screens\": 0}}");
        assertEquals(Menus.Kind.NONE, Menus.kind(true, false, false, prefs));
        assertEquals(Menus.Kind.PAUSE, Menus.kind(false, true, false, prefs));
        assertEquals(Menus.Kind.NONE, Menus.kind(false, false, true, prefs));
    }

    @Test
    void iconsSitInsideTheButton() {
        int[] box = Menus.fitIcon(10, 20, 20, 20, 15, 15);
        assertArrayEquals(new int[] {14, 24, 12, 12}, box);
        int[] wide = Menus.fitIcon(0, 0, 20, 20, 16, 8);
        assertTrue(wide[2] <= 12 && wide[3] <= 12);
        assertEquals(10, wide[0] + wide[2] / 2);
    }
}
