package com.github.olympus.proteus.client.ui.theme;

import com.github.olympus.proteus.client.ui.Canvas;
import com.github.olympus.proteus.client.ui.Colors;
import com.github.olympus.proteus.client.ui.Draw;
import com.github.olympus.proteus.client.ui.Icons;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Layer {
    enum Type {
        FILL, ROUND, RING, PANEL, GRADIENT, SHADOW, SPRITE, TILED, ICON, CHECK, TEXT, GROUP, WIDGET
    }

    private final Type type;
    private final Expr x;
    private final Expr y;
    private final Expr w;
    private final Expr h;
    private final Expr inset;
    private final Expr radius;
    private final Expr color;
    private final Expr border;
    private final Expr top;
    private final Expr bottom;
    private final Expr spread;
    private final Expr alpha;
    private final Expr when;
    private final Expr rotate;
    private final Expr scale;
    private final Expr opacity;
    private final Expr shadow;
    private final Expr resource;
    private final Expr align;
    private final String widget;
    private final Map<String, Expr> vars;
    private final List<Layer> children;

    private Layer(JsonObject json) {
        String name = json.has("type") ? json.get("type").getAsString() : "fill";
        try {
            type = Type.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ExprException("unknown layer type '" + name + "'");
        }
        x = value(json, "x");
        y = value(json, "y");
        w = value(json, "w");
        h = value(json, "h");
        inset = value(json, "inset");
        radius = value(json, "radius");
        color = value(json, "color");
        border = value(json, "border");
        top = value(json, "top");
        bottom = value(json, "bottom");
        spread = value(json, "spread");
        alpha = value(json, "alpha");
        when = value(json, "when");
        rotate = value(json, "rotate");
        scale = value(json, "scale");
        opacity = value(json, "opacity");
        shadow = value(json, "shadow");
        align = text(json.get("align"));
        resource = text(json.has("sprite") ? json.get("sprite")
                : json.has("texture") ? json.get("texture")
                : json.has("text") ? json.get("text")
                : json.get("name"));
        widget = json.has("widget") ? json.get("widget").getAsString() : null;
        vars = new LinkedHashMap<>();
        if (json.has("vars") && json.get("vars").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("vars").entrySet()) {
                vars.put(entry.getKey(), value(entry.getValue()));
            }
        }
        JsonElement nested = json.has("layers") ? json.get("layers") : json.get("fallback");
        children = nested != null && nested.isJsonArray() ? list(nested.getAsJsonArray()) : null;
    }

    public static List<Layer> list(JsonArray array) {
        List<Layer> layers = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                throw new ExprException("layers must be objects");
            }
            layers.add(new Layer(element.getAsJsonObject()));
        }
        return layers;
    }

    static Expr value(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (!element.isJsonPrimitive()) {
            throw new ExprException("expected a value but got " + element);
        }
        if (element.getAsJsonPrimitive().isBoolean()) {
            return Expr.constant(element.getAsBoolean() ? 1f : 0f);
        }
        if (element.getAsJsonPrimitive().isNumber()) {
            return Expr.constant(element.getAsFloat());
        }
        return Expr.parse(element.getAsString());
    }

    private static Expr value(JsonObject json, String key) {
        return value(json.get(key));
    }

    static Expr text(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        String raw = element.getAsString();
        return raw.startsWith("=") ? Expr.parse(raw.substring(1)) : Expr.constant(raw);
    }

    private static int round(Expr expr, Scope scope, int fallback) {
        return expr == null ? fallback : Math.round(expr.number(scope));
    }

    private int color(Expr expr, DrawScope scope) {
        int argb = expr == null ? 0xFFFFFFFF : expr.color(scope);
        return opacity == null ? argb : Colors.alpha(argb, opacity.number(scope));
    }

    void draw(DrawScope scope, int bx, int by, int bw, int bh) {
        if (when != null && !when.truthy(scope)) {
            return;
        }
        int lx = bx + round(x, scope, 0);
        int ly = by + round(y, scope, 0);
        int lw = round(w, scope, bw);
        int lh = round(h, scope, bh);
        if (inset != null) {
            int i = round(inset, scope, 0);
            lx += i;
            ly += i;
            lw -= 2 * i;
            lh -= 2 * i;
        }
        float degrees = rotate == null ? 0f : rotate.number(scope);
        float factor = scale == null ? 1f : scale.number(scope);
        boolean transform = degrees != 0f || factor != 1f;
        Canvas c = scope.canvas;
        if (transform) {
            float cx = lx + lw / 2f;
            float cy = ly + lh / 2f;
            c.push();
            c.translate(cx, cy);
            if (degrees != 0f) {
                c.rotate(degrees);
            }
            if (factor != 1f) {
                c.scale(factor, factor);
            }
            c.translate(-cx, -cy);
        }
        int r = round(radius, scope, 0);
        switch (type) {
            case FILL -> c.fill(lx, ly, lw, lh, color(color, scope));
            case ROUND -> Draw.round(c, lx, ly, lw, lh, r, color(color, scope));
            case RING -> Draw.ring(c, lx, ly, lw, lh, r, color(color, scope));
            case PANEL -> Draw.panel(c, lx, ly, lw, lh, r, color(color, scope), color(border, scope));
            case GRADIENT -> c.gradient(lx, ly, lw, lh, color(top, scope), color(bottom, scope));
            case SHADOW -> Draw.shadow(c, lx, ly, lw, lh, r, round(spread, scope, 4), round(alpha, scope, 90));
            case SPRITE -> c.sprite(resource.string(scope), lx, ly, lw, lh);
            case TILED -> tiled(scope, lx, ly, lw, lh);
            case ICON -> scope.theme.icon(c, resource.string(scope), lx + lw / 2, ly + lh / 2, color(color, scope));
            case CHECK -> Icons.check(c, lx, ly, color(color, scope));
            case TEXT -> text(scope, lx, ly, lw, lh);
            case GROUP -> nested(scope, children, lx, ly, lw, lh);
            case WIDGET -> {
                Map<String, Object> state = new LinkedHashMap<>(scope.values);
                for (Map.Entry<String, Expr> entry : vars.entrySet()) {
                    state.put(entry.getKey(), entry.getValue().eval(scope));
                }
                scope.theme.render(widget, c, lx, ly, lw, lh, state);
            }
        }
        if (transform) {
            c.pop();
        }
    }

    private void tiled(DrawScope scope, int lx, int ly, int lw, int lh) {
        String path = resource.string(scope);
        int colon = path.indexOf(':');
        String namespace = colon < 0 ? "minecraft" : path.substring(0, colon);
        String file = colon < 0 ? path : path.substring(colon + 1);
        if (children != null && !scope.canvas.exists(namespace, file)) {
            nested(scope, children, lx, ly, lw, lh);
            return;
        }
        scope.canvas.tiled(namespace, file, lx, ly, lw, lh);
    }

    private void text(DrawScope scope, int lx, int ly, int lw, int lh) {
        Canvas c = scope.canvas;
        String value = resource.string(scope);
        String where = align == null ? "center" : align.string(scope);
        int width = c.textWidth(value);
        int tx = switch (where) {
            case "left" -> lx;
            case "right" -> lx + lw - width;
            default -> lx + (lw - width) / 2;
        };
        int ty = ly + (lh - c.lineHeight()) / 2 + 1;
        boolean drop = shadow == null ? scope.theme.shadowText() : shadow.truthy(scope);
        c.text(value, tx, ty, color(color, scope), drop);
    }

    private static void nested(DrawScope scope, List<Layer> layers, int lx, int ly, int lw, int lh) {
        Object oldW = scope.values.put("w", (float) lw);
        Object oldH = scope.values.put("h", (float) lh);
        for (Layer layer : layers) {
            layer.draw(scope, lx, ly, lw, lh);
        }
        scope.values.put("w", oldW);
        scope.values.put("h", oldH);
    }
}
