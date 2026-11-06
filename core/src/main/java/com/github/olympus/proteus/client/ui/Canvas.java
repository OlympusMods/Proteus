package com.github.olympus.proteus.client.ui;

import java.util.UUID;

public interface Canvas {
    int width();

    int height();

    void fill(int x, int y, int w, int h, int argb);

    void gradient(int x, int y, int w, int h, int topArgb, int bottomArgb);

    void text(String text, int x, int y, int argb, boolean shadow);

    int textWidth(String text);

    int lineHeight();

    void push();

    void pop();

    void translate(float x, float y);

    void scale(float x, float y);

    void rotate(float degrees);

    void clip(int x, int y, int w, int h);

    void unclip();

    boolean hasItem(String name);

    void item(String name, int x, int y);

    void sprite(String path, int x, int y, int w, int h);

    default void tinted(String sprite, int x, int y, int w, int h, float alpha) {
        sprite(sprite, x, y, w, h);
    }

    boolean exists(String namespace, String path);

    void tiled(String namespace, String path, int x, int y, int w, int h);

    default void head(UUID player, int x, int y, int size) {
    }

    default void effectIcon(String effect, int x, int y, int size, float alpha) {
    }

    default void glass(int x, int y, int w, int h, Glass glass) {
        Draw.round(this, x, y, w, h, glass.radius(), glass.fallbackColor());
    }
}
