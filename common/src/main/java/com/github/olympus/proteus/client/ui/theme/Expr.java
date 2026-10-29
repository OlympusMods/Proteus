package com.github.olympus.proteus.client.ui.theme;

import com.github.olympus.proteus.client.ui.Colors;
import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface Expr {
    record Color(int argb) {
    }

    Object eval(Scope scope);

    default float number(Scope scope) {
        return toNumber(eval(scope));
    }

    default int color(Scope scope) {
        Object value = eval(scope);
        if (value instanceof Color color) {
            return color.argb();
        }
        throw new ExprException("expected a color but got " + describe(value));
    }

    default String string(Scope scope) {
        Object value = eval(scope);
        return value instanceof Float number ? formatNumber(number) : String.valueOf(value);
    }

    default boolean truthy(Scope scope) {
        return truth(eval(scope));
    }

    static Expr constant(Object value) {
        return scope -> value;
    }

    static Expr parse(String source) {
        return new Parser(source).parseAll();
    }

    private static String formatNumber(float number) {
        return number == Math.rint(number) ? Integer.toString((int) number) : Float.toString(number);
    }

    private static String describe(Object value) {
        if (value instanceof Float) {
            return "a number";
        }
        if (value instanceof Color) {
            return "a color";
        }
        return "a string";
    }

    private static float toNumber(Object value) {
        if (value instanceof Float number) {
            return number;
        }
        throw new ExprException("expected a number but got " + describe(value));
    }

    private static Color toColor(Object value) {
        if (value instanceof Color color) {
            return color;
        }
        throw new ExprException("expected a color but got " + describe(value));
    }

    private static boolean truth(Object value) {
        if (value instanceof Float number) {
            return number != 0f;
        }
        if (value instanceof String text) {
            return !text.isEmpty();
        }
        return value != null;
    }

    private static Float bool(boolean value) {
        return value ? 1f : 0f;
    }

    final class Parser {
        private final String source;
        private int pos;

        private Parser(String source) {
            this.source = source;
        }

        private ExprException error(String message) {
            return new ExprException(message + " at column " + (pos + 1) + " in \"" + source + "\"");
        }

        private void skip() {
            while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) {
                pos++;
            }
        }

        private boolean eat(String token) {
            skip();
            if (source.startsWith(token, pos)) {
                pos += token.length();
                return true;
            }
            return false;
        }

        private void expect(String token) {
            if (!eat(token)) {
                throw error("expected '" + token + "'");
            }
        }

        Expr parseAll() {
            Expr expr = or();
            skip();
            if (pos < source.length()) {
                throw error("unexpected '" + source.charAt(pos) + "'");
            }
            return expr;
        }

        private Expr or() {
            Expr left = and();
            while (eat("||")) {
                Expr a = left;
                Expr b = and();
                left = scope -> bool(truth(a.eval(scope)) || truth(b.eval(scope)));
            }
            return left;
        }

        private Expr and() {
            Expr left = equality();
            while (eat("&&")) {
                Expr a = left;
                Expr b = equality();
                left = scope -> bool(truth(a.eval(scope)) && truth(b.eval(scope)));
            }
            return left;
        }

        private Expr equality() {
            Expr left = comparison();
            while (true) {
                if (eat("==")) {
                    Expr a = left;
                    Expr b = comparison();
                    left = scope -> bool(a.eval(scope).equals(b.eval(scope)));
                } else if (eat("!=")) {
                    Expr a = left;
                    Expr b = comparison();
                    left = scope -> bool(!a.eval(scope).equals(b.eval(scope)));
                } else {
                    return left;
                }
            }
        }

        private Expr comparison() {
            Expr left = additive();
            while (true) {
                String op = eat("<=") ? "<=" : eat(">=") ? ">=" : eat("<") ? "<" : eat(">") ? ">" : null;
                if (op == null) {
                    return left;
                }
                Expr a = left;
                Expr b = additive();
                left = switch (op) {
                    case "<=" -> scope -> bool(a.number(scope) <= b.number(scope));
                    case ">=" -> scope -> bool(a.number(scope) >= b.number(scope));
                    case "<" -> scope -> bool(a.number(scope) < b.number(scope));
                    default -> scope -> bool(a.number(scope) > b.number(scope));
                };
            }
        }

        private Expr additive() {
            Expr left = multiplicative();
            while (true) {
                if (eat("+")) {
                    Expr a = left;
                    Expr b = multiplicative();
                    left = scope -> {
                        Object x = a.eval(scope);
                        Object y = b.eval(scope);
                        if (x instanceof String || y instanceof String) {
                            return constant(x).string(scope) + constant(y).string(scope);
                        }
                        return toNumber(x) + toNumber(y);
                    };
                } else if (eat("-")) {
                    Expr a = left;
                    Expr b = multiplicative();
                    left = scope -> a.number(scope) - b.number(scope);
                } else {
                    return left;
                }
            }
        }

        private Expr multiplicative() {
            Expr left = unary();
            while (true) {
                if (eat("*")) {
                    Expr a = left;
                    Expr b = unary();
                    left = scope -> a.number(scope) * b.number(scope);
                } else if (eat("/")) {
                    Expr a = left;
                    Expr b = unary();
                    left = scope -> a.number(scope) / b.number(scope);
                } else if (eat("%")) {
                    Expr a = left;
                    Expr b = unary();
                    left = scope -> a.number(scope) % b.number(scope);
                } else {
                    return left;
                }
            }
        }

        private Expr unary() {
            if (eat("-")) {
                Expr a = unary();
                return scope -> -a.number(scope);
            }
            if (eat("!")) {
                Expr a = unary();
                return scope -> bool(!truth(a.eval(scope)));
            }
            return primary();
        }

        private Expr primary() {
            skip();
            if (pos >= source.length()) {
                throw error("unexpected end");
            }
            char c = source.charAt(pos);
            if (c == '(') {
                pos++;
                Expr inner = or();
                expect(")");
                return inner;
            }
            if (c == '#') {
                return hex();
            }
            if (c == '"') {
                return string();
            }
            if (Character.isDigit(c) || c == '.') {
                return number();
            }
            if (Character.isLetter(c) || c == '_') {
                String name = identifier();
                if (eat("(")) {
                    return call(name);
                }
                return scope -> {
                    Object value = scope.lookup(name);
                    if (value == null) {
                        throw new ExprException("unknown name '" + name + "'");
                    }
                    return value;
                };
            }
            throw error("unexpected '" + c + "'");
        }

        private Expr hex() {
            int start = ++pos;
            while (pos < source.length() && Character.digit(source.charAt(pos), 16) >= 0) {
                pos++;
            }
            String digits = source.substring(start, pos);
            if (digits.length() != 6 && digits.length() != 8) {
                throw error("colors are #RRGGBB or #AARRGGBB");
            }
            int argb = (int) Long.parseLong(digits, 16);
            Color color = new Color(digits.length() == 6 ? 0xFF000000 | argb : argb);
            return constant(color);
        }

        private Expr string() {
            int start = ++pos;
            while (pos < source.length() && source.charAt(pos) != '"') {
                pos++;
            }
            if (pos >= source.length()) {
                throw error("unclosed string");
            }
            String text = source.substring(start, pos++);
            return constant(text);
        }

        private Expr number() {
            int start = pos;
            while (pos < source.length() && (Character.isDigit(source.charAt(pos)) || source.charAt(pos) == '.')) {
                pos++;
            }
            try {
                return constant(Float.parseFloat(source.substring(start, pos)));
            } catch (NumberFormatException exception) {
                throw error("bad number");
            }
        }

        private String identifier() {
            int start = pos;
            while (pos < source.length()
                    && (Character.isLetterOrDigit(source.charAt(pos)) || source.charAt(pos) == '_')) {
                pos++;
            }
            return source.substring(start, pos);
        }

        private Expr call(String name) {
            List<Expr> args = new ArrayList<>();
            if (!eat(")")) {
                do {
                    args.add(or());
                } while (eat(","));
                expect(")");
            }
            Expr[] a = args.toArray(new Expr[0]);
            return switch (name) {
                case "mix" -> {
                    arity(name, a, 3);
                    yield scope -> {
                        Object from = a[0].eval(scope);
                        Object to = a[1].eval(scope);
                        float t = a[2].number(scope);
                        if (from instanceof Color || to instanceof Color) {
                            return new Color(Colors.mix(toColor(from).argb(), toColor(to).argb(), t));
                        }
                        float x = toNumber(from);
                        return x + (toNumber(to) - x) * t;
                    };
                }
                case "lighten" -> {
                    arity(name, a, 2);
                    yield scope -> new Color(Colors.lighten(a[0].color(scope), a[1].number(scope)));
                }
                case "darken" -> {
                    arity(name, a, 2);
                    yield scope -> new Color(Colors.darken(a[0].color(scope), a[1].number(scope)));
                }
                case "alpha" -> {
                    arity(name, a, 2);
                    yield scope -> new Color(Colors.alpha(a[0].color(scope), a[1].number(scope)));
                }
                case "hsl" -> {
                    arity(name, a, 3);
                    yield scope -> new Color(Colors.hsl(a[0].number(scope), a[1].number(scope), a[2].number(scope)));
                }
                case "min" -> {
                    arity(name, a, 2);
                    yield scope -> Math.min(a[0].number(scope), a[1].number(scope));
                }
                case "max" -> {
                    arity(name, a, 2);
                    yield scope -> Math.max(a[0].number(scope), a[1].number(scope));
                }
                case "clamp" -> {
                    arity(name, a, 3);
                    yield scope -> Math.max(a[1].number(scope), Math.min(a[2].number(scope), a[0].number(scope)));
                }
                case "floor" -> {
                    arity(name, a, 1);
                    yield scope -> (float) Math.floor(a[0].number(scope));
                }
                case "round" -> {
                    arity(name, a, 1);
                    yield scope -> (float) Math.round(a[0].number(scope));
                }
                case "step" -> {
                    arity(name, a, 2);
                    yield scope -> bool(a[0].number(scope) >= a[1].number(scope));
                }
                case "if" -> {
                    arity(name, a, 3);
                    yield scope -> a[0].truthy(scope) ? a[1].eval(scope) : a[2].eval(scope);
                }
                case "exists" -> {
                    arity(name, a, 1);
                    yield scope -> bool(scope.exists(a[0].string(scope)));
                }
                default -> throw error("unknown function '" + name + "'");
            };
        }

        private void arity(String name, Expr[] args, int count) {
            if (args.length != count) {
                throw error(name + " takes " + count + " arguments");
            }
        }
    }
}
