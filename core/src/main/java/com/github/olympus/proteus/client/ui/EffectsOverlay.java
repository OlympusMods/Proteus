package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ProteusClient;
import com.github.olympus.proteus.client.ui.theme.Moment;
import com.github.olympus.proteus.client.ui.theme.MotionSpec;
import java.util.ArrayList;
import java.util.List;

public final class EffectsOverlay {
    public static final String HUD = "effects.hud";
    public static final String INVENTORY = "effects.inventory";
    public static final String COMPACT = "effects.compact";
    public static final String HUD_TIME = "effects.hud_time";

    private static final FadeCanvas FADE = new FadeCanvas();
    private static final EffectList HUD_LIST = new EffectList();
    private static final EffectList INVENTORY_LIST = new EffectList();
    private static final String TIP_MOTION = "effect_tip";
    private static final long TIP_GAP = 120_000_000L;
    private static boolean failed;
    private static Tip pending;
    private static String tipId;
    private static long tipStart;
    private static long tipLast;

    private EffectsOverlay() {
    }

    public static boolean hudActive() {
        UiPrefs prefs = UiPrefs.get();
        return !failed && prefs.surface(Surface.EFFECTS) && prefs.flag(HUD, true);
    }

    public static boolean inventoryActive() {
        UiPrefs prefs = UiPrefs.get();
        return !failed && prefs.surface(Surface.EFFECTS) && prefs.flag(INVENTORY, true);
    }

    public static void hudHidden() {
        HUD_LIST.clear();
    }

    public static void reset() {
        pending = null;
        HUD_LIST.clear();
        INVENTORY_LIST.clear();
    }

    public static boolean hud(Canvas target, Object owner, List<EffectInfo> effects, boolean demo, float tickrate) {
        try {
            drawHud(target, owner, effects, demo, tickrate);
            return true;
        } catch (RuntimeException exception) {
            fail(exception);
            return false;
        }
    }

    public static boolean inventory(Canvas target, Object owner, List<EffectInfo> effects, int left, int top,
            int available, int mouseX, int mouseY, float tickrate) {
        try {
            drawInventory(target, owner, effects, left, top, available, mouseX, mouseY, tickrate);
            return true;
        } catch (RuntimeException exception) {
            fail(exception);
            return false;
        }
    }

    private static void fail(RuntimeException exception) {
        if (!failed) {
            failed = true;
            ProteusClient.LOGGER.warn("Proteus: could not draw the effects, using the vanilla ones", exception);
        }
        reset();
    }

    private static final class Look {
        final Theme theme;
        final UiPrefs prefs;
        final boolean animated;
        final Moment in;
        final Moment out;
        final Moment move;
        final Moment warn;
        final boolean animateIn;
        final boolean animateOut;
        final boolean animateMove;
        final boolean throb;
        final float speed;

        Look() {
            prefs = UiPrefs.get();
            theme = Themes.current();
            MotionSpec motion = theme.motion();
            animated = motion.enabled() && prefs.animations();
            in = motion.get("effect_in");
            out = motion.get("effect_out");
            move = motion.get("effect_move");
            warn = motion.get("effect_warn");
            animateIn = animated && motion.has("effect_in");
            animateOut = animated && motion.has("effect_out");
            animateMove = animated && motion.has("effect_move");
            throb = animated && motion.has("effect_warn");
            speed = motion.speed();
        }
    }

    private static void drawHud(Canvas target, Object owner, List<EffectInfo> effects, boolean demo,
            float tickrate) {
        Look look = new Look();
        boolean timed = look.prefs.flag(HUD_TIME, false);
        List<EffectInfo> shown = new ArrayList<>();
        for (EffectInfo info : effects) {
            if (info.icon()) {
                shown.add(info);
            }
        }
        float dt = HUD_LIST.step();
        HUD_LIST.sync(owner, shown, dt, look.animateIn, look.in, look.animateOut, look.out, look.speed);
        int good = 0;
        int bad = 0;
        for (EffectList.Entry entry : HUD_LIST.entries()) {
            if (!entry.leaving) {
                int index = entry.info.beneficial() ? ++good : ++bad;
                entry.place(EffectLayout.HUD_STEP * index, look.animateMove, look.move, dt, look.speed);
            }
        }
        for (EffectList.Entry entry : HUD_LIST.entries()) {
            EffectInfo info = entry.info;
            int x = target.width() - Math.round(entry.pos);
            int y = EffectLayout.hudY(info.beneficial(), demo, timed);
            int size = EffectLayout.HUD_CELL;
            begin(target, entry, look, x, y, size, size);
            try {
                look.theme.effect(FADE, x, y, size, size, !info.beneficial(), info.ambient(), false, info.warn());
                FADE.effectIcon(info.id(), x + 3, y + 3, 18, info.blink());
                if (timed) {
                    String time = info.time(tickrate);
                    float scale = 0.75f;
                    int width = Math.round(FADE.textWidth(time) * scale);
                    FADE.push();
                    FADE.translate(x + (size - width) / 2f, y + size + 2);
                    FADE.scale(scale, scale);
                    FADE.text(time, 0, 0, timeColor(look.theme, info), look.theme.shadowText());
                    FADE.pop();
                }
            } finally {
                end();
            }
        }
    }

