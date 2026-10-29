package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ui.theme.StyledTheme;
import com.github.olympus.proteus.client.ui.theme.ThemeSpec;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Themes {
    public static final String MODERN = ThemeRegistry.MODERN;
    public static final String CLASSIC = ThemeRegistry.CLASSIC;

    private static final ThemeRegistry REGISTRY = new ThemeRegistry();

    private Themes() {
    }

    public static void register(String id, Supplier<Theme> factory) {
        REGISTRY.register(id, factory);
    }

    public static void registerStyled(String id, JsonObject spec) {
        REGISTRY.registerStyled(id, spec, StyledTheme::new);
    }

    public static void registerStyled(String id, JsonObject spec, Function<ThemeSpec, ? extends StyledTheme> factory) {
        REGISTRY.registerStyled(id, spec, factory);
    }

    public static void reload(Map<String, JsonObject> files, Consumer<String> warn) {
        REGISTRY.reload(files, warn);
    }

    public static List<Theme> all() {
        return REGISTRY.all();
    }

    public static Theme get(String id) {
        return REGISTRY.get(id);
    }

    public static String id(Theme theme) {
        return REGISTRY.id(theme);
    }

    public static Theme current() {
        UiPrefs prefs = UiPrefs.get();
        return REGISTRY.current(prefs.classic() ? CLASSIC : MODERN, "preset", prefs.accentColor());
    }
}
