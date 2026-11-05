package com.github.olympus.proteus.client.ji;

import com.ji.afkcinematic.config.ModConfig;
import com.ji.afkcinematic.input.KeySequenceTracker;
import com.mojang.blaze3d.platform.InputConstants;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

final class JiKeys {
    enum Slot {
        MENU("menu_keys"),
        TOGGLE("toggle_keys"),
        IMMEDIATE("immediate_keys");

        final String key;

        Slot(String key) {
            this.key = key;
        }
    }

    private static final int REJECTED_TICKS = 30;
    private static final Map<Integer, String> NAMES = names();

    private Slot slot;
    private boolean second;
    private int backupFirst;
    private int backupSecond;
    private long started;
    private int rejected;
    private Slot rejectedSlot;

    boolean listening() {
        return slot != null;
    }

    boolean listening(Slot target) {
        return slot == target;
    }

    void start(Slot target, ModConfig config) {
        cancel(config);
        slot = target;
        second = false;
        backupFirst = first(target, config);
        backupSecond = second(target, config);
        started = System.currentTimeMillis();
        KeySequenceTracker.startRebind();
    }

    void cancel(ModConfig config) {
        if (slot != null) {
            set(slot, config, backupFirst, backupSecond);
        }
        stop();
    }

    void stop() {
        slot = null;
        second = false;
        KeySequenceTracker.resetRebind();
    }

    void tick() {
        if (rejected > 0 && --rejected == 0) {
            rejectedSlot = null;
        }
    }

    boolean press(int code, boolean escape, ModConfig config) {
        if (slot == null) {
            return false;
        }
        if (escape) {
            if (second) {
                cancel(config);
            } else {
                set(slot, config, -1, -1);
                stop();
            }
            return true;
        }
        if (!second && System.currentTimeMillis() - started > KeySequenceTracker.SEQUENCE_TIMEOUT_MS) {
            cancel(config);
            return true;
        }
        if (second && KeySequenceTracker.getRebindRemainingMs() <= 0L) {
            cancel(config);
            return true;
        }
        int[] out = new int[2];
        int result = KeySequenceTracker.processRebindKey(code, out);
        if (result == -1) {
            cancel(config);
        } else if (result == KeySequenceTracker.UNSUPPORTED_KEY_REJECTED) {
            rejected = REJECTED_TICKS;
            rejectedSlot = slot;
        } else if (result == 1) {
            second = true;
        } else if (result == 2) {
            set(slot, config, out[0], out[1]);
            stop();
        }
        return true;
    }

    boolean release(int code, ModConfig config) {
        if (slot == null || !second) {
            return false;
        }
        int[] out = new int[2];
        int result = KeySequenceTracker.processRebindRelease(code, out);
        if (result == -1) {
            cancel(config);
        } else if (result == 2) {
            set(slot, config, out[0], out[1]);
            stop();
        }
        return true;
    }

    String label(Slot target, ModConfig config) {
        String name = Ji.text(target.key + ".label");
        if (slot == target) {
            return name + Ji.text(second ? "key_waiting_second" : "key_waiting_first");
        }
        if (rejected > 0 && rejectedSlot == target) {
            return name + Ji.proteus("proteus.ji.unsupported_key");
        }
        int one = first(target, config);
        int two = second(target, config);
        if (one == -1 && two == -1) {
            return name + Ji.text("keybind_disabled_label");
        }
        return name + format(one, two);
    }

    private static int first(Slot target, ModConfig config) {
        return switch (target) {
            case MENU -> config.menuKey1;
            case TOGGLE -> config.toggleKey1;
            case IMMEDIATE -> config.immediateKey1;
        };
    }

    private static int second(Slot target, ModConfig config) {
        return switch (target) {
            case MENU -> config.menuKey2;
            case TOGGLE -> config.toggleKey2;
            case IMMEDIATE -> config.immediateKey2;
        };
    }

    private static void set(Slot target, ModConfig config, int first, int second) {
        switch (target) {
            case MENU -> {
                config.menuKey1 = first;
                config.menuKey2 = second;
            }
            case TOGGLE -> {
                config.toggleKey1 = first;
                config.toggleKey2 = second;
            }
            case IMMEDIATE -> {
                config.immediateKey1 = first;
                config.immediateKey2 = second;
            }
        }
    }

    private static String format(int first, int second) {
        if (first == second) {
            return name(first);
        }
        return name(first) + " + " + name(second);
    }

    private static String name(int code) {
        if (code == -1) {
            return "NONE";
        }
        if (!KeySequenceTracker.isBindableKeyCode(code)) {
            return "Unsupported Key #" + code;
        }
        String name = NAMES.get(code);
        return name == null ? "Key " + code : name;
    }

    private static Map<Integer, String> names() {
        Map<Integer, String> map = new HashMap<>();
        for (Field field : InputConstants.class.getFields()) {
            String id = field.getName();
            if (!id.startsWith("KEY_") || field.getType() != int.class || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                map.putIfAbsent(field.getInt(null), title(id.substring(4)));
            } catch (IllegalAccessException error) {
                continue;
            }
        }
        return map;
    }

    private static String title(String id) {
        return switch (id) {
            case "LSHIFT", "RSHIFT" -> "Shift";
            case "LCONTROL", "RCONTROL" -> "Ctrl";
            case "LALT", "RALT" -> "Alt";
            case "PAGEUP" -> "Page Up";
            case "PAGEDOWN" -> "Page Down";
            case "CAPSLOCK" -> "Caps Lock";
            default -> id.length() <= 3 || id.matches("F\\d+") ? id : id.charAt(0) + id.substring(1).toLowerCase();
        };
    }
}
