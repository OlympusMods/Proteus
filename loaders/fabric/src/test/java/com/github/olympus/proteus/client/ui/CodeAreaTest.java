package com.github.olympus.proteus.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.olympus.proteus.client.ui.code.Kind;
import com.github.olympus.proteus.client.ui.code.Language;
import com.github.olympus.proteus.client.ui.code.Problem;
import java.util.List;
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
}
