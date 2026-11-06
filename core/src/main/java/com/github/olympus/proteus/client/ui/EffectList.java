package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ui.theme.Moment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class EffectList {
    private static final double STALE_SECONDS = 0.5;

    static final class Entry {
        EffectInfo info;
        float enter;
        float exit;
        boolean leaving;
        float pos;
        float from;
        float slot = -1f;
        float travel = 1f;

        void place(float target, boolean animated, Moment move, float dt, float speed) {
            if (slot < 0f || !animated) {
                slot = target;
                pos = target;
                from = target;
                travel = 1f;
                return;
            }
            if (move.spring()) {
                slot = target;
                pos = move.advance(pos, target, dt, speed);
                return;
            }
            if (target != slot) {
                from = pos;
                slot = target;
                travel = 0f;
            }
            travel = move.advance(travel, 1f, dt, speed);
            pos = from + (target - from) * move.value(travel);
        }
    }

    private List<Entry> entries = new ArrayList<>();
    private Object owner;
    private long last;
    private boolean primed;

    List<Entry> entries() {
        return entries;
    }

    void clear() {
        entries = new ArrayList<>();
        owner = null;
        last = 0L;
        primed = false;
    }

    float step() {
        long now = System.nanoTime();
        double gap = last == 0L ? Double.MAX_VALUE : (now - last) / 1_000_000_000.0;
        last = now;
        if (gap > STALE_SECONDS) {
            entries = new ArrayList<>();
            primed = false;
            return 0f;
        }
        return Math.min(0.1f, (float) gap);
    }

    void sync(Object source, List<EffectInfo> live, float dt, boolean animateIn, Moment in, boolean animateOut,
            Moment out, float speed) {
        if (source != owner) {
            entries = new ArrayList<>();
            primed = false;
            owner = source;
        }
        Map<String, Entry> known = new HashMap<>();
        for (Entry entry : entries) {
            known.put(entry.info.id(), entry);
        }
        List<Entry> next = new ArrayList<>();
        for (EffectInfo info : live) {
            Entry entry = known.remove(info.id());
            if (entry == null) {
                entry = new Entry();
                entry.enter = animateIn && primed ? 0f : 1f;
            }
            entry.info = info;
            entry.leaving = false;
            next.add(entry);
        }
        for (Entry entry : entries) {
            if (known.containsKey(entry.info.id())) {
                entry.leaving = true;
                next.add(entry);
            }
        }
        primed = true;
        entries = next;
        entries.removeIf(entry -> {
            entry.enter = animateIn ? in.advance(entry.enter, 1f, dt, speed) : 1f;
            if (animateOut) {
                entry.exit = out.advance(entry.exit, entry.leaving ? 1f : 0f, dt, speed);
            } else {
                entry.exit = entry.leaving ? 1f : 0f;
            }
            return entry.leaving && entry.exit >= 1f;
        });
    }
}
