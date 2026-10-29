package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ui.theme.StyledTheme;
import com.github.olympus.proteus.client.ui.theme.ThemeSources;
import com.github.olympus.proteus.client.ui.theme.ThemeSpec;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ThemeRegistry {
    public static final String MODERN = "proteus:modern";
    public static final String CLASSIC = "proteus:classic";

    private record Styled(JsonObject spec, Function<ThemeSpec, ? extends StyledTheme> factory) {
    }

    private final Map<String, Supplier<Theme>> scratch = new LinkedHashMap<>();
    private final Map<String, Styled> styled = new LinkedHashMap<>();
    private Map<String, JsonObject> files = new HashMap<>();
    private Consumer<String> warn = message -> {
    };
    private volatile Map<String, Theme> built;

    public synchronized void register(String id, Supplier<Theme> factory) {
        scratch.put(id, factory);
        built = null;
    }

    public synchronized void registerStyled(String id, JsonObject spec,
            Function<ThemeSpec, ? extends StyledTheme> factory) {
        styled.put(id, new Styled(spec.deepCopy(), factory));
        built = null;
    }

    public synchronized void reload(Map<String, JsonObject> files, Consumer<String> warn) {
        this.files = new HashMap<>(files);
        this.warn = warn;
        built = build();
    }

    public static Map<String, JsonObject> parseAll(Map<String, String> sources, Consumer<String> warn) {
        Map<String, JsonObject> parsed = new TreeMap<>();
        for (Map.Entry<String, String> entry : new TreeMap<>(sources).entrySet()) {
            try {
                parsed.put(entry.getKey(), JsonParser.parseString(entry.getValue()).getAsJsonObject());
            } catch (RuntimeException exception) {
                warn.accept(entry.getKey() + ": " + exception.getMessage());
            }
        }
        return parsed;
    }

    private static JsonObject bundled(String name) {
        try (InputStream stream = ThemeRegistry.class.getResourceAsStream(
                "/assets/proteus/proteus/themes/" + name + ".json")) {
            if (stream == null) {
                return new JsonObject();
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (IOException | RuntimeException exception) {
            return new JsonObject();
        }
    }

    private Map<String, Theme> build() {
        Map<String, JsonObject> raw = new HashMap<>();
        raw.put(MODERN, bundled("modern"));
        raw.put(CLASSIC, bundled("classic"));
        for (Map.Entry<String, Styled> entry : styled.entrySet()) {
            raw.put(entry.getKey(), entry.getValue().spec());
        }
        for (Map.Entry<String, JsonObject> entry : files.entrySet()) {
            Styled mod = styled.get(entry.getKey());
            JsonObject base = mod != null ? mod.spec() : raw.get(entry.getKey());
            boolean builtIn = entry.getKey().equals(MODERN) || entry.getKey().equals(CLASSIC);
            raw.put(entry.getKey(), mod != null || builtIn ? ThemeSources.merge(base, entry.getValue()) : entry.getValue());
        }
        ThemeSources sources = new ThemeSources(raw, warn);
        Map<String, ThemeSpec> specs = new HashMap<>();
        Map<String, Theme> themes = new LinkedHashMap<>();
        for (String id : raw.keySet()) {
            ThemeSpec spec = spec(id, raw, sources, specs, new HashSet<>());
            Styled mod = styled.get(id);
            Theme theme;
            try {
                theme = mod != null ? mod.factory().apply(spec) : new StyledTheme(spec);
            } catch (RuntimeException exception) {
                warn.accept(id + ": " + exception.getMessage());
                theme = new StyledTheme(spec);
            }
            themes.put(id, theme);
        }
        for (Map.Entry<String, Supplier<Theme>> entry : scratch.entrySet()) {
            if (themes.containsKey(entry.getKey())) {
                continue;
            }
            try {
                themes.put(entry.getKey(), entry.getValue().get());
            } catch (RuntimeException exception) {
                warn.accept(entry.getKey() + ": " + exception.getMessage());
            }
        }
        return themes;
    }

    private ThemeSpec spec(String id, Map<String, JsonObject> raw, ThemeSources sources, Map<String, ThemeSpec> specs,
            Set<String> visiting) {
        ThemeSpec done = specs.get(id);
        if (done != null) {
            return done;
        }
        ThemeSpec fallback = null;
        if (!id.equals(MODERN)) {
            JsonObject own = raw.get(id);
            String parent = own.has("parent") && own.get("parent").isJsonPrimitive()
                    ? own.get("parent").getAsString()
                    : MODERN;
            visiting.add(id);
            if (!raw.containsKey(parent) || visiting.contains(parent)) {
                parent = MODERN;
            }
            fallback = spec(parent, raw, sources, specs, visiting);
            visiting.remove(id);
        }
        ThemeSpec spec = ThemeSpec.parse(id, sources.resolve(id), fallback, warn);
        specs.put(id, spec);
        return spec;
    }

    private Map<String, Theme> themes() {
        Map<String, Theme> themes = built;
        if (themes == null) {
            synchronized (this) {
                if (built == null) {
                    built = build();
                }
                themes = built;
            }
        }
        return themes;
    }

    public List<Theme> all() {
        Map<String, Theme> themes = themes();
        List<Theme> list = new ArrayList<>();
        list.add(themes.get(MODERN));
        list.add(themes.get(CLASSIC));
        List<Map.Entry<String, Theme>> rest = new ArrayList<>();
        for (Map.Entry<String, Theme> entry : themes.entrySet()) {
            if (!entry.getKey().equals(MODERN) && !entry.getKey().equals(CLASSIC)) {
                rest.add(entry);
            }
        }
        rest.sort((a, b) -> {
            int byName = a.getValue().name().compareToIgnoreCase(b.getValue().name());
            return byName != 0 ? byName : a.getKey().compareTo(b.getKey());
        });
        for (Map.Entry<String, Theme> entry : rest) {
            list.add(entry.getValue());
        }
        return list;
    }

    public Theme get(String id) {
        return themes().get(id);
    }

    public String id(Theme theme) {
        for (Map.Entry<String, Theme> entry : themes().entrySet()) {
            if (entry.getValue() == theme) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Theme current(String selected, String accentChoice, int accentColor) {
        Theme theme = get(selected);
        if (theme == null) {
            theme = get(MODERN);
        }
        if (!theme.accentLocked()) {
            theme.accent("theme".equals(accentChoice) ? theme.defaultAccent() : accentColor);
        }
        return theme;
    }
}
