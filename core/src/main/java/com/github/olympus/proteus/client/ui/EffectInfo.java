package com.github.olympus.proteus.client.ui;

public record EffectInfo(String id, String name, int amplifier, int ticks, boolean infinite, boolean ambient,
        boolean beneficial, boolean icon, boolean listed) {
    public String title() {
        String level = EffectTiming.level(amplifier);
        return level.isEmpty() ? name : name + " " + level;
    }

    public String time(float tickrate) {
        return infinite ? "**:**" : EffectTiming.format(ticks, tickrate);
    }

    public boolean expiring() {
        return !infinite && !ambient && ticks <= EffectTiming.WARN_TICKS;
    }

    public float blink() {
        return expiring() ? EffectTiming.blink(ticks) : 1f;
    }

    public float warn() {
        return expiring() ? EffectTiming.warn(ticks) : 0f;
    }
}
