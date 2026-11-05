package com.github.olympus.proteus.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.olympus.proteus.client.ui.code.Kind;
import com.github.olympus.proteus.client.ui.code.Language;
import com.github.olympus.proteus.client.ui.code.Problem;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

class CodeAreaTest {
    private final RecordingCanvas canvas = new RecordingCanvas();
    private Area area;

    @BeforeEach
    void setUp() {
        Language language = Language.builder("Test")
                .comment("#")
                .block("end", "repeat", "if")
                .middle("else")
                .word("from", "to")
                .mark(Pattern.compile("\\{[^{}]*}"), Kind.VARIABLE)
                .entry(Language.Entry.of("say", "say <message>", "Sends a message"))
                .entry(Language.Entry.of("sound", "sound <name>", "Plays a sound").values("bell", "bass", "chime"))
                .entry(Language.Entry.of("wait", "wait <time>", "Pauses"))
                .entry(Language.Entry.of("repeat", "repeat <n>", "Loops"))
                .entry(Language.Entry.of("end", "end", "Closes a loop"))
                .entry(Language.Entry.of("if", "if <test>", "Branches"))
                .entry(Language.Entry.of("else", "else", "Other branch"))
                .entry(Language.Entry.of("command", "command <text>", "Runs a command").aliases("cmd"))
                .validator(source -> source.contains("oops") ? List.of(new Problem(1, "bad")) : List.of())
                .build();
        area = new Area();
        area.language = language;
        area.wrap(canvas, 1000);
    }

    private void type(String text) {
        for (char c : text.toCharArray()) {
            area.insert(String.valueOf(c));
        }
        area.wrap(canvas, 1000);
    }

    private void press(int key) {
        area.key(canvas, key, 0, text -> { }, () -> "");
        area.wrap(canvas, 1000);
    }

    private void press(int key, int mods) {
        area.key(canvas, key, mods, text -> { }, () -> "");
        area.wrap(canvas, 1000);
    }

    private void load(String value) {
        area.reset(value);
        area.wrap(canvas, 1000);
    }

    @Test
    void popupListsMatchingCommands() {
        type("s");
        List<String> labels = area.completion().items().stream().map(Language.Suggestion::label).toList();
        assertEquals(List.of("say", "sound"), labels);
    }

    @Test
    void tabAcceptsTheSelectedSuggestion() {
        type("wa");
        press(GLFW.GLFW_KEY_TAB);
        assertEquals("wait ", area.text);
    }

    @Test
    void arrowsMovePickAndTabTakesIt() {
        type("s");
        press(GLFW.GLFW_KEY_DOWN);
        press(GLFW.GLFW_KEY_TAB);
        assertEquals("sound ", area.text);
    }

    @Test
    void completesValuesAfterTheCommand() {
        type("sound b");
        List<String> labels = area.completion().items().stream().map(Language.Suggestion::label).toList();
        assertEquals(List.of("bell", "bass"), labels);
        press(GLFW.GLFW_KEY_TAB);
        assertEquals("sound bell", area.text);
    }

    @Test
    void exactWordShowsNoPopup() {
        type("end");
        assertTrue(area.completion().isEmpty());
    }

    @Test
    void escapeDismissesUntilTheTextChanges() {
        type("s");
        assertTrue(area.dismiss());
        assertTrue(area.completion().isEmpty());
        type("a");
        assertFalse(area.completion().isEmpty());
    }

    @Test
    void tabWithoutAPopupIndents() {
        type("say hello");
        press(GLFW.GLFW_KEY_TAB);
        assertEquals("say hello  ", area.text);
    }

    @Test
    void enterKeepsAndGrowsIndent() {
        type("repeat 3");
        press(GLFW.GLFW_KEY_ENTER);
        assertEquals("repeat 3\n  ", area.text);
        type("say hi");
        press(GLFW.GLFW_KEY_ENTER);
        assertEquals("repeat 3\n  say hi\n  ", area.text);
    }

    @Test
    void typingEndDedents() {
        type("repeat 3");
        press(GLFW.GLFW_KEY_ENTER);
        type("say hi");
        press(GLFW.GLFW_KEY_ENTER);
        type("end");
        assertEquals("repeat 3\n  say hi\nend", area.text);
        assertEquals(area.text.length(), area.cursor);
    }

    @Test
    void elseLinesUpWithItsIfAndIndentsAfterwards() {
        type("if a");
        press(GLFW.GLFW_KEY_ENTER);
        type("say hi");
        press(GLFW.GLFW_KEY_ENTER);
        type("else");
        assertEquals("if a\n  say hi\nelse", area.text);
        press(GLFW.GLFW_KEY_ENTER);
        assertEquals("if a\n  say hi\nelse\n  ", area.text);
    }

