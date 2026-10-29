package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ui.theme.Moment;
import com.github.olympus.proteus.client.ui.theme.MotionSpec;
import com.github.olympus.proteus.client.ui.theme.Sizes;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import net.minecraft.network.chat.Component;

public final class ToastOverlay {
    private static final FadeCanvas FADE = new FadeCanvas();

    private record Pending(String title, String body, String icon) {
    }

    private static final int MAX_QUEUED = 8;
    private static final Deque<Pending> QUEUE = new ArrayDeque<>();

    private static String title = "";
    private static String body = "";
    private static String icon = "";
    private static boolean active;
    private static double age;
    private static float enter;
    private static float exit;
    private static float width;
    private static float height;
    private static long last;

    private ToastOverlay() {
    }

    public static synchronized void show(String newTitle, String newBody, String newIcon) {
        title = newTitle;
        body = newBody;
        icon = newIcon;
        if (!active) {
            width = 0f;
            height = 0f;
            age = 0;
            enter = 0f;
            last = System.nanoTime();
        } else {
            age = Math.min(age, Themes.current().motion().get("toast_in").duration());
        }
        exit = 0f;
        active = true;
    }

    public static synchronized void queue(String newTitle, String newBody, String newIcon) {
        if (!active) {
            show(newTitle, newBody, newIcon);
            return;
        }
        if (title.equals(newTitle) && body.equals(newBody)) {
            return;
        }
        for (Pending pending : QUEUE) {
            if (pending.title().equals(newTitle) && pending.body().equals(newBody)) {
                return;
            }
        }
        if (QUEUE.size() < MAX_QUEUED) {
            QUEUE.add(new Pending(newTitle, newBody, newIcon));
        }
    }

    public static void preview() {
        show(Component.translatable("proteus.toast.preview").getString(),
                Component.translatable("proteus.ui.toast.sample").getString(), "music_disc_cat");
    }

    public static synchronized boolean active() {
        return active;
    }

    public static synchronized void dismiss() {
        active = false;
        QUEUE.clear();
    }

    public static synchronized void render(Canvas target) {
        if (!active) {
            return;
        }
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - last) / 1_000_000_000f);
        last = now;
        age += dt;

        UiPrefs prefs = UiPrefs.get();
        Theme theme = Themes.current();
        Sizes sizes = theme.sizes();
        MotionSpec motion = theme.motion();
        Moment in = motion.get("toast_in");
        Moment out = motion.get("toast_out");
        Moment resize = motion.get("toast_resize");
        boolean animated = motion.enabled() && prefs.animations();
        float hold = prefs.toastSeconds();
        if (animated) {
            enter = in.advance(enter, 1f, dt, motion.speed());
            if (age >= hold) {
                exit = out.advance(exit, 1f, dt, motion.speed());
            }
        } else {
            enter = 1f;
            exit = age >= hold ? 1f : 0f;
        }
        if (exit >= 1f) {
            active = false;
            Pending next = QUEUE.poll();
            if (next != null) {
                show(next.title(), next.body(), next.icon());
            }
            return;
        }

        int margin = sizes.toastMargin();
        int pad = sizes.toastPad();
        int iconSize = sizes.toastIcon();
        int minWidth = sizes.toastMinWidth();
        int maxLines = sizes.toastMaxLines();
        boolean hasIcon = !icon.isEmpty() && target.hasItem(icon);
        int textX = pad + (hasIcon ? iconSize + pad : 0);
        int maxWidth = Math.max(minWidth, Math.min(sizes.toastMaxWidth(), target.width() - 2 * margin));
        int textMax = maxWidth - textX - pad;
        List<String> lines = Draw.wrap(target, body, textMax);
        if (lines.size() > maxLines) {
            lines = lines.subList(0, maxLines);
            lines.set(maxLines - 1, Draw.fit(target, lines.get(maxLines - 1) + "...", textMax));
        }
        int textWidth = target.textWidth(title);
        for (String line : lines) {
            textWidth = Math.max(textWidth, target.textWidth(line));
        }
        int lineHeight = target.lineHeight() + 1;
        float wantedWidth = Math.max(minWidth, Math.min(maxWidth, textX + textWidth + pad));
        float wantedHeight = pad + lineHeight + 2 + lines.size() * lineHeight + pad - 1;
        if (hasIcon) {
            wantedHeight = Math.max(iconSize + 2 * pad, wantedHeight);
        }
        if (width == 0f || !animated || !resize.spring()) {
            width = wantedWidth;
            height = wantedHeight;
        } else {
            width = resize.advance(width, wantedWidth, dt, motion.speed());
            height = resize.advance(height, wantedHeight, dt, motion.speed());
        }
        int w = Math.round(width);
        int h = Math.round(height);

        int corner = prefs.toastCorner();
        boolean right = corner == 1 || corner == 3;
        boolean bottom = corner == 2 || corner == 3;
        int x = right ? target.width() - margin - w : margin;
        int y = bottom ? target.height() - margin - h : margin;

        float entered = animated ? in.value(enter) : 1f;
        float leave = animated ? out.value(exit) : 0f;
        float shown = entered * (1f - leave);
        float slideX = animated ? in.effects().dx(enter, shown, w + margin) : 0f;
        float slideY = animated ? in.effects().dy(enter, shown, h + margin) : 0f;
        int dx = Math.round(right ? slideX : -slideX);
        int dy = Math.round(bottom ? slideY : -slideY);
        float alpha = animated
                ? Motion.clamp(Math.min(in.effects().alpha(enter, entered), 1f - out.effects().alpha(exit, leave)), 0f, 1f)
                : 1f;

        FADE.bind(target, alpha);
        FADE.push();
        FADE.translate(dx, dy);
        if (animated && in.effects().transforms()) {
            float cx = x + w / 2f;
            float cy = y + h / 2f;
            FADE.translate(cx, cy);
            FADE.rotate(in.effects().degrees(enter, shown));
            float scale = in.effects().scale(enter, shown);
            FADE.scale(scale, scale);
            FADE.translate(-cx, -cy);
        }
        theme.toast(FADE, x, y, w, h);
        if (hasIcon) {
            int boxY = y + (h - iconSize) / 2;
            theme.inset(FADE, x + pad, boxY, iconSize, iconSize);
            FADE.push();
            FADE.translate(x + pad + 2, boxY + 2);
            FADE.scale((iconSize - 4) / 16f, (iconSize - 4) / 16f);
            FADE.item(icon, 0, 0);
            FADE.pop();
        }
        boolean shadow = theme.shadowText();
        int top = y + (h - (lineHeight + 2 + lines.size() * lineHeight - 1)) / 2;
        FADE.text(Draw.fit(FADE, title, w - textX - pad), x + textX, top, theme.toastTitle(), shadow);
        for (int i = 0; i < lines.size(); i++) {
            FADE.text(lines.get(i), x + textX, top + lineHeight + 2 + i * lineHeight, theme.titleColor(), shadow);
        }
        FADE.pop();
        FADE.unbind();
    }
}
