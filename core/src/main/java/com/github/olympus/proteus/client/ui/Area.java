package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ui.code.Kind;
import com.github.olympus.proteus.client.ui.code.Language;
import com.github.olympus.proteus.client.ui.code.Problem;
import com.github.olympus.proteus.client.ui.code.Span;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.lwjgl.glfw.GLFW;

final class Area {
    String text = "";
    int max = 4000;
    int cursor;
    int anchor = -1;
    int clicks;
    int clickIndex = -1;
    double clickTime = -1;
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
    private final Deque<Snapshot> undo = new ArrayDeque<>();
    private final Deque<Snapshot> redo = new ArrayDeque<>();
    private int lastKind;
    private String lastText;
    private int lastCursor = -1;

    private record Snapshot(String text, int cursor) {
    }

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

    boolean selecting() {
        return anchor >= 0 && anchor != cursor;
    }

    int selectionStart() {
        return selecting() ? Math.min(anchor, cursor) : cursor;
    }

    int selectionEnd() {
        return selecting() ? Math.max(anchor, cursor) : cursor;
    }

    String selected() {
        return text.substring(selectionStart(), selectionEnd());
    }

    void reset(String value) {
        text = value;
        cursor = value.length();
        anchor = -1;
        undo.clear();
        redo.clear();
        lastKind = 0;
        lastText = null;
    }

    void selectAll() {
        anchor = 0;
        cursor = text.length();
        wantX = -1;
    }

    void selectWord(int index) {
        if (text.isEmpty()) {
            return;
        }
        int pivot = Math.min(index, text.length() - 1);
        int group = group(text.charAt(pivot));
        int from = pivot;
        while (from > 0 && group(text.charAt(from - 1)) == group) {
            from--;
        }
        int to = pivot + 1;
        while (to < text.length() && group(text.charAt(to)) == group) {
            to++;
        }
        anchor = from;
        cursor = to;
        wantX = -1;
    }

    void selectLine(int index) {
        int from = text.lastIndexOf('\n', index - 1) + 1;
        int newline = text.indexOf('\n', index);
        anchor = from;
        cursor = newline < 0 ? text.length() : newline + 1;
        wantX = -1;
    }

    void insert(String value) {
        String clean = value.replace("\r", "").replaceAll("[\\p{Cntrl}&&[^\n]]", "");
        int from = selectionStart();
        int to = selectionEnd();
        String next = text.substring(0, from) + clean + text.substring(to);
        if (next.length() > max) {
            return;
        }
        int kind = clean.length() == 1 && !Character.isWhitespace(clean.charAt(0)) ? 1 : 0;
        record(kind);
        text = next;
        cursor = from + clean.length();
        anchor = -1;
        wantX = -1;
        follow = true;
        tidy();
        remember(kind);
    }