    @Test
    void enterAcceptsOnlyWhenThereIsAPrefix() {
        type("say");
        assertTrue(area.completion().isEmpty());
        type(" ");
        press(GLFW.GLFW_KEY_ENTER);
        assertEquals("say \n", area.text);
    }

    @Test
    void controlSpaceListsEverything() {
        area.key(canvas, GLFW.GLFW_KEY_SPACE, GLFW.GLFW_MOD_CONTROL, text -> { }, () -> "");
        assertEquals(8, area.completion().items().size());
    }

    @Test
    void shiftTabRemovesIndent() {
        type("  say hi");
        area.key(canvas, GLFW.GLFW_KEY_TAB, GLFW.GLFW_MOD_SHIFT, text -> { }, () -> "");
        assertEquals("say hi", area.text);
    }

    @Test
    void highlightsKeywordsNumbersAndUnknownWords() {
        type("wait 20t");
        area.analyse();
        assertEquals(Kind.KEYWORD, area.kinds[0]);
        assertEquals(Kind.PLAIN, area.kinds[4]);
        assertEquals(Kind.NUMBER, area.kinds[5]);
        area.text = "foo";
        area.analyse();
        assertEquals(Kind.UNKNOWN, area.kinds[0]);
    }

    @Test
    void secondaryWordsAndMarkedPatternsAreColored() {
        area.text = "say a from {x} 3";
        area.analyse();
        assertEquals(Kind.PLAIN, area.kinds[4]);
        assertEquals(Kind.KEYWORD, area.kinds[6]);
        assertEquals(Kind.VARIABLE, area.kinds[11]);
        assertEquals(Kind.NUMBER, area.kinds[15]);
    }

    @Test
    void aliasesAreKnownWords() {
        area.text = "cmd list";
        area.analyse();
        assertEquals(Kind.KEYWORD, area.kinds[0]);
    }

    @Test
    void commentsAreNotCompleted() {
        type("# s");
        assertTrue(area.completion().isEmpty());
        area.analyse();
        assertEquals(Kind.COMMENT, area.kinds[2]);
    }

    @Test
    void validatorRunsOnChange() {
        area.text = "say oops";
        area.analyse();
        assertEquals(1, area.problems.size());
    }

    @Test
    void shiftArrowsExtendTheSelection() {
        load("say hello");
        press(GLFW.GLFW_KEY_LEFT, GLFW.GLFW_MOD_SHIFT);
        press(GLFW.GLFW_KEY_LEFT, GLFW.GLFW_MOD_SHIFT);
        assertEquals("lo", area.selected());
        press(GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_MOD_SHIFT);
        assertEquals("o", area.selected());
    }

    @Test
    void plainArrowCollapsesToTheEdge() {
        load("say hello");
        press(GLFW.GLFW_KEY_LEFT, GLFW.GLFW_MOD_SHIFT);
        press(GLFW.GLFW_KEY_LEFT, GLFW.GLFW_MOD_SHIFT);
        press(GLFW.GLFW_KEY_LEFT);
        assertFalse(area.selecting());
        assertEquals(7, area.cursor);
    }

    @Test
    void controlArrowsJumpWords() {
        load("say hello world");
        press(GLFW.GLFW_KEY_LEFT, GLFW.GLFW_MOD_CONTROL);
        assertEquals(10, area.cursor);
        press(GLFW.GLFW_KEY_LEFT, GLFW.GLFW_MOD_CONTROL);
        assertEquals(4, area.cursor);
        press(GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_MOD_CONTROL);
        assertEquals(9, area.cursor);
    }

    @Test
    void controlShiftArrowsSelectWords() {
        load("say hello world");
        int both = GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT;
        press(GLFW.GLFW_KEY_LEFT, both);
        press(GLFW.GLFW_KEY_LEFT, both);
        assertEquals("hello world", area.selected());
    }

    @Test
    void controlArrowStopsAtLineBreaks() {
        load("say\nhello");
        area.cursor = 4;
        press(GLFW.GLFW_KEY_LEFT, GLFW.GLFW_MOD_CONTROL);
        assertEquals(3, area.cursor);
        press(GLFW.GLFW_KEY_LEFT, GLFW.GLFW_MOD_CONTROL);
        assertEquals(0, area.cursor);
    }