    private static void drawInventory(Canvas target, Object owner, List<EffectInfo> effects, int left, int top,
            int available, int mouseX, int mouseY, float tickrate) {
        Look look = new Look();
        boolean compact = look.prefs.flag(COMPACT, false);
        float dt = INVENTORY_LIST.step();
        List<EffectInfo> listed = new ArrayList<>();
        for (EffectInfo info : effects) {
            if (info.listed()) {
                listed.add(info);
            }
        }
        INVENTORY_LIST.sync(owner, listed, dt, look.animateIn, look.in, look.animateOut, look.out, look.speed);
        int count = 0;
        for (EffectList.Entry entry : INVENTORY_LIST.entries()) {
            if (!entry.leaving) {
                count++;
            }
        }
        int step = EffectLayout.inventoryStep(count);
        int index = 0;
        for (EffectList.Entry entry : INVENTORY_LIST.entries()) {
            if (!entry.leaving) {
                entry.place(index++ * step, look.animateMove, look.move, dt, look.speed);
            }
        }
        boolean wide = EffectLayout.inventoryWide(available, compact);
        EffectInfo hovered = null;
        int hoveredY = 0;
        int hoveredW = 0;
        for (EffectList.Entry entry : INVENTORY_LIST.entries()) {
            EffectInfo info = entry.info;
            String title = info.title();
            String time = info.time(tickrate);
            int w = EffectLayout.inventoryWidth(available, compact, target.textWidth(title), target.textWidth(time));
            int h = EffectLayout.PLATE;
            int y = top + Math.round(entry.pos);
            begin(target, entry, look, left, y, w, h);
            try {
                look.theme.effect(FADE, left, y, w, h, !info.beneficial(), info.ambient(), true, info.warn());
                FADE.effectIcon(info.id(), left + EffectLayout.PAD, y + EffectLayout.PAD, 18, info.blink());
                int room = w - EffectLayout.TEXT_X - EffectLayout.PAD;
                if (wide && room > 0) {
                    FADE.text(Draw.fit(FADE, title, room), left + EffectLayout.TEXT_X, y + EffectLayout.PAD,
                            nameColor(look.theme, info), look.theme.shadowText());
                    FADE.text(time, left + EffectLayout.TEXT_X, y + EffectLayout.PAD + 9,
                            timeColor(look.theme, info), look.theme.shadowText());
                }
            } finally {
                end();
            }
            boolean clipped = !wide || target.textWidth(title) > room(w);
            if (!entry.leaving && clipped && mouseX >= left && mouseX < left + w && mouseY >= y && mouseY < y + h) {
                hovered = info;
                hoveredY = y;
                hoveredW = w;
            }
        }
        if (hovered != null) {
            pending = new Tip(hovered, tickrate, left, hoveredY, hoveredW);
        }
    }

    public static void tip(Canvas target) {
        Tip tip = pending;
        pending = null;
        if (tip == null) {
            return;
        }
        try {
            drawTip(target, tip);
        } catch (RuntimeException exception) {
            fail(exception);
        }
    }

    private record Tip(EffectInfo info, float tickrate, int left, int y, int w) {
    }

    private static int room(int width) {
        return width - EffectLayout.TEXT_X - EffectLayout.PAD;
    }

