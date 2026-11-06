package com.github.olympus.proteus.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.olympus.proteus.client.ui.theme.Easing;
import com.github.olympus.proteus.client.ui.theme.Effects;
import com.github.olympus.proteus.client.ui.theme.Moment;
import com.github.olympus.proteus.client.ui.theme.MotionSpec;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class TooltipsTest {
    private static MotionSpec motion(String name) throws IOException {
        try (Reader reader = new InputStreamReader(
                TooltipsTest.class.getResourceAsStream("/assets/proteus/proteus/themes/" + name + ".json"),
                StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return MotionSpec.parse(json.getAsJsonObject("motion"), message -> {
            });
        }
    }

    @Test
    void rawProgressRunsFromZeroToOne() throws IOException {
        Moment moment = motion("liquid_glass").get("tooltip");
        assertEquals(0f, moment.raw(0f, 1f), 0.0001f);
        assertTrue(moment.raw(0.1f, 1f) > 0f && moment.raw(0.1f, 1f) < 1f);
        assertEquals(1f, moment.raw(5f, 1f), 0.0001f);
    }

    @Test
    void instantMomentsAreAlreadyDone() {
        Moment instant = new Moment(false, 0f, 0f, 0f, Easing.LINEAR, Effects.NONE);
        assertEquals(1f, instant.raw(0f, 1f));
    }

    @Test
    void animatedThemesSlideTheEffectTipOut() throws IOException {
        for (String name : new String[] {"liquid_glass", "modern", "aero"}) {
            MotionSpec spec = motion(name);
            assertTrue(spec.has("effect_tip"), name);
            Moment tip = spec.get("effect_tip");
            float start = tip.effects().dx(0f, tip.value(0f), 40f);
            float end = tip.effects().dx(1f, tip.value(1f), 40f);
            assertTrue(start < 0f, name);
            assertEquals(0f, end, 0.0001f, name);
            assertEquals(0f, tip.effects().alpha(0f, tip.value(0f)), 0.0001f, name);
        }
    }

    @Test
    void staticThemesHaveNoTooltipMotion() throws IOException {
        for (String name : new String[] {"classic", "win98"}) {
            MotionSpec spec = motion(name);
            assertFalse(spec.enabled(), name);
            assertFalse(spec.has("effect_tip"), name);
            assertFalse(spec.has("tooltip"), name);
        }
    }
}
