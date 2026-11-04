package com.github.olympus.proteus.client.ui.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.olympus.proteus.client.ui.Motion;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MotionTest {
    private final List<String> warnings = new ArrayList<>();

    private MotionSpec motion(String text) {
        JsonObject json = JsonParser.parseString(text).getAsJsonObject();
        return MotionSpec.parse(json, warnings::add);
    }

    private static float oldBack(float t) {
        float k = 1.4f;
        float u = t - 1f;
        return 1f + (k + 1f) * u * u * u + k * u * u;
    }

    @Test
    void springMatchesApproach() {
        Moment window = motion("{}").get("window");
        float raw = 0f;
        float old = 0f;
        for (int i = 0; i < 10; i++) {
            raw = window.advance(raw, 1f, 0.016f, 1f);
            old = Motion.approach(old, 1f, 0.016f, 14f);
            assertEquals(old, raw);
            assertEquals(old, window.value(raw));
        }
    }

    @Test
    void backMatchesOldToastEasing() {
        Moment in = motion("{}").get("toast_in");
        for (int i = 0; i <= 20; i++) {
            float t = i / 20f;
            assertEquals(oldBack(t), in.value(t), 1e-6f);
        }
    }

    @Test
    void easeOutMatchesCubic() {
        Easing easing = Easing.named("ease_out", new JsonObject(), warnings::add);
        for (int i = 0; i <= 20; i++) {
            float t = i / 20f;
            assertEquals(Motion.easeOutCubic(t), easing.apply(t), 1e-6f);
        }
    }

    @Test
    void curvesHitEndpoints() {
        for (String name : new String[] {"linear", "ease_in", "ease_out", "ease_in_out", "back", "bounce", "elastic"}) {
            Easing easing = Easing.named(name, new JsonObject(), warnings::add);
            assertEquals(0f, easing.apply(0f), 1e-4f, name);
            assertEquals(1f, easing.apply(1f), 1e-4f, name);
        }
        assertTrue(warnings.isEmpty());
    }

    @Test
    void curveAdvancesOverDuration() {
        Moment in = motion("{}").get("toast_in");
        float raw = in.advance(0f, 1f, 0.21f, 1f);
        assertEquals(0.5f, raw, 1e-6f);
        assertEquals(1f, in.advance(raw, 1f, 0.5f, 1f));
    }

    @Test
    void speedHalvesDuration() {
        Moment in = motion("{}").get("toast_in");
        assertEquals(1f, in.advance(0f, 1f, 0.21f, 2f), 1e-6f);
    }

    @Test
    void delayHoldsCurveAtStart() {
        Moment tooltip = motion("{}").get("tooltip");
        assertEquals(0.35f, tooltip.delay(), 1e-6f);
        float raw = tooltip.advance(0f, 1f, 0.35f, 1f);
        assertEquals(0f, tooltip.value(raw), 1e-5f);
        raw = tooltip.advance(raw, 1f, 0.06f, 1f);
        assertEquals(0.5f, tooltip.value(raw), 1e-4f);
    }

    @Test
    void contentEffectsMatchOldShell() {
        Moment content = motion("{}").get("content");
        float raw = 0.4f;
        float p = content.value(raw);
        assertEquals(Motion.easeOutCubic(raw), content.effects().alpha(raw, p), 1e-6f);
        assertEquals((1f - Motion.easeOutCubic(raw)) * 8f, content.effects().dy(raw, p, 0f), 1e-5f);
    }

    @Test
    void toastSlideIsRelative() {
        Moment in = motion("{}").get("toast_in");
        assertEquals(0.5f * 168f, in.effects().dx(0.5f, 0.5f, 168f), 1e-4f);
        assertEquals(Math.min(0.3f * 1.8f, 1f), in.effects().alpha(0.3f, in.value(0.3f)), 1e-6f);
    }

    @Test
    void scaleAndSpinEffects() {
        Moment window = motion("""
                {"window": {"easing": "linear", "duration": 0.5,
                            "effects": {"scale": {"from": 0.8}, "spin": {"degrees": 90}}}}
                """).get("window");
        assertEquals(0.9f, window.effects().scale(0.5f, 0.5f), 1e-6f);
        assertEquals(45f, window.effects().degrees(0.5f, 0.5f), 1e-6f);
        assertEquals(1f, window.effects().alpha(0.5f, 0.5f));
    }

    @Test
    void themeOverridesOneKeyAndKeepsOthers() {
        MotionSpec spec = motion("{\"hover\": {\"easing\": \"spring\", \"rate\": 4}}");
        assertEquals(4f, spec.get("hover").rate());
        assertEquals(30f, spec.get("press").rate());
    }

    @Test
    void modKeysFallBack() {
        MotionSpec spec = motion("{\"orpheus:eq\": {\"easing\": \"spring\", \"rate\": 2}}");
        assertEquals(2f, spec.get("orpheus:eq", 6f).rate());
        assertEquals(14f, spec.get("orpheus:slide", 14f).rate());
    }

    @Test
    void disabledSnaps() {
        MotionSpec spec = motion("{\"enabled\": false, \"speed\": 2}");
        assertFalse(spec.enabled());
        assertEquals(2f, spec.speed());
        assertTrue(motion("{}").enabled());
    }

    @Test
    void unknownEasingFallsBackToSpring() {
        Moment moment = motion("{\"hover\": {\"easing\": \"wobble\"}}").get("hover");
        assertTrue(moment.spring());
        assertEquals(1, warnings.size());
    }

    @Test
    void zeroOrNegativeSpeedAndRateAreClamped() {
        MotionSpec spec = motion("""
                {"speed": 0, "window": {"easing": "spring", "rate": 0},
                 "hover": {"easing": "spring", "rate": -4}, "toast_in": {"easing": "linear", "duration": -1}}
                """);
        assertTrue(spec.speed() > 0f);
        assertTrue(spec.get("window").rate() > 0f);
        assertTrue(spec.get("hover").rate() > 0f);
        assertTrue(spec.get("toast_in").duration() >= 0f);
        float raw = 0f;
        for (int i = 0; i < 2000 && raw < 1f; i++) {
            raw = spec.get("window").advance(raw, 1f, 0.05f, spec.speed());
        }
        assertEquals(1f, raw);
    }

    @Test
    void wrongTypesDoNotThrow() {
        MotionSpec spec = motion("{\"speed\": \"fast\", \"enabled\": [], \"hover\": {\"rate\": \"x\"}}");
        assertEquals(1f, spec.speed());
        assertTrue(spec.enabled());
        assertEquals(16f, spec.get("hover").rate());
        assertFalse(warnings.isEmpty());
    }

    @Test
    void exitWithoutFadeKeepsToastVisible() {
        Moment out = motion("{\"toast_out\": {\"effects\": {\"fade\": false}}}").get("toast_out");
        assertEquals(0f, out.effects().fadeOut(0.5f, 0.5f));
        Moment fading = motion("{}").get("toast_out");
        assertEquals(0.5f, fading.effects().fadeOut(0.5f, 0.5f), 1e-6f);
    }

    @Test
    void tooltipFadeWithOwnEasingBecomesVisible() {
        Moment tooltip = motion("{\"tooltip\": {\"effects\": {\"fade\": {\"easing\": \"ease_out\"}}}}").get("tooltip");
        assertEquals(1f, tooltip.alphaAfter(0.47f, 1f), 1e-5f);
        assertEquals(0f, motion("{}").get("tooltip").alphaAfter(0.2f, 1f), 1e-6f);
    }

    @Test
    void slowEffectStillEndsAtRest() {
        Moment content = motion("""
                {"content": {"effects": {"spin": {"degrees": 20, "easing": "linear", "speed": 0.5}}}}
                """).get("content");
        assertEquals(0f, content.effects().degrees(1f, 1f), 1e-6f);
        assertEquals(15f, content.effects().degrees(0.5f, 0.5f), 1e-5f);
    }
}
