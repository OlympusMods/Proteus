package com.github.olympus.proteus.client.ui;

import java.util.List;
import net.minecraft.network.chat.Component;

public final class ProteusUi extends UiShell {
    private static final int PAD = 10;
    private static final int GAP = 6;
    private static final int ROW = 20;

    private enum Tab {
        APPEARANCE,
        VANILLA,
        MODS
    }

    private final AppearancePanel appearance;
    private Tab tab = Tab.APPEARANCE;

    public ProteusUi(UiHost host) {
        super(host);
        this.appearance = new AppearancePanel(gui(), fade());
    }

    @Override
    protected void renderContent(Theme theme) {
        int pw = windowWidth();
        int ph = windowHeight();
        int px = windowX(pw);
        int py = windowY(ph);
        theme.window(fade(), px, py, pw, ph);

        int ix = px + PAD;
        int iw = pw - 2 * PAD;
        int y = py + PAD;

        String title = text("proteus.hub.title");
        if (theme.centeredTitle() && pw >= 340) {
            gui().centered(title, px + pw / 2, y + 5, theme.titleColor());
        } else {
            fade().push();
            fade().translate(ix, y + 2);
            fade().scale(1.25f, 1.25f);
            gui().text(title, 0, 0, theme.titleColor());
            fade().pop();
        }
        y += 26;

        String[] tabs = {text("proteus.tab.appearance"), text("proteus.tab.vanilla"), text("proteus.tab.mods")};
        String[] tips = {text("proteus.tab.appearance.tooltip"), text("proteus.tab.vanilla.tooltip"),
            text("proteus.tab.mods.tooltip")};
        int chosen = gui().segmented("tabs", ix, y, iw, 20, tabs, tab.ordinal(), tips, null);
        if (chosen >= 0) {
            tab = Tab.values()[chosen];
            restartContentFade();
        }
        y += 20 + GAP;

        int footerY = py + ph - PAD - ROW;
        int contentHeight = footerY - GAP - y;

        float before = beginContent();
        switch (tab) {
            case APPEARANCE -> {
                if (appearance.render(ix, y, iw, contentHeight, ToastOverlay::preview)) {
                    restartContentFade();
                }
            }
            case VANILLA -> renderVanilla(theme, ix, y, iw, contentHeight);
            case MODS -> renderMods(theme, ix, y, iw, contentHeight);
        }
        endContent(before);

        if (gui().button("done", ix, footerY, iw, ROW, text("proteus.ui.done"), ButtonKind.PRIMARY, false, true,
                null)) {
            requestClose();
        }
    }

    private void renderVanilla(Theme theme, int ix, int cy, int iw, int ch) {
        float scroll = gui().scrollBegin("vanilla", ix, cy, iw, ch);
        int lw = iw - 8;
        int top = cy - Math.round(scroll);
        int y = top;

        y = heading(text("proteus.vanilla.heading"), ix, y);
        for (String line : Draw.wrap(fade(), text("proteus.vanilla.note"), lw - 8)) {
            gui().text(line, ix + 4, y, theme.textSecondary());
            y += 11;
        }
        y += 4;

        Surface[] surfaces = Surface.values();
        int cardH = 8 + surfaces.length * (ROW + 6) - 6 + 8;
        theme.card(fade(), ix, y, lw, cardH);
        int rowY = y + 8;
        for (Surface surface : surfaces) {
            boolean enabled = prefs().surface(surface);
            if (gui().switchRow("surface-" + surface.key(), ix + 12, rowY, lw - 24, ROW, text(surface.nameKey()),
                    enabled, true, text(surface.tooltipKey()), text("proteus.state.on"),
                    text("proteus.state.off"))) {
                prefs().setSurface(surface, !enabled);
            }
            rowY += ROW + 6;
        }
        y += cardH + GAP;

        SurfacePanel panel = new SurfacePanel(gui(), fade(), theme, prefs(), ix, lw);
        if (prefs().surface(Surface.TOASTS)) {
            y = renderToasts(theme, ix, y, lw);
        }
        if (prefs().surface(Surface.CHAT)) {
            y = ChatOptions.render(panel, y);
        }
        if (prefs().surface(Surface.MENUS)) {
            y = MenuOptions.render(panel, y);
        }
        if (prefs().surface(Surface.EFFECTS)) {
            y = EffectsOptions.render(panel, y);
        }
        if (prefs().surface(Surface.TOOLTIPS)) {
            y = TooltipOptions.render(panel, y);
        }

        gui().scrollEnd(y - top);
    }

    private int renderToasts(Theme theme, int ix, int y, int lw) {
        y = heading(text("proteus.toasts.heading"), ix, y);
        for (String line : Draw.wrap(fade(), text("proteus.toasts.note"), lw - 8)) {
            gui().text(line, ix + 4, y, theme.textSecondary());
            y += 11;
        }
        y += 4;

        ToastKind[] kinds = ToastKind.values();
        int cardH = 8 + kinds.length * (ROW + 6) - 6 + 8;
        theme.card(fade(), ix, y, lw, cardH);
        String[] choices = {text("proteus.toasts.proteus"), text("proteus.toasts.vanilla")};
        int choiceW = Math.min(150, lw / 2);
        int rowY = y + 8;
        for (ToastKind kind : kinds) {
            String label = text("proteus.toasts." + kind.key());
            String tip = text("proteus.toasts." + kind.key() + ".tooltip");
            gui().text(gui().fit(label, lw - choiceW - 28), ix + 12, gui().textY(rowY, ROW), theme.textPrimary());
            if (gui().over(ix, rowY, lw - choiceW - 16, ROW)) {
                gui().tooltip(tip);
            }
            boolean replaced = prefs().kindReplaced(kind);
            int picked = gui().segmented("toast-" + kind.key(), ix + lw - 10 - choiceW, rowY, choiceW, ROW, choices,
                    replaced ? 0 : 1, new String[] {tip, tip}, null);
            if (picked >= 0) {
                prefs().setReplaces(kind, picked == 0);
            }
            rowY += ROW + 6;
        }
        return y + cardH + GAP;
    }

    private void renderMods(Theme theme, int ix, int cy, int iw, int ch) {
        float scroll = gui().scrollBegin("mods", ix, cy, iw, ch);
        int lw = iw - 8;
        int top = cy - Math.round(scroll);
        int y = top;

        y = heading(text("proteus.mods.heading"), ix, y);
        for (String line : Draw.wrap(fade(), text("proteus.mods.note"), lw - 8)) {
            gui().text(line, ix + 4, y, theme.textSecondary());
            y += 11;
        }
        y += 4;

        List<Mods.Entry> entries = Mods.installed();
        if (entries.isEmpty()) {
            gui().text(text("proteus.mods.none"), ix + 4, y, theme.textDisabled());
            y += 11 + GAP;
        } else {
            int cardH = 8 + entries.size() * (ROW + 6) - 6 + 8;
            theme.card(fade(), ix, y, lw, cardH);
            int rowY = y + 8;
            for (Mods.Entry entry : entries) {
                boolean enabled = prefs().modEnabled(entry.id());
                if (gui().switchRow("mod-" + entry.id(), ix + 12, rowY, lw - 24, ROW, text(entry.nameKey()), enabled,
                        true, text(entry.tooltipKey()), text("proteus.state.on"), text("proteus.state.off"))) {
                    prefs().setModEnabled(entry.id(), !enabled);
                }
                rowY += ROW + 6;
            }
            y += cardH + GAP;
        }

        gui().scrollEnd(y - top);
    }

    private static String text(String key, Object... arguments) {
        return Component.translatable(key, arguments).getString();
    }
}
