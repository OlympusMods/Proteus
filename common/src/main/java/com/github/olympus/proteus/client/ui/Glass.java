package com.github.olympus.proteus.client.ui;

public record Glass(int radius, int tint, float blur, float refraction, float depth, float dispersion,
        float zoom, float smear, float highlight, float light, float saturation, float glow, float opacity) {
    public static final float BLUR = 2f;
    public static final float REFRACTION = 6f;
    public static final float DEPTH = 8f;
    public static final float DISPERSION = 0.3f;
    public static final float ZOOM = 4f;
    public static final float SMEAR = 3f;
    public static final float HIGHLIGHT = 0.7f;
    public static final float LIGHT = -45f;
    public static final float SATURATION = 1.5f;

    public Glass {
        radius = Math.max(0, radius);
        blur = clamp(blur, 0f, 24f);
        refraction = clamp(refraction, -48f, 48f);
        depth = clamp(depth, 0f, 64f);
        dispersion = clamp(dispersion, 0f, 1f);
        zoom = clamp(zoom, -48f, 48f);
        smear = clamp(smear, 0f, 32f);
        highlight = clamp(highlight, 0f, 4f);
        saturation = clamp(saturation, 0f, 4f);
        glow = clamp(glow, 0f, 4f);
        opacity = clamp(opacity, 0f, 1f);
    }

    public static Glass of(int radius, int tint) {
        return new Glass(radius, tint, BLUR, REFRACTION, DEPTH, DISPERSION, ZOOM, SMEAR, HIGHLIGHT, LIGHT,
                SATURATION, 0f, 1f);
    }

    public Glass opacity(float value) {
        return new Glass(radius, tint, blur, refraction, depth, dispersion, zoom, smear, highlight, light,
                saturation, glow, value);
    }

    public int fallbackColor() {
        int alpha = Math.round(Math.max(0x40, (tint >>> 24) * 0.9f) * opacity);
        return (Math.min(255, alpha) << 24) | (tint & 0xFFFFFF);
    }

    private static float clamp(float value, float min, float max) {
        if (Float.isNaN(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }
}