    boolean key(Canvas canvas, int key, int mods, Consumer<String> copy, Supplier<String> paste) {
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
                case GLFW.GLFW_KEY_A -> selectAll();
                case GLFW.GLFW_KEY_C -> {
                    if (selecting()) {
                        copy.accept(selected());
                    }
                }
                case GLFW.GLFW_KEY_X -> {
                    if (selecting()) {
                        copy.accept(selected());
                        erase(selectionStart(), selectionEnd(), 0);
                    }
                }
                case GLFW.GLFW_KEY_V -> insert(paste.get());
                case GLFW.GLFW_KEY_Z -> {
                    if (shift) {
                        redo();
                    } else {
                        undo();
                    }
                }
                case GLFW.GLFW_KEY_Y -> redo();
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    if (selecting()) {
                        erase(selectionStart(), selectionEnd(), 0);
                    } else {
                        erase(wordLeft(cursor), cursor, 0);
                    }
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    if (selecting()) {
                        erase(selectionStart(), selectionEnd(), 0);
                    } else {
                        erase(cursor, wordRight(cursor), 0);
                    }
                }
                case GLFW.GLFW_KEY_LEFT -> move(wordLeft(cursor), shift);
                case GLFW.GLFW_KEY_RIGHT -> move(wordRight(cursor), shift);
                case GLFW.GLFW_KEY_HOME -> move(0, shift);
                case GLFW.GLFW_KEY_END -> move(text.length(), shift);
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
                if (selecting()) {
                    erase(selectionStart(), selectionEnd(), 0);
                } else {
                    erase(cursor - 1, cursor, 2);
                }
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (selecting()) {
                    erase(selectionStart(), selectionEnd(), 0);
                } else {
                    erase(cursor, cursor + 1, 3);
                }
            }
            case GLFW.GLFW_KEY_LEFT -> {
                if (!shift && selecting()) {
                    move(selectionStart(), false);
                } else {
                    move(Math.max(0, cursor - 1), shift);
                }
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (!shift && selecting()) {
                    move(selectionEnd(), false);
                } else {
                    move(Math.min(text.length(), cursor + 1), shift);
                }
            }
            case GLFW.GLFW_KEY_HOME -> move(lines.get(lineOf(cursor))[0], shift);
            case GLFW.GLFW_KEY_END -> move(lines.get(lineOf(cursor))[1], shift);
            case GLFW.GLFW_KEY_UP -> {
                if (shift || !browse(-1)) {
                    vertical(canvas, -1, shift);
                }
            }
            case GLFW.GLFW_KEY_DOWN -> {
                if (shift || !browse(1)) {
                    vertical(canvas, 1, shift);
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    void undo() {
        if (undo.isEmpty()) {
            return;
        }
        redo.push(new Snapshot(text, cursor));
        restore(undo.pop());
    }

    void redo() {
        if (redo.isEmpty()) {
            return;
        }
        undo.push(new Snapshot(text, cursor));
        restore(redo.pop());
    }

    private void restore(Snapshot snapshot) {
        text = snapshot.text();
        cursor = Math.min(snapshot.cursor(), text.length());
        anchor = -1;
        wantX = -1;
        follow = true;
        lastKind = 0;
    }

    private void record(int kind) {
        if (kind != 0 && kind == lastKind && cursor == lastCursor && text.equals(lastText)) {
            return;
        }
        undo.push(new Snapshot(text, cursor));
        if (undo.size() > 200) {
            undo.removeLast();
        }
        redo.clear();
    }

    private void remember(int kind) {
        lastKind = kind;
        lastText = text;
        lastCursor = cursor;
    }

    private void erase(int from, int to, int kind) {
        int start = Math.max(0, from);
        int end = Math.min(text.length(), to);
        if (start >= end) {
            return;
        }
        record(kind);
        text = text.substring(0, start) + text.substring(end);
        cursor = start;
        anchor = -1;
        wantX = -1;
        follow = true;
        remember(kind);
    }

    private static int group(char c) {
        if (c == '\n') {
            return 3;
        }
        if (Character.isWhitespace(c)) {
            return 0;
        }
        return Character.isLetterOrDigit(c) || c == '_' ? 1 : 2;
    }

    private int wordLeft(int from) {
        int i = from;
        if (i > 0 && text.charAt(i - 1) == '\n') {
            return i - 1;
        }
        while (i > 0 && group(text.charAt(i - 1)) == 0) {
            i--;
        }
        if (i == 0 || text.charAt(i - 1) == '\n') {
            return i;
        }
        int group = group(text.charAt(i - 1));
        while (i > 0 && group(text.charAt(i - 1)) == group) {
            i--;
        }
        return i;
    }

    private int wordRight(int from) {
        int i = from;
        int length = text.length();
        if (i < length && text.charAt(i) == '\n') {
            return i + 1;
        }
        while (i < length && group(text.charAt(i)) == 0) {
            i++;
        }
        if (i == length || text.charAt(i) == '\n') {
            return i;
        }
        int group = group(text.charAt(i));
        while (i < length && group(text.charAt(i)) == group) {
            i++;
        }
        return i;
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
        if (language == null || selecting()) {
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
        record(0);
        text = next;
        cursor = from + item.insert().length();
        anchor = -1;
        wantX = -1;
        follow = true;
        tidy();
        remember(0);
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
        int start = selectionStart();
        int lineStart = text.lastIndexOf('\n', start - 1) + 1;
        int indent = language.indentAfter(text.substring(lineStart, start));
        insert("\n" + " ".repeat(indent));
    }

    private void tab(boolean shift) {
        if (selecting()) {
            indentBlock(shift);
            return;
        }
        if (shift) {
            int lineStart = text.lastIndexOf('\n', cursor - 1) + 1;
            int spaces = 0;
            while (spaces < Language.indentWidth() && lineStart + spaces < text.length()
                    && text.charAt(lineStart + spaces) == ' ') {
                spaces++;
            }
            if (spaces > 0) {
                record(0);
                text = text.substring(0, lineStart) + text.substring(lineStart + spaces);
                cursor = Math.max(lineStart, cursor - spaces);
                remember(0);
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

    private void indentBlock(boolean shift) {
        int from = text.lastIndexOf('\n', selectionStart() - 1) + 1;
        int to = selectionEnd();
        if (to > from && text.charAt(to - 1) == '\n') {
            to--;
        }
        String[] rows = text.substring(from, to).split("\n", -1);
        StringBuilder block = new StringBuilder();
        for (int i = 0; i < rows.length; i++) {
            String row = rows[i];
            if (shift) {
                int spaces = 0;
                while (spaces < Language.indentWidth() && spaces < row.length() && row.charAt(spaces) == ' ') {
                    spaces++;
                }
                row = row.substring(spaces);
            } else if (!row.isEmpty()) {
                row = " ".repeat(Language.indentWidth()) + row;
            }
            if (i > 0) {
                block.append('\n');
            }
            block.append(row);
        }
        String next = text.substring(0, from) + block + text.substring(to);
        if (next.length() > max) {
            return;
        }
        record(0);
        text = next;
        anchor = from;
        cursor = from + block.length();
        wantX = -1;
        follow = true;
        remember(0);
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

    void move(int index, boolean extend) {
        if (!extend) {
            anchor = -1;
        } else if (anchor < 0) {
            anchor = cursor;
        }
        cursor = index;
        wantX = -1;
        follow = true;
    }

    private void vertical(Canvas canvas, int direction, boolean extend) {
        int line = lineOf(cursor);
        int target = line + direction;
        if (target < 0) {
            move(0, extend);
            return;
        }
        if (target >= lines.size()) {
            move(text.length(), extend);
            return;
        }
        if (wantX < 0) {
            wantX = canvas.textWidth(text.substring(lines.get(line)[0], cursor));
        }
        int keep = wantX;
        move(indexAt(canvas, target, keep), extend);
        wantX = keep;
    }
}
