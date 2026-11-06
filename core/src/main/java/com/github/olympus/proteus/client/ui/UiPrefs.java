package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ProteusClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class UiPrefs {
    public static final int[] ACCENTS = {
        0xFF8B5CF6, 0xFF3B82F6, 0xFF14B8A6, 0xFF22C55E, 0xFFF59E0B, 0xFFF43F5E
    };
    public static final String[] ACCENT_KEYS = {"violet", "blue", "teal", "green", "amber", "rose"};

    public static final String THEME = "theme";
    public static final String CUSTOM = "custom";
    public static final String CHROMA = "chroma";

    private static final String FILE = "ui.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static UiPrefs instance;

    private String theme = Themes.MODERN;
    private String accent = THEME;
    private float hue = 0.72f;
    private float saturation = 0.75f;
    private float lightness = 0.5f;
    private int toastCorner;
    private int toastSeconds = 5;
    private boolean blur = true;
    private boolean animations = true;
    private final Set<ToastKind> vanillaToasts = EnumSet.noneOf(ToastKind.class);
    private final Set<String> disabledMods = new HashSet<>();
    private final Set<Surface> surfaces = defaultSurfaces();
    private final Map<String, Integer> options = new LinkedHashMap<>();

    private UiPrefs() {
    }

    public static synchronized UiPrefs get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public String theme() {
        return theme;
    }

    public void setTheme(String id) {
        theme = id;
        save();
    }

    public String accentChoice() {
        return accent;
    }

    public void setAccentChoice(String choice) {
        accent = validAccent(choice);
        save();
    }

    private static String validAccent(String choice) {
        if (THEME.equals(choice) || CUSTOM.equals(choice) || CHROMA.equals(choice)) {
            return choice;
        }
        for (String key : ACCENT_KEYS) {
            if (key.equals(choice)) {
                return choice;
            }
        }
        return THEME;
    }

    public int accentColor() {
        if (CHROMA.equals(accent)) {
            return chromaColor();
        }
        if (CUSTOM.equals(accent)) {
            return customColor();
        }
        for (int i = 0; i < ACCENT_KEYS.length; i++) {
            if (ACCENT_KEYS[i].equals(accent)) {
                return ACCENTS[i];
            }
        }
        return ACCENTS[0];
    }

    public int chromaColor() {
        return Colors.hsl((float) (System.nanoTime() / 1_000_000_000.0 * 0.12 % 1.0), 0.75f, 0.55f);
    }

    public int customColor() {
        return Colors.hsl(hue, 0.3f + 0.7f * saturation, 0.3f + 0.4f * lightness);
    }

    public float hue() {
        return hue;
    }

    public float saturation() {
        return saturation;
    }

    public float lightness() {
        return lightness;
    }

    public void setCustom(float hue, float saturation, float lightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.lightness = lightness;
    }

    public int toastCorner() {
        return toastCorner;
    }

    public void setToastCorner(int corner) {
        toastCorner = Math.max(0, Math.min(3, corner));
        save();
    }

    public int toastSeconds() {
        return toastSeconds;
    }

    public void setToastSeconds(int seconds) {
        toastSeconds = Math.max(2, Math.min(15, seconds));
        save();
    }

    public boolean blur() {
        return blur;
    }

    public void setBlur(boolean value) {
        blur = value;
        save();
    }

    public boolean animations() {
        return animations;
    }

    public void setAnimations(boolean value) {
        animations = value;
        save();
    }

    private static Set<Surface> defaultSurfaces() {
        Set<Surface> set = EnumSet.noneOf(Surface.class);
        for (Surface surface : Surface.values()) {
            if (surface.defaultOn()) {
                set.add(surface);
            }
        }
        return set;
    }

    public boolean surface(Surface surface) {
        return surfaces.contains(surface);
    }

    public void setSurface(Surface surface, boolean value) {
        if (value) {
            surfaces.add(surface);
        } else {
            surfaces.remove(surface);
        }
        save();
    }

    public int option(String key, int fallback) {
        return options.getOrDefault(key, fallback);
    }

    public void setOption(String key, int value) {
        options.put(key, value);
        save();
    }

    public boolean flag(String key, boolean fallback) {
        return option(key, fallback ? 1 : 0) != 0;
    }

    public void setFlag(String key, boolean value) {
        setOption(key, value ? 1 : 0);
    }

    public boolean replaces(ToastKind kind) {
        return surface(Surface.TOASTS) && !vanillaToasts.contains(kind);
    }

    public boolean kindReplaced(ToastKind kind) {
        return !vanillaToasts.contains(kind);
    }

    public void setReplaces(ToastKind kind, boolean value) {
        if (value) {
            vanillaToasts.remove(kind);
        } else {
            vanillaToasts.add(kind);
        }
        save();
    }

    public boolean modEnabled(String id) {
        return !disabledMods.contains(id);
    }

    public void setModEnabled(String id, boolean value) {
        if (value) {
            disabledMods.remove(id);
        } else {
            disabledMods.add(id);
        }
        save();
    }

    private static Path file() {
        return ProteusClient.configDirectory().resolve(ProteusClient.MOD_ID).resolve(FILE);
    }

    private static UiPrefs load() {
        Path path = file();
        if (!Files.isRegularFile(path)) {
            return new UiPrefs();
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return fromJson(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (IOException | RuntimeException exception) {
            ProteusClient.LOGGER.warn("Could not read {}: {}", path, exception.getMessage());
            return new UiPrefs();
        }
    }

    static UiPrefs fromJson(JsonObject object) {
        UiPrefs prefs = new UiPrefs();
        if (object.has("theme")) {
            prefs.theme = object.get("theme").getAsString();
        } else if (object.has("style") && "classic".equals(object.get("style").getAsString())) {
            prefs.theme = Themes.CLASSIC;
        }
        if (object.has("accent")) {
            JsonPrimitive accent = object.getAsJsonPrimitive("accent");
            if (accent.isNumber()) {
                int index = accent.getAsInt();
                prefs.accent = index >= 0 && index < ACCENT_KEYS.length ? ACCENT_KEYS[index]
                        : index == ACCENT_KEYS.length ? CUSTOM
                        : index == ACCENT_KEYS.length + 1 ? CHROMA
                        : THEME;
            } else {
                prefs.accent = validAccent(accent.getAsString());
            }
        }
        if (object.has("hue")) {
            prefs.hue = object.get("hue").getAsFloat();
            prefs.saturation = object.get("saturation").getAsFloat();
            prefs.lightness = object.get("lightness").getAsFloat();
        }
        if (object.has("toastCorner")) {
            prefs.toastCorner = Math.max(0, Math.min(3, object.get("toastCorner").getAsInt()));
        }
        if (object.has("toastSeconds")) {
            prefs.toastSeconds = Math.max(2, Math.min(15, object.get("toastSeconds").getAsInt()));
        }
        if (object.has("blur")) {
            prefs.blur = object.get("blur").getAsBoolean();
        }
        if (object.has("animations")) {
            prefs.animations = object.get("animations").getAsBoolean();
        }
        if (object.has("toasts")) {
            JsonObject toasts = object.getAsJsonObject("toasts");
            for (ToastKind kind : ToastKind.values()) {
                if (toasts.has(kind.key()) && "vanilla".equals(toasts.get(kind.key()).getAsString())) {
                    prefs.vanillaToasts.add(kind);
                }
            }
        }
        if (object.has("surfaces")) {
            JsonObject saved = object.getAsJsonObject("surfaces");
            for (Surface surface : Surface.values()) {
                if (saved.has(surface.key())) {
                    prefs.setSurfaceQuiet(surface, saved.get(surface.key()).getAsBoolean());
                }
            }
        }
        if (object.has("options")) {
            JsonObject saved = object.getAsJsonObject("options");
            for (Map.Entry<String, JsonElement> entry : saved.entrySet()) {
                prefs.options.put(entry.getKey(), entry.getValue().getAsInt());
            }
        }
        if (object.has("mods")) {
            JsonObject mods = object.getAsJsonObject("mods");
            for (String id : mods.keySet()) {
                if (!mods.get(id).getAsBoolean()) {
                    prefs.disabledMods.add(id);
                }
            }
        }
        return prefs;
    }

    private void setSurfaceQuiet(Surface surface, boolean value) {
        if (value) {
            surfaces.add(surface);
        } else {
            surfaces.remove(surface);
        }
    }

    JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("theme", theme);
        object.addProperty("accent", accent);
        object.addProperty("hue", hue);
        object.addProperty("saturation", saturation);
        object.addProperty("lightness", lightness);
        object.addProperty("toastCorner", toastCorner);
        object.addProperty("toastSeconds", toastSeconds);
        object.addProperty("blur", blur);
        object.addProperty("animations", animations);
        JsonObject toasts = new JsonObject();
        for (ToastKind kind : ToastKind.values()) {
            toasts.addProperty(kind.key(), vanillaToasts.contains(kind) ? "vanilla" : "proteus");
        }
        object.add("toasts", toasts);
        JsonObject saved = new JsonObject();
        for (Surface surface : Surface.values()) {
            saved.addProperty(surface.key(), surfaces.contains(surface));
        }
        object.add("surfaces", saved);
        JsonObject extra = new JsonObject();
        for (Map.Entry<String, Integer> entry : options.entrySet()) {
            extra.addProperty(entry.getKey(), entry.getValue());
        }
        object.add("options", extra);
        JsonObject mods = new JsonObject();
        for (String id : disabledMods) {
            mods.addProperty(id, false);
        }
        object.add("mods", mods);
        return object;
    }

    public void save() {
        Path path = file();
        JsonObject object = toJson();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(object, writer);
            }
        } catch (IOException exception) {
            ProteusClient.LOGGER.warn("Could not save {}: {}", path, exception.getMessage());
        }
    }
}
