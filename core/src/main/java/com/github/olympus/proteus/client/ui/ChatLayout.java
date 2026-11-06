package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ui.ChatSkin.Box;
import com.github.olympus.proteus.client.ui.ChatSkin.Spacing;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ChatLayout {
    public record Row(int index, Object key, int group, boolean first, Box box, Spacing spacing, boolean hidden) {
        public float lift() {
            return ChatSkin.lift(group, spacing);
        }
    }

    private static final Row[] NONE = new Row[0];

    private final Map<Object, Row> byKey = new IdentityHashMap<>();
    private Row[] rows = NONE;
    private int cursor;

    public void clear() {
        byKey.clear();
        rows = NONE;
        cursor = 0;
    }

    public boolean ready() {
        return rows.length > 0;
    }

    public void build(Object[] keys, int[] widths, boolean[] starts, int rowHeight, int height, Spacing spacing) {
        clear();
        int count = keys.length;
        Row[] built = new Row[count];
        int first = 0;
        int group = 0;
        boolean hidden = false;
        while (first < count) {
            int end = first + 1;
            int width = widths[first];
            while (end < count && !starts[end]) {
                width = Math.max(width, widths[end]);
                end++;
            }
            Box box = ChatSkin.bubble(first, end - first, rowHeight, group, width, spacing);
            hidden = hidden || !ChatSkin.fits(box, height);
            for (int i = first; i < end; i++) {
                built[i] = new Row(i, keys[i], group, i == first, box, spacing, hidden);
            }
            group++;
            first = end;
        }
        rows = built;
        for (int i = 0; i < count; i++) {
            byKey.put(keys[i], built[i]);
        }
    }

    public Row next() {
        return cursor < rows.length ? rows[cursor++] : null;
    }

    public Row row(Object key) {
        return byKey.get(key);
    }

    public double locate(double y, int rowHeight) {
        for (Row row : rows) {
            if (row.hidden()) {
                break;
            }
            double shift = -(double) row.lift() / rowHeight;
            double low = row.index() + shift;
            if (y >= low && y < low + 1.0) {
                return y - shift;
            }
        }
        return -1.0;
    }
}
