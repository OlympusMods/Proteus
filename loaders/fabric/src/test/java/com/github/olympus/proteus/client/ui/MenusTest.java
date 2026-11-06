package com.github.olympus.proteus.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class MenusTest {
    private static UiPrefs prefs(String text) {
        return UiPrefs.fromJson(JsonParser.parseString(text).getAsJsonObject());
    }

    @Test
    void offByDefault() {
        UiPrefs prefs = prefs("{}");
        assertEquals(Menus.Kind.NONE, Menus.kind(true, false, prefs));
        assertEquals(Menus.Kind.NONE, Menus.kind(false, true, prefs));
    }

    @Test
    void bothScreensFollowTheSwitch() {
        UiPrefs prefs = prefs("{\"surfaces\": {\"menus\": true}}");
        assertEquals(Menus.Kind.TITLE, Menus.kind(true, false, prefs));
        assertEquals(Menus.Kind.PAUSE, Menus.kind(false, true, prefs));
        assertEquals(Menus.Kind.NONE, Menus.kind(false, false, prefs));
    }

    @Test
    void eachScreenHasItsOwnFlag() {
        UiPrefs prefs = prefs("{\"surfaces\": {\"menus\": true}, \"options\": {\"menus.title\": 0}}");
        assertEquals(Menus.Kind.NONE, Menus.kind(true, false, prefs));
        assertEquals(Menus.Kind.PAUSE, Menus.kind(false, true, prefs));
    }
}
