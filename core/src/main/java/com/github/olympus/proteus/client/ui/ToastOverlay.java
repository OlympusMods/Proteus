package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ui.theme.Moment;
import com.github.olympus.proteus.client.ui.theme.MotionSpec;
import com.github.olympus.proteus.client.ui.theme.Sizes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import net.minecraft.network.chat.Component;

public final class ToastOverlay {
    private static final FadeCanvas FADE = new FadeCanvas();
    private static final int MAX_VISIBLE = 5;
    private static final int MAX_QUEUED = 8;

    private record Pending(String title, String body, String icon) {
    }

    private static final class Toast {
        final String title;
        final String body;
        final String icon;
        double age;
        float enter;
        float exit;
        float width;
        float height;
        float offset;
        float from;
        float slot = -1f;
        float travel = 1f;
        boolean hasIcon;
        int textX;
        List<String> lines = List.of();

        Toast(String title, String body, String icon) {
            this.title = title;
            this.body = body;
            this.icon = icon;
        }

        boolean same(String otherTitle, String otherBody) {
            return title.equals(otherTitle) && body.equals(otherBody);
        }
    }

    private static final List<Toast> TOASTS = new ArrayList<>();
    private static final Deque<Pending> QUEUE = new ArrayDeque<>();
    private static long last;

    private ToastOverlay() {
    }

    public static synchronized void show(String title, String body, String icon) {
        if (TOASTS.isEmpty()) {
            last = System.nanoTime();
        }
        TOASTS.add(0, new Toast(title, body, icon));
        int seen = 0;
        for (Toast toast : TOASTS) {
            if (leaving(toast)) {
                continue;
            }
            if (++seen > MAX_VISIBLE) {
                toast.age = Double.MAX_VALUE;
            }
        }
        while (TOASTS.size() > MAX_VISIBLE * 2) {
            TOASTS.remove(TOASTS.size() - 1);
        }
    }

    public static synchronized void queue(String title, String body, String icon) {
        for (Toast toast : TOASTS) {
            if (toast.same(title, body) && !leaving(toast)) {
                return;
            }
        }
        for (Pending pending : QUEUE) {
            if (pending.title().equals(title) && pending.body().equals(body)) {
                return;
            }
        }
        if (live() < MAX_VISIBLE) {
            show(title, body, icon);
        } else if (QUEUE.size() < MAX_QUEUED) {
            QUEUE.add(new Pending(title, body, icon));
        }
    }

    public static void preview() {
        show(Component.translatable("proteus.toast.preview").getString(),
                Component.translatable("proteus.ui.toast.sample").getString(), "music_disc_cat");
    }

    public static synchronized boolean active() {
        return !TOASTS.isEmpty();
    }

    public static synchronized void dismiss() {
        TOASTS.clear();
        QUEUE.clear();
    }

    private static boolean leaving(Toast toast) {
        return toast.exit > 0f || toast.age == Double.MAX_VALUE;
    }

    private static int live() {
        int count = 0;
        for (Toast toast : TOASTS) {
            if (!leaving(toast)) {
                count++;
            }
        }
        return count;
    }