    @Test
    void controlBackspaceAndDeleteRemoveWords() {
        load("say hello world");
        press(GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_MOD_CONTROL);
        assertEquals("say hello ", area.text);
        area.cursor = 4;
        press(GLFW.GLFW_KEY_DELETE, GLFW.GLFW_MOD_CONTROL);
        assertEquals("say  ", area.text);
    }

    @Test
    void typingReplacesTheSelection() {
        load("say hello");
        press(GLFW.GLFW_KEY_A, GLFW.GLFW_MOD_CONTROL);
        type("wait");
        assertEquals("wait", area.text);
        assertFalse(area.selecting());
    }

    @Test
    void backspaceDeletesTheSelection() {
        load("say hello");
        press(GLFW.GLFW_KEY_LEFT, GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT);
        press(GLFW.GLFW_KEY_BACKSPACE);
        assertEquals("say ", area.text);
    }

    @Test
    void copyCutAndPaste() {
        List<String> clipboard = new ArrayList<>();
        load("say hello");
        press(GLFW.GLFW_KEY_LEFT, GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT);
        area.key(canvas, GLFW.GLFW_KEY_C, GLFW.GLFW_MOD_CONTROL, clipboard::add, () -> "");
        assertEquals(List.of("hello"), clipboard);
        assertEquals("say hello", area.text);
        area.key(canvas, GLFW.GLFW_KEY_X, GLFW.GLFW_MOD_CONTROL, clipboard::add, () -> "");
        assertEquals("say ", area.text);
        assertEquals("hello", clipboard.get(1));
        area.key(canvas, GLFW.GLFW_KEY_V, GLFW.GLFW_MOD_CONTROL, clipboard::add, () -> "bye");
        assertEquals("say bye", area.text);
    }

    @Test
    void copyWithoutASelectionLeavesTheClipboardAlone() {
        List<String> clipboard = new ArrayList<>();
        load("say hello");
        area.key(canvas, GLFW.GLFW_KEY_C, GLFW.GLFW_MOD_CONTROL, clipboard::add, () -> "");
        assertTrue(clipboard.isEmpty());
    }

    @Test
    void homeAndEndSelectToLineEdges() {
        load("say hello");
        press(GLFW.GLFW_KEY_HOME, GLFW.GLFW_MOD_SHIFT);
        assertEquals("say hello", area.selected());
        press(GLFW.GLFW_KEY_END);
        press(GLFW.GLFW_KEY_HOME, GLFW.GLFW_MOD_CONTROL);
        assertEquals(0, area.cursor);
    }

    @Test
    void shiftDownSelectsAcrossLines() {
        load("say a\nsay b");
        area.cursor = 0;
        press(GLFW.GLFW_KEY_DOWN, GLFW.GLFW_MOD_SHIFT);
        assertEquals("say a\n", area.selected());
    }

    @Test
    void doubleClickSelectsTheWordAndTripleTheLine() {
        load("say hello\nwait 5");
        area.selectWord(6);
        assertEquals("hello", area.selected());
        area.selectLine(6);
        assertEquals("say hello\n", area.selected());
    }

    @Test
    void tabIndentsSelectedLinesAndShiftTabUndoesIt() {
        load("say a\nsay b");
        press(GLFW.GLFW_KEY_A, GLFW.GLFW_MOD_CONTROL);
        press(GLFW.GLFW_KEY_TAB);
        assertEquals("  say a\n  say b", area.text);
        press(GLFW.GLFW_KEY_TAB, GLFW.GLFW_MOD_SHIFT);
        assertEquals("say a\nsay b", area.text);
    }

    @Test
    void undoGroupsTypingAndRedoRestores() {
        type("say");
        type(" hi");
        press(GLFW.GLFW_KEY_Z, GLFW.GLFW_MOD_CONTROL);
        assertEquals("say ", area.text);
        press(GLFW.GLFW_KEY_Z, GLFW.GLFW_MOD_CONTROL);
        press(GLFW.GLFW_KEY_Z, GLFW.GLFW_MOD_CONTROL);
        assertEquals("", area.text);
        press(GLFW.GLFW_KEY_Y, GLFW.GLFW_MOD_CONTROL);
        assertEquals("say", area.text);
        press(GLFW.GLFW_KEY_Z, GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT);
        assertEquals("say ", area.text);
    }

    @Test
    void undoBringsBackACutSelection() {
        load("say hello");
        press(GLFW.GLFW_KEY_A, GLFW.GLFW_MOD_CONTROL);
        press(GLFW.GLFW_KEY_BACKSPACE);
        assertEquals("", area.text);
        press(GLFW.GLFW_KEY_Z, GLFW.GLFW_MOD_CONTROL);
        assertEquals("say hello", area.text);
    }
}
