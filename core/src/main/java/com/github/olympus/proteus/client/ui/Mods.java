package com.github.olympus.proteus.client.ui;

import java.util.ArrayList;
import java.util.List;

public final class Mods {
    public static final String VOICECHAT = "voicechat";

    public record Entry(String id, String probe) {
        public boolean installed() {
            try {
                Class.forName(probe, false, Mods.class.getClassLoader());
                return true;
            } catch (ClassNotFoundException | LinkageError error) {
                return false;
            }
        }

        public String nameKey() {
            return "proteus.mods." + id;
        }

        public String tooltipKey() {
            return "proteus.mods." + id + ".tooltip";
        }
    }

    private static final List<Entry> ENTRIES = List.of(new Entry(VOICECHAT, "de.maxhenkel.voicechat.Voicechat"));

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

    public static boolean active(String id) {
        for (Entry entry : ENTRIES) {
            if (entry.id().equals(id)) {
                return entry.installed() && UiPrefs.get().modEnabled(id);
            }
        }
        return false;
    }
}
