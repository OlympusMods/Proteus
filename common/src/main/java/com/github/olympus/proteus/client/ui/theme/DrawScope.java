package com.github.olympus.proteus.client.ui.theme;

import com.github.olympus.proteus.client.ui.Canvas;
import java.util.HashMap;
import java.util.Map;

final class DrawScope implements Scope {
    final Map<String, Object> values;
    final StyledTheme theme;
    final Canvas canvas;

    DrawScope(StyledTheme theme, Canvas canvas, Map<String, Object> state) {
        this.theme = theme;
        this.canvas = canvas;
        this.values = new HashMap<>(state);
    }

    @Override
    public Object lookup(String name) {
        Object value = values.get(name);
        return value != null ? value : theme.colorValue(name);
    }

    @Override
    public boolean exists(String resource) {
        int colon = resource.indexOf(':');
        if (colon < 0) {
            return canvas.exists("minecraft", resource);
        }
        return canvas.exists(resource.substring(0, colon), resource.substring(colon + 1));
    }
}
