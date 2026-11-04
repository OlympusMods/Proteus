package com.github.olympus.proteus.client.ui;

public record RawMouse(int button) {
    public boolean left() {
        return button == 0;
    }
}
