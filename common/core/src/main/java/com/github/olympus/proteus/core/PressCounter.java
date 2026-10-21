package com.github.olympus.proteus.core;

/** Version- and loader-independent logic goes in this module. This one only counts key presses. */
public final class PressCounter {
    private int presses;

    public int press() {
        return ++presses;
    }
}