    private static void drawTip(Canvas target, Tip tip) {
        Theme theme = Themes.current();
        EffectInfo info = tip.info();
        int pad = theme.sizes().tooltipPad();
        String title = info.title();
        String time = info.time(tip.tickrate());
        int w = Math.max(target.textWidth(title), target.textWidth(time)) + 2 * pad;
        int line = target.lineHeight() + 1;
        int h = 2 * line + 2 * pad - 1;
        int gap = 4;
        int x = tip.left() + tip.w() + gap;
        boolean flipped = x + w > target.width() - 2;
        if (flipped) {
            x = Math.max(2, tip.left() - w - gap);
        }
        int y = Math.max(2, Math.min(target.height() - h - 2, tip.y() + EffectLayout.PLATE / 2 - h / 2));
        long now = System.nanoTime();
        if (!info.id().equals(tipId) || now - tipLast > TIP_GAP) {
            tipStart = now;
        }
        tipId = info.id();
        tipLast = now;
        float alpha = 1f;
        int dx = 0;
        float scale = 1f;
        MotionSpec motion = theme.motion();
        if (motion.enabled() && UiPrefs.get().animations() && motion.has(TIP_MOTION)) {
            Moment moment = motion.get(TIP_MOTION);
            float raw = moment.raw((now - tipStart) / 1e9f, motion.speed());
            float eased = moment.value(raw);
            alpha = moment.effects().alpha(raw, eased);
            dx = Math.round(moment.effects().dx(raw, eased, w));
            if (flipped) {
                dx = -dx;
            }
            scale = moment.effects().scale(raw, eased);
        }
        FADE.bind(target, Motion.clamp(alpha, 0f, 1f));
        FADE.push();
        try {
            FADE.translate(dx, 0f);
            if (scale != 1f) {
                float cx = x + (flipped ? w : 0f);
                float cy = y + h / 2f;
                FADE.translate(cx, cy);
                FADE.scale(scale, scale);
                FADE.translate(-cx, -cy);
            }
            theme.tooltip(FADE, x, y, w, h);
            FADE.text(title, x + pad, y + pad, nameColor(theme, info), theme.shadowText());
            FADE.text(time, x + pad, y + pad + line, timeColor(theme, info), theme.shadowText());
        } finally {
            FADE.pop();
            FADE.unbind();
        }
    }

    private static int nameColor(Theme theme, EffectInfo info) {
        return info.beneficial() ? theme.textPrimary() : theme.effectBad();
    }

    private static int timeColor(Theme theme, EffectInfo info) {
        return Colors.mix(theme.textSecondary(), theme.effectBad(), info.warn());
    }

    private static void begin(Canvas target, EffectList.Entry entry, Look look, int x, int y, int w, int h) {
        float entered = look.animateIn ? look.in.value(entry.enter) : 1f;
        float leave = look.animateOut ? look.out.value(entry.exit) : 0f;
        float shown = entered * (1f - leave);
        float alpha = 1f;
        float dx = 0f;
        float dy = 0f;
        if (look.animateIn) {
            alpha = look.in.effects().alpha(entry.enter, entered);
            dx = look.in.effects().dx(entry.enter, shown, w + 4);
            dy = look.in.effects().dy(entry.enter, shown, h + 4);
        }
        if (look.animateOut) {
            alpha = Math.min(alpha, 1f - look.out.effects().fadeOut(entry.exit, leave));
            dx += look.out.effects().dx(1f - entry.exit, 1f - leave, w + 4);
            dy += look.out.effects().dy(1f - entry.exit, 1f - leave, h + 4);
        }
        FADE.bind(target, Motion.clamp(alpha, 0f, 1f));
        FADE.push();
        FADE.translate(Math.round(dx), Math.round(dy));
        float scale = 1f;
        float degrees = 0f;
        if (look.animateIn && look.in.effects().transforms()) {
            scale *= look.in.effects().scale(entry.enter, shown);
            degrees = look.in.effects().degrees(entry.enter, shown);
        }
        if (look.animateOut && entry.exit > 0f && look.out.effects().transforms()) {
            scale *= look.out.effects().scale(1f - entry.exit, 1f - leave);
        }
        if (look.throb && !entry.leaving) {
            float pulse = entry.info.warn();
            scale *= look.warn.effects().scale(1f - pulse, 1f - pulse);
        }
        if (scale != 1f || degrees != 0f) {
            float cx = x + w / 2f;
            float cy = y + h / 2f;
            FADE.translate(cx, cy);
            FADE.rotate(degrees);
            FADE.scale(scale, scale);
            FADE.translate(-cx, -cy);
        }
    }

    private static void end() {
        FADE.pop();
        FADE.unbind();
    }
}
