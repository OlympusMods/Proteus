package com.github.olympus.proteus.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.olympus.proteus.client.ui.ChatSkin.Spacing;
import org.junit.jupiter.api.Test;

class ChatLayoutTest {
    private static final Spacing SPACING = new Spacing(2, 2);

    private static ChatLayout layout(boolean[] starts, int[] widths, int height) {
        Object[] keys = new Object[starts.length];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = new Object();
        }
        ChatLayout layout = new ChatLayout();
        layout.build(keys, widths, starts, 9, height, SPACING);
        return layout;
    }

    @Test
    void emptyLayoutIsNotReady() {
        assertFalse(new ChatLayout().ready());
    }

    @Test
    void rowsOfOneMessageShareAGroupAndTheWidestLine() {
        ChatLayout layout = layout(new boolean[] {true, false, false, true}, new int[] {40, 90, 60, 30}, 400);
        ChatLayout.Row bottom = layout.next();
        ChatLayout.Row middle = layout.next();
        ChatLayout.Row top = layout.next();
        ChatLayout.Row other = layout.next();
        assertTrue(bottom.first());
        assertFalse(middle.first());
        assertFalse(top.first());
        assertEquals(0, bottom.group());
        assertEquals(0, top.group());
        assertEquals(1, other.group());
        assertTrue(other.first());
        assertEquals(bottom.box(), top.box());
        assertEquals(90 + 8, bottom.box().w());
        assertEquals(30 + 8, other.box().w());
    }

    @Test
    void laterGroupsAreLiftedByTheirGroupsBelow() {
        ChatLayout layout = layout(new boolean[] {true, true, true}, new int[] {10, 10, 10}, 400);
        float first = layout.next().lift();
        float second = layout.next().lift();
        float third = layout.next().lift();
        assertEquals(second - first, third - second);
        assertTrue(second < first);
    }

    @Test
    void nextRunsOutAfterTheLastRow() {
        ChatLayout layout = layout(new boolean[] {true, true}, new int[] {10, 10}, 400);
        layout.next();
        layout.next();
        assertNull(layout.next());
    }

    @Test
    void groupsAboveTheChatHeightAreHiddenWithEverythingAfterThem() {
        ChatLayout layout = layout(new boolean[] {true, true, true, true}, new int[] {10, 10, 10, 10}, 30);
        boolean seenHidden = false;
        for (int i = 0; i < 4; i++) {
            ChatLayout.Row row = layout.next();
            if (row.hidden()) {
                seenHidden = true;
            }
            if (seenHidden) {
                assertTrue(row.hidden());
            }
        }
        assertTrue(seenHidden);
    }

    @Test
    void rowsAreFoundByTheIdentityOfTheirKey() {
        Object first = new Object();
        Object second = new Object();
        ChatLayout layout = new ChatLayout();
        layout.build(new Object[] {first, second}, new int[] {10, 10}, new boolean[] {true, true}, 9, 400, SPACING);
        assertEquals(0, layout.row(first).index());
        assertEquals(1, layout.row(second).index());
        assertSame(null, layout.row(new Object()));
    }

    @Test
    void clearForgetsEverything() {
        ChatLayout layout = layout(new boolean[] {true}, new int[] {10}, 400);
        layout.clear();
        assertFalse(layout.ready());
        assertNull(layout.next());
    }

    @Test
    void locateMapsALiftedRowBackToItsOwnSlot() {
        ChatLayout layout = layout(new boolean[] {true, true}, new int[] {10, 10}, 400);
        ChatLayout.Row first = layout.next();
        ChatLayout.Row second = layout.next();
        double firstShift = -(double) first.lift() / 9.0;
        double secondShift = -(double) second.lift() / 9.0;
        assertEquals(0.5, layout.locate(0.5 + firstShift, 9), 1e-9);
        assertEquals(1.25, layout.locate(1.25 + secondShift, 9), 1e-9);
    }

    @Test
    void locateFindsNothingBetweenBubbles() {
        ChatLayout layout = layout(new boolean[] {true, true}, new int[] {10, 10}, 400);
        ChatLayout.Row first = layout.next();
        double firstShift = -(double) first.lift() / 9.0;
        assertEquals(-1.0, layout.locate(firstShift + 1.0 + 0.01, 9), 1e-9);
        assertEquals(-1.0, layout.locate(-5.0, 9), 1e-9);
    }
}
