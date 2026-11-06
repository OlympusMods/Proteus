package com.github.olympus.proteus.client.ui;

public final class EffectLayout {
    public static final int HUD_CELL = 24;
    public static final int HUD_STEP = 25;
    public static final int ROW_PLAIN = 26;
    public static final int ROW_TIMED = 36;
    public static final int DEMO_SHIFT = 15;
    public static final int PLATE = 32;
    public static final int WIDE_MIN = 120;
    public static final int TEXT_X = 32;
    public static final int PAD = 7;

    private EffectLayout() {
    }

    public static int hudY(boolean beneficial, boolean demo, boolean timed) {
        int y = 1 + (demo ? DEMO_SHIFT : 0);
        return beneficial ? y : y + (timed ? ROW_TIMED : ROW_PLAIN);
    }

    public static int hudX(int screenWidth, int index) {
        return screenWidth - HUD_STEP * index;
    }

    public static int inventoryStep(int count) {
        return count > 5 ? 132 / (count - 1) : 33;
    }

    public static boolean inventoryWide(int available, boolean compact) {
        return !compact && available >= WIDE_MIN;
    }

    public static int inventoryWidth(int available, boolean compact, int titleWidth, int timeWidth) {
        if (!inventoryWide(available, compact)) {
            return PLATE;
        }
        int wanted = Math.max(TEXT_X + titleWidth + PAD, TEXT_X + timeWidth + PAD);
        return Math.min(available - PAD, wanted);
    }
}
