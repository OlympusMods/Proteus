package com.github.olympus.proteus.client.ui;

import java.util.Locale;

public final class ThemeScript {
    private static final float[] STATES = {0f, 0.5f, 1f};
    private static final boolean[] FLAGS = {true, false};

    private ThemeScript() {
    }

    private static String hex(int argb) {
        return String.format(Locale.ROOT, "#%08X", argb);
    }

    public static void run(Theme theme, RecordingCanvas c) {
        Icons.register("test:icon", 10, 8, (canvas, cx, cy, color) -> canvas.fill(cx - 1, cy - 1, 2, 2, color));
        theme.accent(0xFF8B5CF6);
        c.note("shadowText " + theme.shadowText());
        c.note("centeredTitle " + theme.centeredTitle());
        c.note("slidingSegments " + theme.slidingSegments());
        c.note("toggleWidth " + theme.toggleWidth());
        c.note("textPrimary " + hex(theme.textPrimary()));
        c.note("textSecondary " + hex(theme.textSecondary()));
        c.note("textDisabled " + hex(theme.textDisabled()));
        c.note("titleColor " + hex(theme.titleColor()));

        for (boolean inWorld : FLAGS) {
            c.note("> backdrop " + inWorld);
            theme.backdrop(c, 427, 240, inWorld);
        }
        c.existing.add("proteus:textures/gui/classic/background.png");
        c.note("> backdrop textured");
        theme.backdrop(c, 427, 240, false);
        c.existing.clear();

        c.note("> window");
        theme.window(c, 10, 20, 300, 200);
        c.note("> card");
        theme.card(c, 12, 22, 100, 36);
        c.note("> inset");
        theme.inset(c, 5, 6, 40, 20);
        c.note("> tooltip");
        theme.tooltip(c, 30, 40, 80, 24);
        c.note("> toast");
        theme.toast(c, 8, 8, 160, 40);
        c.note("> segmentTrack");
        theme.segmentTrack(c, 4, 4, 120, 20);
        c.note("> segmentThumb");
        theme.segmentThumb(c, 6, 6, 40, 16);

        for (ButtonKind kind : ButtonKind.values()) {
            for (float hover : STATES) {
                for (float press : STATES) {
                    for (float selected : STATES) {
                        for (boolean enabled : FLAGS) {
                            c.note("> button " + kind + " " + hover + " " + press + " " + selected + " " + enabled);
                            theme.button(c, 10, 10, 60, 20, kind, hover, press, selected, enabled);
                            c.note("buttonText " + hex(theme.buttonText(kind, hover, selected, enabled)));
                        }
                    }
                }
            }
        }
        for (float selected : STATES) {
            for (float hover : STATES) {
                c.note("segmentText " + selected + " " + hover + " " + hex(theme.segmentText(selected, hover)));
            }
        }
        for (float on : STATES) {
            for (float hover : STATES) {
                for (boolean enabled : FLAGS) {
                    c.note("> toggle " + on + " " + hover + " " + enabled);
                    theme.toggle(c, 50, 10, 16, on, hover, enabled, "ON", "OFF");
                    c.note("> checkbox " + on + " " + hover + " " + enabled);
                    theme.checkbox(c, 4, 4, 11, on, hover, enabled);
                }
            }
        }
        for (float focus : STATES) {
            for (float hover : STATES) {
                c.note("> field " + focus + " " + hover);
                theme.field(c, 4, 4, 100, 20, focus, hover);
            }
        }
        for (float hover : STATES) {
            for (boolean highlighted : FLAGS) {
                c.note("> row " + hover + " " + highlighted);
                theme.row(c, 4, 4, 100, 20, hover, highlighted);
            }
            c.note("> scrollbar " + hover);
            theme.scrollbar(c, 200, 10, 3, 100, 30, 20, hover);
        }
        for (float value : new float[] {-1f, 0f, 0.4f, 1f}) {
            for (double time : new double[] {0.0, 0.37}) {
                c.note("> progress " + value + " " + time);
                theme.progress(c, 4, 4, 120, 10, value, time);
            }
        }
        for (String icon : new String[] {"left", "right", "cross", "test:icon", "test:missing"}) {
            c.note("> icon " + icon);
            theme.icon(c, icon, 20, 20, 0xFFEEEEEE);
        }
        for (float hover : STATES) {
            for (float selected : STATES) {
                c.note("> swatch " + hover + " " + selected);
                theme.swatch(c, 10, 10, 20, 0xFF3B82F6, hover, selected);
            }
        }
    }
}
