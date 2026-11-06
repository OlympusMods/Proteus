package com.github.olympus.proteus.client.ui;

import java.util.ArrayList;
import java.util.List;

public final class IconShape {
    private IconShape() {
    }

    public static int inset(int row, int size, int radius) {
        int edge = Math.min(row, size - 1 - row);
        if (edge >= radius) {
            return 0;
        }
        double dy = radius - edge - 0.5;
        int reach = (int) Math.round(Math.sqrt(radius * (double) radius - dy * dy));
        return Math.max(0, radius - reach);
    }

    public static List<int[]> runs(int size, int radius) {
        List<int[]> runs = new ArrayList<>();
        int start = 0;
        int current = inset(0, size, radius);
        for (int row = 1; row <= size; row++) {
            int next = row < size ? inset(row, size, radius) : -1;
            if (next != current) {
                runs.add(new int[] {current, start, size - 2 * current, row - start});
                start = row;
                current = next;
            }
        }
        return runs;
    }
}
