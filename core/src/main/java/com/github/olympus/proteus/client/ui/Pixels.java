package com.github.olympus.proteus.client.ui;

public final class Pixels {
    private Pixels() {
    }

    public static void draw(Canvas c, int centerX, int centerY, int color, String... rows) {
        int width = 0;
        for (String row : rows) {
            width = Math.max(width, row.length());
        }
        int left = centerX - width / 2;
        int top = centerY - rows.length / 2;
        for (int y = 0; y < rows.length; y++) {
            String row = rows[y];
            int x = 0;
            while (x < row.length()) {
                if (row.charAt(x) != '#') {
                    x++;
                    continue;
                }
                int start = x;
                while (x < row.length() && row.charAt(x) == '#') {
                    x++;
                }
                c.fill(left + start, top + y, x - start, 1, color);
            }
        }
    }

    public static void slash(Canvas c, int centerX, int centerY, int size, int color) {
        for (int i = 0; i < size; i++) {
            c.fill(centerX - size / 2 + i, centerY - size / 2 + i, 2, 1, color);
        }
    }
}
