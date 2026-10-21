package com.github.olympus.proteus.client.ui;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFW;

final class Area {
    String text = "";
    int max = 4000;
    int cursor;
    boolean all;
    float scroll;
    float content;
    int wantX = -1;
    boolean follow;
    final List<int[]> lines = new ArrayList<>();

    void wrap(Canvas canvas, int width) {
        lines.clear();
        int paragraph = 0;
        while (paragraph <= text.length()) {
            int newline = text.indexOf('\n', paragraph);
            int end = newline < 0 ? text.length() : newline;
            wrapParagraph(canvas, paragraph, end, width);
            if (newline < 0) {
                break;
            }
            paragraph = newline + 1;
        }
    }

    private void wrapParagraph(Canvas canvas, int from, int to, int width) {
        int start = from;
        if (from == to) {
            lines.add(new int[] {from, to});
            return;
        }
        int lastSpace = -1;
        for (int i = from; i < to; i++) {
            if (text.charAt(i) == ' ') {
                lastSpace = i;
            }
            if (canvas.textWidth(text.substring(start, i + 1)) > width && i > start) {
                int cut = lastSpace > start ? lastSpace + 1 : i;
                lines.add(new int[] {start, cut});
                start = cut;
                lastSpace = -1;
                i = cut - 1;
            }
        }
        lines.add(new int[] {start, to});
    }

    int lineOf(int index) {
        int found = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i)[0] <= index) {
                found = i;
            }
        }
        return found;
    }

    int indexAt(Canvas canvas, int line, int offset) {
        int[] segment = lines.get(Math.max(0, Math.min(lines.size() - 1, line)));
        int best = segment[0];
        for (int i = segment[0] + 1; i <= segment[1]; i++) {
            int half = canvas.textWidth(text.substring(i - 1, i)) / 2;
            if (canvas.textWidth(text.substring(segment[0], i)) - half > offset) {
                return best;
            }
            best = i;
        }
        return best;
    }

    void insert(String value) {
        String clean = value.replace("\r", "").replaceAll("[\\p{Cntrl}&&[^\n]]", "");
        if (all) {
            text = "";
            cursor = 0;
            all = false;
        }
        String next = text.substring(0, cursor) + clean + text.substring(cursor);
        if (next.length() > max) {
            return;
        }
        text = next;
        cursor += clean.length();
        wantX = -1;
        follow = true;
    }

    boolean key(Canvas canvas, int key, int mods, java.util.function.Consumer<String> copy,
            java.util.function.Supplier<String> paste) {
        boolean control = (mods & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SUPER)) != 0;
        if (control) {
            switch (key) {
                case GLFW.GLFW_KEY_A -> all = !text.isEmpty();
                case GLFW.GLFW_KEY_C -> copy.accept(text);
                case GLFW.GLFW_KEY_X -> {
                    copy.accept(text);
                    text = "";
                    cursor = 0;
                    all = false;
                }
                case GLFW.GLFW_KEY_V -> insert(paste.get());
                default -> {
                    return false;
                }
            }
            return true;
        }
        switch (key) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> insert("\n");
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (all) {
                    text = "";
                    cursor = 0;
                    all = false;
                } else if (cursor > 0) {
                    text = text.substring(0, cursor - 1) + text.substring(cursor);
                    cursor--;
                }
                wantX = -1;
                follow = true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (all) {
                    text = "";
                    cursor = 0;
                    all = false;
                } else if (cursor < text.length()) {
                    text = text.substring(0, cursor) + text.substring(cursor + 1);
                }
                follow = true;
            }
            case GLFW.GLFW_KEY_LEFT -> move(Math.max(0, cursor - 1));
            case GLFW.GLFW_KEY_RIGHT -> move(Math.min(text.length(), cursor + 1));
            case GLFW.GLFW_KEY_HOME -> move(lines.get(lineOf(cursor))[0]);
            case GLFW.GLFW_KEY_END -> move(lines.get(lineOf(cursor))[1]);
            case GLFW.GLFW_KEY_UP -> vertical(canvas, -1);
            case GLFW.GLFW_KEY_DOWN -> vertical(canvas, 1);
            default -> {
                return false;
            }
        }
        return true;
    }

    private void move(int index) {
        all = false;
        cursor = index;
        wantX = -1;
        follow = true;
    }

    private void vertical(Canvas canvas, int direction) {
        all = false;
        int line = lineOf(cursor);
        int target = line + direction;
        if (target < 0 || target >= lines.size()) {
            return;
        }
        if (wantX < 0) {
            wantX = canvas.textWidth(text.substring(lines.get(line)[0], cursor));
        }
        cursor = indexAt(canvas, target, wantX);
        follow = true;
    }
}
