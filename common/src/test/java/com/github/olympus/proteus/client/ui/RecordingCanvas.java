package com.github.olympus.proteus.client.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class RecordingCanvas implements Canvas {
    private final List<String> calls = new ArrayList<>();
    public final Set<String> existing = new HashSet<>();

    public List<String> calls() {
        return calls;
    }

    public void note(String line) {
        calls.add(line);
    }

    private static String hex(int argb) {
        return String.format(Locale.ROOT, "#%08X", argb);
    }

    @Override
    public int width() {
        return 427;
    }

    @Override
    public int height() {
        return 240;
    }

    @Override
    public void fill(int x, int y, int w, int h, int argb) {
        if ((argb >>> 24) == 0) {
            return;
        }
        calls.add("fill " + x + " " + y + " " + w + " " + h + " " + hex(argb));
    }

    @Override
    public void gradient(int x, int y, int w, int h, int topArgb, int bottomArgb) {
        if (((topArgb | bottomArgb) >>> 24) == 0) {
            return;
        }
        calls.add("gradient " + x + " " + y + " " + w + " " + h + " " + hex(topArgb) + " " + hex(bottomArgb));
    }

    @Override
    public void text(String text, int x, int y, int argb, boolean shadow) {
        calls.add("text " + text + " " + x + " " + y + " " + hex(argb) + " " + shadow);
    }

    @Override
    public int textWidth(String text) {
        return 6 * text.length();
    }

    @Override
    public int lineHeight() {
        return 9;
    }

    @Override
    public void push() {
        calls.add("push");
    }

    @Override
    public void pop() {
        calls.add("pop");
    }

    @Override
    public void translate(float x, float y) {
        calls.add("translate " + x + " " + y);
    }

    @Override
    public void scale(float x, float y) {
        calls.add("scale " + x + " " + y);
    }

    @Override
    public void rotate(float degrees) {
        calls.add("rotate " + degrees);
    }

    @Override
    public void clip(int x, int y, int w, int h) {
        calls.add("clip " + x + " " + y + " " + w + " " + h);
    }

    @Override
    public void unclip() {
        calls.add("unclip");
    }

    @Override
    public boolean hasItem(String name) {
        return true;
    }

    @Override
    public void item(String name, int x, int y) {
        calls.add("item " + name + " " + x + " " + y);
    }

    @Override
    public void sprite(String path, int x, int y, int w, int h) {
        calls.add("sprite " + path + " " + x + " " + y + " " + w + " " + h);
    }

    @Override
    public boolean exists(String namespace, String path) {
        return existing.contains(namespace + ":" + path);
    }

    @Override
    public void tiled(String namespace, String path, int x, int y, int w, int h) {
        calls.add("tiled " + namespace + ":" + path + " " + x + " " + y + " " + w + " " + h);
    }
}
