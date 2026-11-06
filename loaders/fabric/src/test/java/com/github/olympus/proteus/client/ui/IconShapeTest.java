package com.github.olympus.proteus.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class IconShapeTest {
    @Test
    void middleRowsKeepTheFullWidth() {
        assertEquals(0, IconShape.inset(16, 32, 8));
        assertEquals(0, IconShape.inset(8, 32, 8));
    }

    @Test
    void cornersCutIn() {
        assertTrue(IconShape.inset(0, 32, 8) > IconShape.inset(4, 32, 8));
        assertEquals(IconShape.inset(0, 32, 8), IconShape.inset(31, 32, 8));
    }

    @Test
    void runsCoverEveryRowOnce() {
        List<int[]> runs = IconShape.runs(32, IconShape.radius(32));
        int rows = 0;
        for (int[] run : runs) {
            assertEquals(rows, run[1]);
            assertEquals(32 - 2 * run[0], run[2]);
            rows += run[3];
        }
        assertEquals(32, rows);
    }
}
