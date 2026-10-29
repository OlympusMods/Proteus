package com.github.olympus.proteus.client.ui.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.olympus.proteus.client.ui.Colors;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExprTest {
    private static final Scope SCOPE = Scope.of(Map.of(
            "accent", new Expr.Color(0xFF8B5CF6),
            "hover", 0.5f,
            "enabled", 0f,
            "time", 1.5f));

    private static Object eval(String source) {
        return Expr.parse(source).eval(SCOPE);
    }

    @Test
    void parsesArgbHex() {
        assertEquals(new Expr.Color(0xFF8B5CF6), eval("#FF8B5CF6"));
    }

    @Test
    void sixDigitHexIsOpaque() {
        assertEquals(new Expr.Color(0xFF8B5CF6), eval("#8B5CF6"));
    }

    @Test
    void respectsPrecedence() {
        assertEquals(7f, eval("1 + 2 * 3"));
        assertEquals(9f, eval("(1 + 2) * 3"));
        assertEquals(-2f, eval("-2"));
    }

    @Test
    void mixesColorsLikeColors() {
        assertEquals(new Expr.Color(Colors.mix(0xFF000000, 0xFFFFFFFF, 0.5f)), eval("mix(#000000, #FFFFFF, 0.5)"));
    }

    @Test
    void appliesAlphaToNamedColor() {
        assertEquals(new Expr.Color(Colors.alpha(0xFF8B5CF6, 0.28f * 0.5f)), eval("alpha(accent, 0.28 * hover)"));
    }

    @Test
    void lightensAndDarkens() {
        assertEquals(new Expr.Color(Colors.lighten(0xFF8B5CF6, 0.18f * 0.5f)), eval("lighten(accent, 0.18 * hover)"));
        assertEquals(new Expr.Color(Colors.darken(0xFF8B5CF6, 0.2f)), eval("darken(accent, 0.2)"));
    }

    @Test
    void ifPicksBranch() {
        assertEquals(0.45f, eval("if(enabled, 1, 0.45)"));
        assertEquals(1f, eval("if(hover > 0.2, 1, unknown)"));
    }

    @Test
    void moduloIsFloat() {
        assertEquals(1.5f * 0.9f % 1f, eval("(time * 0.9) % 1"));
    }

    @Test
    void concatenatesStrings() {
        assertEquals("classic/button", eval("\"classic/\" + \"button\""));
    }

    @Test
    void stepAndComparisons() {
        assertEquals(1f, eval("step(0.6, 0.5)"));
        assertEquals(0f, eval("step(0.4, 0.5)"));
        assertEquals(1f, eval("hover >= 0.5 && !enabled"));
        assertEquals(1f, eval("\"a\" == \"a\""));
    }

    @Test
    void numericHelpers() {
        assertEquals(12f, eval("max(12, floor(30 / 4))"));
        assertEquals(3f, eval("round(2.5)"));
        assertEquals(1f, eval("clamp(4, 0, 1)"));
        assertEquals(new Expr.Color(Colors.hsl(0.5f, 0.75f, 0.5f)), eval("hsl(0.5, 0.75, 0.5)"));
    }

    @Test
    void existsAsksScope() {
        Scope scope = new Scope() {
            @Override
            public Object lookup(String name) {
                return null;
            }

            @Override
            public boolean exists(String resource) {
                return resource.equals("proteus:textures/a.png");
            }
        };
        assertEquals(1f, Expr.parse("exists(\"proteus:textures/a.png\")").eval(scope));
        assertEquals(0f, Expr.parse("exists(\"proteus:textures/b.png\")").eval(scope));
    }

    @Test
    void chainedScopeUsesFirstHit() {
        Scope scope = Scope.chain(Scope.of(Map.of("a", 1f)), Scope.of(Map.of("a", 2f, "b", 3f)));
        assertEquals(4f, Expr.parse("a + b").eval(scope));
    }

    @Test
    void unknownNameThrows() {
        ExprException error = assertThrows(ExprException.class, () -> eval("nope + 1"));
        assertTrue(error.getMessage().contains("nope"));
    }

    @Test
    void syntaxErrorNamesColumn() {
        ExprException error = assertThrows(ExprException.class, () -> Expr.parse("mix(1,"));
        assertTrue(error.getMessage().contains("column"), error.getMessage());
    }

    @Test
    void typedAccessors() {
        Expr expr = Expr.parse("alpha(accent, 1)");
        assertEquals(0xFF8B5CF6, expr.color(SCOPE));
        assertEquals(2.5f, Expr.parse("1.5 + 1").number(SCOPE));
        assertTrue(Expr.parse("hover").truthy(SCOPE));
        assertEquals("x", Expr.constant("x").string(SCOPE));
    }
}
