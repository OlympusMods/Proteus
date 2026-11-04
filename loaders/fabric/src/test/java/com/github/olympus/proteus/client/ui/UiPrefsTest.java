package com.github.olympus.proteus.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class UiPrefsTest {
    private static UiPrefs prefs(String text) {
        return UiPrefs.fromJson(JsonParser.parseString(text).getAsJsonObject());
    }

    @Test
    void migratesOldStyleAndAccent() {
        UiPrefs prefs = prefs("{\"style\": \"classic\", \"accent\": 3}");
        assertEquals("proteus:classic", prefs.theme());
        assertEquals("green", prefs.accentChoice());
        assertEquals(0xFF22C55E, prefs.accentColor());
    }

    @Test
    void migratesCustomAndChroma() {
        assertEquals("custom", prefs("{\"accent\": 6}").accentChoice());
        assertEquals("chroma", prefs("{\"accent\": 7}").accentChoice());
        assertEquals("proteus:modern", prefs("{\"style\": \"modern\"}").theme());
    }

    @Test
    void missingAccentMeansTheme() {
        UiPrefs prefs = prefs("{}");
        assertEquals("theme", prefs.accentChoice());
        assertEquals("proteus:modern", prefs.theme());
    }

    @Test
    void roundTrips() {
        UiPrefs prefs = prefs("{\"theme\": \"sunset:sunset\", \"accent\": \"rose\", \"toastCorner\": 2, \"blur\": false}");
        JsonObject json = prefs.toJson();
        assertEquals("sunset:sunset", json.get("theme").getAsString());
        assertEquals("rose", json.get("accent").getAsString());
        UiPrefs again = UiPrefs.fromJson(json);
        assertEquals("sunset:sunset", again.theme());
        assertEquals("rose", again.accentChoice());
        assertEquals(2, again.toastCorner());
        assertEquals(false, again.blur());
    }

    @Test
    void unknownAccentKeyMeansTheme() {
        assertEquals("theme", prefs("{\"accent\": \"plaid\"}").accentChoice());
    }

    @Test
    void modsDefaultToEnabledAndRoundTrip() {
        assertTrue(prefs("{}").modEnabled("voicechat"));
        UiPrefs prefs = prefs("{\"mods\": {\"voicechat\": false}}");
        assertFalse(prefs.modEnabled("voicechat"));
        assertFalse(UiPrefs.fromJson(prefs.toJson()).modEnabled("voicechat"));
        assertTrue(UiPrefs.fromJson(prefs.toJson()).modEnabled("other"));
    }
}
