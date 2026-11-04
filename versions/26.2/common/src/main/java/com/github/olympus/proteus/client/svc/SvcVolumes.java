package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.Gui;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumeSlider.AdjustVolumeEntry;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class SvcVolumes {
    private static final float MAXIMUM = 4f;

    private final Map<String, Float> values = new HashMap<>();
    private final Map<String, AdjustVolumeEntry> entries = new HashMap<>();
    private final Set<String> dirty = new HashSet<>();

    void slider(Gui gui, String id, int x, int y, int w, int h, AdjustVolumeEntry entry) {
        entries.put(id, entry);
        float value = values.computeIfAbsent(id, key -> (float) (entry.get() / MAXIMUM));
        String label;
        if (value <= 0f) {
            label = Svc.text("message.voicechat.muted");
        } else {
            long amp = Math.round(value * MAXIMUM * 100f - 100f);
            label = Svc.text("message.voicechat.volume_amplification", (amp > 0 ? "+" : "") + amp + "%");
        }
        float level = 0f;
        double audio = entry.getAudioLevel();
        if (audio > AudioUtils.LOWEST_DB) {
            level = (float) AudioUtils.dbToPerc(audio + AudioUtils.linearToDb(value * MAXIMUM));
        }
        float changed = gui.valueSlider("svc-volume-" + id, x, y, w, h, label, value, level, true, null);
        if (changed != value) {
            values.put(id, changed);
            dirty.add(id);
        }
    }

    void apply() {
        for (String id : dirty) {
            AdjustVolumeEntry entry = entries.get(id);
            Float value = values.get(id);
            if (entry != null && value != null) {
                entry.save(value * MAXIMUM);
            }
        }
        dirty.clear();
    }
}
