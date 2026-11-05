package com.github.olympus.proteus.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.screens.Screen;

public final class Mods {
    public static final String VOICECHAT = "voicechat";
    public static final String JI_AFK = "ji_afk";
    public static final String MALILIB = "malilib";

    public record Entry(String id, String probe, String takeover) {
        public boolean installed() {
            return PRESENT.computeIfAbsent(id, key -> {
                try {
                    Class.forName(probe, false, Mods.class.getClassLoader());
                    return true;
                } catch (ClassNotFoundException | LinkageError error) {
                    return false;
                }
            });
        }

        public String nameKey() {
            return "proteus.mods." + id;
        }

        public String tooltipKey() {
            return "proteus.mods." + id + ".tooltip";
        }
    }

    private static final Map<String, Boolean> PRESENT = new ConcurrentHashMap<>();
    private static final ScreenTakeover NONE = screen -> screen;
    private static final Map<String, ScreenTakeover> TAKEOVERS = new ConcurrentHashMap<>();
    private static final List<Entry> ENTRIES = List.of(
            new Entry(VOICECHAT, "de.maxhenkel.voicechat.Voicechat", "com.github.olympus.proteus.client.svc.SvcTakeover"),
            new Entry(JI_AFK, "com.ji.afkcinematic.config.SupportScreen", "com.github.olympus.proteus.client.ji.JiTakeover"),
            new Entry(MALILIB, "fi.dy.masa.malilib.gui.GuiBase", null));

    private Mods() {
    }

    public static List<Entry> installed() {
        List<Entry> list = new ArrayList<>();
        for (Entry entry : ENTRIES) {
            if (entry.installed()) {
                list.add(entry);
            }
        }
        return list;
    }

    public static Screen replace(Screen screen) {
        if (screen == null) {
            return null;
        }
        for (Entry entry : ENTRIES) {
            if (entry.takeover() == null || !entry.installed() || !UiPrefs.get().modEnabled(entry.id())) {
                continue;
            }
            ScreenTakeover takeover = TAKEOVERS.computeIfAbsent(entry.id(), key -> load(entry.takeover()));
            if (takeover != NONE) {
                Screen replaced;
                try {
                    replaced = takeover.replace(screen);
                } catch (LinkageError error) {
                    TAKEOVERS.put(entry.id(), NONE);
                    continue;
                }
                if (replaced != screen) {
                    return replaced;
                }
            }
        }
        return screen;
    }

    private static ScreenTakeover load(String name) {
        try {
            return (ScreenTakeover) Class.forName(name, true, Mods.class.getClassLoader()).getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException | LinkageError error) {
            return NONE;
        }
    }

    public static boolean active(String id) {
        for (Entry entry : ENTRIES) {
            if (entry.id().equals(id)) {
                return entry.installed() && UiPrefs.get().modEnabled(id);
            }
        }
        return false;
    }
}
