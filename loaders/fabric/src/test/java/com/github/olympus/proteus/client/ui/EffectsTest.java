package com.github.olympus.proteus.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.olympus.proteus.client.ui.theme.Moment;
import com.github.olympus.proteus.client.ui.theme.MotionSpec;
import com.github.olympus.proteus.client.ui.theme.StyledTheme;
import com.github.olympus.proteus.client.ui.theme.ThemeSources;
import com.github.olympus.proteus.client.ui.theme.ThemeSpec;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EffectsTest {
    private static final String[] THEMES = {"modern", "liquid_glass", "aero", "win98", "classic"};

    private static EffectInfo info(String id, int ticks, boolean beneficial) {
        return new EffectInfo(id, id, 0, ticks, false, false, beneficial, true);
    }

    private static JsonObject bundled(String name) throws IOException {
        try (Reader reader = new InputStreamReader(
                EffectsTest.class.getResourceAsStream("/assets/proteus/proteus/themes/" + name + ".json"),
                StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    @Test
    void formatsLikeTheGame() {
        assertEquals("00:30", EffectTiming.format(600, 20f));
        assertEquals("01:05", EffectTiming.format(1300, 20f));
        assertEquals("1:00:00", EffectTiming.format(72000, 20f));
        assertEquals("00:15", EffectTiming.format(600, 40f));
        assertEquals("**:**", new EffectInfo("a", "A", 0, -1, true, false, true, true).time(20f));
    }

    @Test
    void levelsStopHidingAtTheFirstAmplifier() {
        assertEquals("", EffectTiming.level(0));
        assertEquals("II", EffectTiming.level(1));
        assertEquals("X", EffectTiming.level(9));
        assertEquals("11", EffectTiming.level(10));
        assertEquals("Speed II", new EffectInfo("s", "Speed", 1, 100, false, false, true, true).title());
    }

    @Test
    void blinkOnlyInTheLastTenSeconds() {
        assertEquals(1f, EffectTiming.blink(201));
        assertEquals(0f, EffectTiming.warn(201));
        assertEquals(0.5f, EffectTiming.blink(200), 0.0001f);
        assertTrue(EffectTiming.blink(10) < 0.5f);
        for (int ticks = 0; ticks <= 200; ticks++) {
            float blink = EffectTiming.blink(ticks);
            float warn = EffectTiming.warn(ticks);
            assertTrue(blink >= 0f && blink <= 1f);
            assertTrue(warn >= 0f && warn <= 1f);
        }
    }

    @Test
    void ambientAndInfiniteNeverWarn() {
        EffectInfo ambient = new EffectInfo("b", "B", 0, 50, false, true, true, true);
        EffectInfo infinite = new EffectInfo("i", "I", 0, -1, true, false, true, true);
        assertFalse(ambient.expiring());
        assertFalse(infinite.expiring());
        assertEquals(1f, ambient.blink());
        assertEquals(0f, infinite.warn());
        assertTrue(info("c", 100, true).expiring());
    }

    @Test
    void hudSitsLikeTheGame() {
        assertEquals(1, EffectLayout.hudY(true, false, false));
        assertEquals(27, EffectLayout.hudY(false, false, false));
        assertEquals(16, EffectLayout.hudY(true, true, false));
        assertEquals(37, EffectLayout.hudY(false, false, true));
        assertEquals(400 - 25, EffectLayout.hudX(400, 1));
        assertEquals(400 - 75, EffectLayout.hudX(400, 3));
    }

    @Test
    void inventoryStepsCompressPastFiveEffects() {
        assertEquals(33, EffectLayout.inventoryStep(1));
        assertEquals(33, EffectLayout.inventoryStep(5));
        assertEquals(132 / 5, EffectLayout.inventoryStep(6));
        assertEquals(132 / 9, EffectLayout.inventoryStep(10));
    }

    @Test
    void inventoryGoesCompactWhenThereIsNoRoom() {
        assertEquals(32, EffectLayout.inventoryWidth(100, false, 40, 30));
        assertEquals(32, EffectLayout.inventoryWidth(300, true, 40, 30));
        assertEquals(32 + 80 + 7, EffectLayout.inventoryWidth(300, false, 80, 30));
        assertEquals(200 - 7, EffectLayout.inventoryWidth(200, false, 400, 30));
    }

    @Test
    void newEffectsEnterAndLostOnesLeave() {
        Moment spring = Moment.spring(14f);
        EffectList list = new EffectList();
        Object owner = new Object();
        list.sync(owner, List.of(info("a", 400, true)), 0.016f, true, spring, true, spring, 1f);
        assertEquals(1f, list.entries().get(0).enter);

        list.sync(owner, List.of(info("a", 399, true), info("b", 400, true)), 0.016f, true, spring, true, spring,
                1f);
        EffectList.Entry fresh = list.entries().get(1);
        assertEquals("b", fresh.info.id());
        assertTrue(fresh.enter > 0f && fresh.enter < 1f);
        assertEquals(1f, list.entries().get(0).enter);

        list.sync(owner, List.of(info("b", 399, true)), 0.016f, true, spring, true, spring, 1f);
        assertEquals(2, list.entries().size());
        EffectList.Entry gone = list.entries().stream().filter(entry -> entry.info.id().equals("a")).findFirst()
                .orElseThrow();
        assertTrue(gone.leaving);
        assertTrue(gone.exit > 0f);
        for (int i = 0; i < 400; i++) {
            list.sync(owner, List.of(info("b", 399, true)), 0.016f, true, spring, true, spring, 1f);
        }
        assertEquals(1, list.entries().size());
    }

    @Test
    void refreshingAnEffectDoesNotReplayTheEntrance() {
        Moment spring = Moment.spring(14f);
        EffectList list = new EffectList();
        Object owner = new Object();
        list.sync(owner, List.of(info("a", 100, true)), 0.016f, true, spring, true, spring, 1f);
        for (int i = 0; i < 200; i++) {
            list.sync(owner, List.of(info("a", 100, true)), 0.016f, true, spring, true, spring, 1f);
        }
        list.sync(owner, List.of(info("a", 900, true)), 0.016f, true, spring, true, spring, 1f);
        assertEquals(1f, list.entries().get(0).enter);
        assertFalse(list.entries().get(0).leaving);
    }

    @Test
    void returningEffectCancelsItsExit() {
        Moment spring = Moment.spring(14f);
        EffectList list = new EffectList();
        Object owner = new Object();
        list.sync(owner, List.of(info("a", 100, true)), 0.016f, true, spring, true, spring, 1f);
        list.sync(owner, List.of(), 0.016f, true, spring, true, spring, 1f);
        assertTrue(list.entries().get(0).leaving);
        list.sync(owner, List.of(info("a", 100, true)), 0.016f, true, spring, true, spring, 1f);
        assertFalse(list.entries().get(0).leaving);
        assertEquals(1, list.entries().size());
    }

    @Test
    void withoutAnimationsChangesAreInstant() {
        Moment spring = Moment.spring(14f);
        EffectList list = new EffectList();
        Object owner = new Object();
        list.sync(owner, List.of(info("a", 100, true)), 0.016f, false, spring, false, spring, 1f);
        list.sync(owner, List.of(info("a", 100, true), info("b", 100, false)), 0.016f, false, spring, false,
                spring, 1f);
        assertEquals(1f, list.entries().get(1).enter);
        list.sync(owner, List.of(info("b", 100, false)), 0.016f, false, spring, false, spring, 1f);
        assertEquals(1, list.entries().size());
    }

    @Test
    void anotherPlayerStartsFresh() {
        Moment spring = Moment.spring(14f);
        EffectList list = new EffectList();
        list.sync(new Object(), List.of(info("a", 100, true)), 0.016f, true, spring, true, spring, 1f);
        list.sync(new Object(), List.of(info("b", 100, true)), 0.016f, true, spring, true, spring, 1f);
        assertEquals(1, list.entries().size());
        assertEquals(1f, list.entries().get(0).enter);
    }

    @Test
    void everyBuiltInThemeDrawsTheEffectPlate() throws IOException {
        List<String> warnings = new ArrayList<>();
        Map<String, JsonObject> raw = new HashMap<>();
        for (String name : THEMES) {
            raw.put("proteus:" + name, bundled(name));
        }
        ThemeSources sources = new ThemeSources(raw, warnings::add);
        ThemeSpec modern = ThemeSpec.parse("proteus:modern", sources.resolve("proteus:modern"), null, warnings::add);
        for (String name : THEMES) {
            ThemeSpec spec = name.equals("modern") ? modern
                    : ThemeSpec.parse("proteus:" + name, sources.resolve("proteus:" + name), modern, warnings::add);
            StyledTheme theme = new StyledTheme(spec);
            for (boolean harmful : new boolean[] {false, true}) {
                for (boolean inventory : new boolean[] {false, true}) {
                    RecordingCanvas canvas = new RecordingCanvas();
                    theme.effect(canvas, 10, 10, inventory ? 120 : 24, inventory ? 32 : 24, harmful, harmful, inventory,
                            0.5f);
                    assertFalse(canvas.calls().isEmpty(), name);
                }
            }
            assertNotNull(theme.effectGood());
            assertTrue(theme.effectBad() != theme.effectGood(), name);
        }
        assertEquals(List.of(), warnings);
    }

    @Test
    void themesAnimateEffectsOnlyWhenTheyAskTo() throws IOException {
        List<String> warnings = new ArrayList<>();
        JsonObject modern = bundled("modern");
        MotionSpec motion = MotionSpec.parse(modern.getAsJsonObject("motion"), warnings::add);
        assertTrue(motion.has("effect_in"));
        assertTrue(motion.has("effect_out"));
        assertTrue(motion.has("effect_move"));
        assertTrue(motion.has("effect_warn"));
        assertFalse(MotionSpec.parse(new JsonObject(), warnings::add).has("effect_in"));
        assertEquals(List.of(), warnings);
    }
}
