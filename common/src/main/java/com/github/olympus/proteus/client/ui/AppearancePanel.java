package com.github.olympus.proteus.client.ui;

import java.util.List;
import net.minecraft.network.chat.Component;

public final class AppearancePanel {
    private static final int ROW = 20;
    private static final int GAP = 6;

    private final Gui gui;
    private final FadeCanvas fade;
    private final UiPrefs prefs = UiPrefs.get();
    private boolean customDirty;

    public AppearancePanel(Gui gui, FadeCanvas fade) {
        this.gui = gui;
        this.fade = fade;
    }

    public boolean render(int ix, int cy, int iw, int ch, Runnable preview) {
        boolean changed = false;
        Theme theme = gui.theme();
        float scroll = gui.scrollBegin("appearance", ix, cy, iw, ch);
        int lw = iw - 8;
        int top = cy - Math.round(scroll);
        int y = top;

        y = heading(text("proteus.ui.style"), ix, y);
        theme.card(fade, ix, y, lw, 36);
        List<Theme> themes = Themes.all();
        String[] styles = new String[themes.size()];
        String[] styleTips = new String[themes.size()];
        int current = 0;
        for (int i = 0; i < themes.size(); i++) {
            styles[i] = themes.get(i).name();
            styleTips[i] = themes.get(i).description();
            if (themes.get(i) == theme) {
                current = i;
            }
        }
        int picked = gui.segmented("style", ix + 8, y + 8, lw - 16, ROW, styles, current, styleTips, null);
        if (picked >= 0) {
            prefs.setTheme(Themes.id(themes.get(picked)));
            changed = true;
        }
        y += 36 + GAP;

        if (theme.accentLocked()) {
            for (String line : Draw.wrap(fade, text("proteus.ui.classic_note"), lw - 8)) {
                gui.text(line, ix + 4, y, theme.textSecondary());
                y += 11;
            }
            y = toastSection(ix, y + GAP, lw, preview);
            gui.scrollEnd(y - top);
            return changed;
        }

        y = heading(text("proteus.ui.accent"), ix, y);
        boolean custom = UiPrefs.CUSTOM.equals(prefs.accentChoice());
        int cardH = custom ? 38 + 3 * 22 + 4 : 38;
        theme.card(fade, ix, y, lw, cardH);
        String[] choices = new String[UiPrefs.ACCENT_KEYS.length + 2];
        System.arraycopy(UiPrefs.ACCENT_KEYS, 0, choices, 0, UiPrefs.ACCENT_KEYS.length);
        choices[choices.length - 2] = UiPrefs.CUSTOM;
        choices[choices.length - 1] = UiPrefs.CHROMA;
        int count = choices.length;
        int size = 20;
        int gap = Math.min(14, (lw - 24 - count * size) / (count - 1));
        for (int i = 0; i < count; i++) {
            int sx = ix + 12 + i * (size + gap);
            String key = choices[i];
            boolean isCustom = UiPrefs.CUSTOM.equals(key);
            boolean isChroma = UiPrefs.CHROMA.equals(key);
            int color = isCustom ? prefs.customColor() : isChroma ? prefs.chromaColor() : UiPrefs.ACCENTS[i];
            if (gui.swatch("accent" + i, sx, y + 9, size, color, key.equals(prefs.accentChoice()), isCustom,
                    text("proteus.ui.accent." + key))) {
                prefs.setAccentChoice(key);
            }
        }
        if (custom) {
            float hue = prefs.hue();
            float saturation = prefs.saturation();
            float lightness = prefs.lightness();
            int sliderX = ix + 12;
            int sliderW = lw - 24;
            int sy = y + 44;
            float newHue = gui.slider("hue", sliderX, sy, sliderW, 8, hue,
                    t -> Colors.hsl((float) t, 0.3f + 0.7f * saturation, 0.3f + 0.4f * lightness));
            float newSaturation = gui.slider("saturation", sliderX, sy + 22, sliderW, 8, saturation,
                    t -> Colors.hsl(hue, 0.3f + 0.7f * (float) t, 0.3f + 0.4f * lightness));
            float newLightness = gui.slider("lightness", sliderX, sy + 44, sliderW, 8, lightness,
                    t -> Colors.hsl(hue, 0.3f + 0.7f * saturation, 0.3f + 0.4f * (float) t));
            if (newHue != hue || newSaturation != saturation || newLightness != lightness) {
                prefs.setCustom(newHue, newSaturation, newLightness);
                customDirty = true;
            }
        }
        if (customDirty && !gui.dragging()) {
            prefs.save();
            customDirty = false;
        }
        y += cardH + GAP;

        y = heading(text("proteus.ui.effects"), ix, y);
        theme.card(fade, ix, y, lw, 62);
        if (gui.switchRow("blur", ix + 10, y + 6, lw - 20, ROW, text("proteus.ui.blur"), prefs.blur(), true,
                text("proteus.ui.blur.tooltip"), text("proteus.state.on"), text("proteus.state.off"))) {
            prefs.setBlur(!prefs.blur());
        }
        if (gui.switchRow("animations", ix + 10, y + 6 + ROW + 6, lw - 20, ROW, text("proteus.ui.animations"),
                prefs.animations(), true, text("proteus.ui.animations.tooltip"), text("proteus.state.on"),
                text("proteus.state.off"))) {
            prefs.setAnimations(!prefs.animations());
        }
        y += 62 + GAP;
        y = toastSection(ix, y, lw, preview);
        gui.scrollEnd(y - top);
        return changed;
    }

    private int toastSection(int ix, int y, int lw, Runnable preview) {
        Theme theme = gui.theme();
        y = heading(text("proteus.ui.toast.heading"), ix, y);
        theme.card(fade, ix, y, lw, 58);
        int boxX = ix + 12;
        int boxY = y + 9;
        theme.inset(fade, boxX, boxY, 72, 40);
        String[] corners = {"top_left", "top_right", "bottom_left", "bottom_right"};
        for (int i = 0; i < corners.length; i++) {
            int padX = boxX + 4 + (i % 2) * (72 - 8 - 24);
            int padY = boxY + 4 + (i / 2) * (40 - 8 - 10);
            if (gui.pad("corner" + i, padX, padY, 24, 10, prefs.toastCorner() == i,
                    text("proteus.ui.toast." + corners[i]))) {
                prefs.setToastCorner(i);
                preview.run();
            }
        }
        int sideX = boxX + 72 + 14;
        int sideW = lw - 12 - (sideX - ix);
        int seconds = prefs.toastSeconds();
        int step = gui.stepper("toast-seconds", sideX, y + 9, sideW, 18, text("proteus.time.seconds", seconds),
                text("proteus.ui.toast.duration"), seconds > 2, seconds < 15);
        if (step != 0) {
            prefs.setToastSeconds(seconds + step);
        }
        if (gui.button("toast-preview", sideX, y + 31, sideW, 18, text("proteus.ui.toast.preview"),
                text("proteus.ui.toast.preview.tooltip"))) {
            preview.run();
        }
        return y + 58 + GAP;
    }

    private static String text(String key, Object... arguments) {
        return Component.translatable(key, arguments).getString();
    }

    private int heading(String label, int x, int y) {
        gui.text(label, x + 2, y + 1, gui.theme().textSecondary());
        return y + 13;
    }
}