    public static synchronized void render(Canvas target) {
        if (TOASTS.isEmpty()) {
            return;
        }
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - last) / 1_000_000_000f);
        last = now;

        UiPrefs prefs = UiPrefs.get();
        Theme theme = Themes.current();
        Sizes sizes = theme.sizes();
        MotionSpec motion = theme.motion();
        Moment in = motion.get("toast_in");
        Moment out = motion.get("toast_out");
        Moment resize = motion.get("toast_resize");
        Moment move = motion.get("toast_move");
        boolean animated = motion.enabled() && prefs.animations();
        float hold = prefs.toastSeconds();

        Iterator<Toast> iterator = TOASTS.iterator();
        while (iterator.hasNext()) {
            Toast toast = iterator.next();
            if (toast.age != Double.MAX_VALUE) {
                toast.age += dt;
            }
            if (animated) {
                toast.enter = in.advance(toast.enter, 1f, dt, motion.speed());
                if (toast.age >= hold) {
                    toast.exit = out.advance(toast.exit, 1f, dt, motion.speed());
                }
            } else {
                toast.enter = 1f;
                toast.exit = toast.age >= hold ? 1f : 0f;
            }
            if (toast.exit >= 1f) {
                iterator.remove();
            }
        }
        while (live() < MAX_VISIBLE && !QUEUE.isEmpty()) {
            Pending next = QUEUE.poll();
            show(next.title(), next.body(), next.icon());
        }
        if (TOASTS.isEmpty()) {
            return;
        }

        int margin = sizes.toastMargin();
        int gap = Math.max(3, margin / 2);
        int corner = prefs.toastCorner();
        boolean right = corner == 1 || corner == 3;
        boolean bottom = corner == 2 || corner == 3;
        float cursor = 0f;
        for (Toast toast : TOASTS) {
            measure(toast, target, sizes, animated, resize, dt, motion.speed());
            place(toast, cursor, animated, move, dt, motion.speed());
            cursor += Math.round(toast.height) + gap;
        }
        for (int i = TOASTS.size() - 1; i >= 0; i--) {
            draw(TOASTS.get(i), target, theme, sizes, animated, in, out, margin, right, bottom);
        }
    }

    private static void measure(Toast toast, Canvas target, Sizes sizes, boolean animated, Moment resize, float dt,
            float speed) {
        int margin = sizes.toastMargin();
        int pad = sizes.toastPad();
        int iconSize = sizes.toastIcon();
        int minWidth = sizes.toastMinWidth();
        int maxLines = sizes.toastMaxLines();
        toast.hasIcon = !toast.icon.isEmpty() && target.hasItem(toast.icon);
        toast.textX = pad + (toast.hasIcon ? iconSize + pad : 0);
        int maxWidth = Math.max(minWidth, Math.min(sizes.toastMaxWidth(), target.width() - 2 * margin));
        int textMax = maxWidth - toast.textX - pad;
        List<String> lines = Draw.wrap(target, toast.body, textMax);
        if (lines.size() > maxLines) {
            lines = new ArrayList<>(lines.subList(0, maxLines));
            lines.set(maxLines - 1, Draw.fit(target, lines.get(maxLines - 1) + "...", textMax));
        }
        toast.lines = lines;
        int textWidth = target.textWidth(toast.title);
        for (String line : lines) {
            textWidth = Math.max(textWidth, target.textWidth(line));
        }
        int lineHeight = target.lineHeight() + 1;
        float wantedWidth = Math.max(minWidth, Math.min(maxWidth, toast.textX + textWidth + pad));
        float wantedHeight = pad + lineHeight + 2 + lines.size() * lineHeight + pad - 1;
        if (toast.hasIcon) {
            wantedHeight = Math.max(iconSize + 2 * pad, wantedHeight);
        }
        if (toast.width == 0f || !animated || !resize.spring()) {
            toast.width = wantedWidth;
            toast.height = wantedHeight;
        } else {
            toast.width = resize.advance(toast.width, wantedWidth, dt, speed);
            toast.height = resize.advance(toast.height, wantedHeight, dt, speed);
        }
    }

    private static void place(Toast toast, float slot, boolean animated, Moment move, float dt, float speed) {
        if (toast.slot < 0f || !animated) {
            toast.slot = slot;
            toast.offset = slot;
            toast.from = slot;
            toast.travel = 1f;
            return;
        }
        if (move.spring()) {
            toast.slot = slot;
            toast.offset = move.advance(toast.offset, slot, dt, speed);
            return;
        }
        if (slot != toast.slot) {
            toast.from = toast.offset;
            toast.slot = slot;
            toast.travel = 0f;
        }
        toast.travel = move.advance(toast.travel, 1f, dt, speed);
        toast.offset = toast.from + (slot - toast.from) * move.value(toast.travel);
    }

    private static void draw(Toast toast, Canvas target, Theme theme, Sizes sizes, boolean animated, Moment in,
            Moment out, int margin, boolean right, boolean bottom) {
        int pad = sizes.toastPad();
        int iconSize = sizes.toastIcon();
        int w = Math.round(toast.width);
        int h = Math.round(toast.height);
        int shift = Math.round(toast.offset);
        int x = right ? target.width() - margin - w : margin;
        int y = bottom ? target.height() - margin - h - shift : margin + shift;

        float entered = animated ? in.value(toast.enter) : 1f;
        float leave = animated ? out.value(toast.exit) : 0f;
        float shown = entered * (1f - leave);
        float slideX = animated ? in.effects().dx(toast.enter, shown, w + margin) : 0f;
        float slideY = animated ? in.effects().dy(toast.enter, shown, h + margin) : 0f;
        int dx = Math.round(right ? slideX : -slideX);
        int dy = Math.round(bottom ? slideY : -slideY);
        float alpha = animated
                ? Motion.clamp(Math.min(in.effects().alpha(toast.enter, entered),
                        1f - out.effects().fadeOut(toast.exit, leave)), 0f, 1f)
                : 1f;

        FADE.bind(target, alpha);
        FADE.push();
        FADE.translate(dx, dy);
        if (animated && (in.effects().transforms() || out.effects().transforms())) {
            float cx = x + w / 2f;
            float cy = y + h / 2f;
            float degrees = in.effects().degrees(toast.enter, shown);
            float scale = in.effects().scale(toast.enter, shown);
            if (toast.exit > 0f) {
                scale *= out.effects().scale(1f - toast.exit, 1f - leave);
            }
            FADE.translate(cx, cy);
            FADE.rotate(degrees);
            FADE.scale(scale, scale);
            FADE.translate(-cx, -cy);
        }
        theme.toast(FADE, x, y, w, h);
        if (toast.hasIcon) {
            int boxY = y + (h - iconSize) / 2;
            theme.inset(FADE, x + pad, boxY, iconSize, iconSize);
            FADE.push();
            FADE.translate(x + pad + 2, boxY + 2);
            FADE.scale((iconSize - 4) / 16f, (iconSize - 4) / 16f);
            FADE.item(toast.icon, 0, 0);
            FADE.pop();
        }
        boolean shadow = theme.shadowText();
        int lineHeight = target.lineHeight() + 1;
        List<String> lines = toast.lines;
        int top = y + (h - (lineHeight + 2 + lines.size() * lineHeight - 1)) / 2;
        FADE.text(Draw.fit(FADE, toast.title, w - toast.textX - pad), x + toast.textX, top, theme.toastTitle(), shadow);
        for (int i = 0; i < lines.size(); i++) {
            FADE.text(lines.get(i), x + toast.textX, top + lineHeight + 2 + i * lineHeight, theme.titleColor(),
                    shadow);
        }
        FADE.pop();
        FADE.unbind();
    }
}
