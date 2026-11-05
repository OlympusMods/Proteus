package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ui.code.Kind;
import com.github.olympus.proteus.client.ui.code.Language;
import com.github.olympus.proteus.client.ui.code.Problem;
import com.github.olympus.proteus.client.ui.code.Span;
import java.util.ArrayList;
import java.util.Arrays;
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
    Language language;
    boolean help;
    int pick;
    Kind[] kinds = new Kind[0];
    List<Problem> problems = List.of();
    private String analysed;
    private int pickKey;
    private String dismissedText;
    private int dismissedCursor = -1;
    private String forcedText;
    private int forcedCursor = -1;

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
        tidy();
    }

    boolean key(Canvas canvas, int key, int mods, java.util.function.Consumer<String> copy,
            java.util.function.Supplier<String> paste) {
        boolean control = (mods & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SUPER)) != 0;
        boolean shift = (mods & GLFW.GLFW_MOD_SHIFT) != 0;
        if (control) {
            switch (key) {
                case GLFW.GLFW_KEY_SPACE -> {
                    if (language == null) {
                        return false;
                    }
                    dismissedText = null;
                    forcedText = text;
                    forcedCursor = cursor;
                }
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
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> enter();
            case GLFW.GLFW_KEY_TAB -> {
                if (language == null) {
                    return false;
                }
                tab(shift);
            }
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
            case GLFW.GLFW_KEY_UP -> {
                if (!browse(-1)) {
                    vertical(canvas, -1);
                }
            }
            case GLFW.GLFW_KEY_DOWN -> {
                if (!browse(1)) {
                    vertical(canvas, 1);
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    void analyse() {
        if (language == null || analysed == text) {
            return;
        }
        analysed = text;
        kinds = new Kind[text.length()];
        Arrays.fill(kinds, Kind.PLAIN);
        int start = 0;
        while (start <= text.length()) {
            int newline = text.indexOf('\n', start);
            int end = newline < 0 ? text.length() : newline;
            for (Span span : language.highlight(text.substring(start, end))) {
                for (int i = Math.max(0, span.start()); i < Math.min(span.end(), end - start); i++) {
                    kinds[start + i] = span.kind();
                }
            }
            if (newline < 0) {
                break;
            }
            start = newline + 1;
        }
        problems = language.check(text);
    }

    int lineNumber(int index) {
        int count = 0;
        for (int i = 0; i < index && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    int lineCount() {
        return lineNumber(text.length()) + 1;
    }

    int columnOf(int index) {
        return index - (text.lastIndexOf('\n', index - 1) + 1);
    }

    Language.Completion completion() {
        if (language == null) {
            return Language.Completion.NONE;
        }
        if (dismissedText != null && dismissedCursor == cursor && dismissedText.equals(text)) {
            return Language.Completion.NONE;
        }
        boolean forced = forcedText != null && forcedCursor == cursor && forcedText.equals(text);
        Language.Completion found = language.complete(text, cursor, forced);
        int key = text.hashCode() * 31 + cursor;
        if (key != pickKey) {
            pickKey = key;
            pick = 0;
        }
        if (!found.isEmpty()) {
            pick = Math.floorMod(pick, found.items().size());
        }
        return found;
    }

    boolean dismiss() {
        if (completion().isEmpty()) {
            return false;
        }
        dismissedText = text;
        dismissedCursor = cursor;
        return true;
    }

    void accept(Language.Completion found, int index) {
        if (found.isEmpty() || index < 0 || index >= found.items().size()) {
            return;
        }
        Language.Suggestion item = found.items().get(index);
        int from = cursor - found.prefixLength();
        String next = text.substring(0, from) + item.insert() + text.substring(cursor);
        if (next.length() > max) {
            return;
        }
        text = next;
        cursor = from + item.insert().length();
        all = false;
        wantX = -1;
        follow = true;
        tidy();
    }

    private boolean browse(int direction) {
        Language.Completion found = completion();
        if (found.isEmpty()) {
            return false;
        }
        pick = Math.floorMod(pick + direction, found.items().size());
        return true;
    }

    private void enter() {
        if (language == null) {
            insert("\n");
            return;
        }
        Language.Completion found = completion();
        if (!found.isEmpty() && found.prefixLength() > 0) {
            accept(found, pick);
            return;
        }
        int lineStart = text.lastIndexOf('\n', cursor - 1) + 1;
        int indent = language.indentAfter(text.substring(lineStart, cursor));
        insert("\n" + " ".repeat(indent));
    }

    private void tab(boolean shift) {
        if (shift) {
            int lineStart = text.lastIndexOf('\n', cursor - 1) + 1;
            int spaces = 0;
            while (spaces < Language.indentWidth() && lineStart + spaces < text.length()
                    && text.charAt(lineStart + spaces) == ' ') {
                spaces++;
            }
            if (spaces > 0) {
                text = text.substring(0, lineStart) + text.substring(lineStart + spaces);
                cursor = Math.max(lineStart, cursor - spaces);
            }
            return;
        }
        Language.Completion found = completion();
        if (!found.isEmpty()) {
            accept(found, pick);
            return;
        }
        insert(" ".repeat(Language.indentWidth()));
    }

    private void tidy() {
        if (language == null) {
            return;
        }
        int lineStart = text.lastIndexOf('\n', cursor - 1) + 1;
        int lineEnd = text.indexOf('\n', cursor);
        if (lineEnd >= 0 && lineEnd != cursor) {
            return;
        }
        String line = text.substring(lineStart, cursor);
        if (line.endsWith(" ") || !language.closes(line)) {
            return;
        }
        int indent = line.length() - line.stripLeading().length();
        int cut = Math.min(indent, Language.indentWidth());
        if (cut > 0) {
            text = text.substring(0, lineStart) + text.substring(lineStart + cut);
            cursor -= cut;
        }
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
