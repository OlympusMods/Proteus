package com.github.olympus.proteus.client.ui;

public final class EffectTiming {
    public static final int WARN_TICKS = 200;

    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private EffectTiming() {
    }

    public static String level(int amplifier) {
        if (amplifier < 1) {
            return "";
        }
        return amplifier < ROMAN.length ? ROMAN[amplifier] : String.valueOf(amplifier + 1);
    }

    public static String format(int ticks, float tickrate) {
        float rate = tickrate <= 0f ? 20f : tickrate;
        int seconds = (int) Math.floor(Math.max(0, ticks) / rate);
        int hours = seconds / 3600;
        int minutes = seconds / 60 % 60;
        int rest = seconds % 60;
        if (hours > 0) {
            return hours + ":" + two(minutes) + ":" + two(rest);
        }
        return two(minutes) + ":" + two(rest);
    }

    private static String two(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }

    public static float blink(int remaining) {
        if (remaining > WARN_TICKS) {
            return 1f;
        }
        int left = Math.max(0, remaining);
        float used = 10f - left / 20f;
        float base = Motion.clamp(left / 10f / 5f * 0.5f, 0f, 0.5f);
        float swing = (float) Math.cos(left * Math.PI / 5.0) * Motion.clamp(used / 10f * 0.25f, 0f, 0.25f);
        return Motion.clamp(base + swing, 0f, 1f);
    }

    public static float warn(int remaining) {
        if (remaining > WARN_TICKS) {
            return 0f;
        }
        int left = Math.max(0, remaining);
        float ramp = Motion.clamp((WARN_TICKS - left) / 40f, 0f, 1f);
        return ramp * (0.5f - 0.5f * (float) Math.cos(left * Math.PI / 5.0));
    }
}
