package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.UiHost;
import com.github.olympus.proteus.client.ui.UiShell;
import com.github.olympus.proteus.client.ui.VoiceIcons;
import net.minecraft.client.gui.screens.Screen;

abstract class SvcPanel extends UiShell implements SvcPage {
    static final int PAD = 10;
    static final int GAP = 4;
    static final int ROW = 20;

    private final Screen original;

    protected SvcPanel(UiHost host, Screen original) {
        super(host);
        this.original = original;
        VoiceIcons.register();
    }

    @Override
    public Screen original() {
        return original;
    }

    protected abstract String title();

    protected abstract void body(Theme theme, int x, int y, int w, int h);

    protected void footer(Theme theme, int x, int y, int w) {
        if (gui().button("svc-done", x, y, w, ROW, Svc.text("proteus.ui.done"), ButtonKind.PRIMARY, false, true,
                null)) {
            requestClose();
        }
    }

    protected int footerHeight() {
        return ROW;
    }

    protected int wide() {
        return 300;
    }

    protected void afterFrame() {
    }

    @Override
    protected int windowWidth() {
        return Math.max(260, Math.min(width() - 16, wide()));
    }

    @Override
    protected int windowHeight() {
        return Math.max(176, Math.min(height() - top() - 12, 300));
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
        gui().centered(gui().fit(title(), iw), px + pw / 2, y + 5, theme.titleColor());
        y += 26;

        int footerY = py + ph - PAD - footerHeight();
        int bodyHeight = footerY - GAP - y;

        float before = beginContent();
        body(theme, ix, y, iw, bodyHeight);
        endContent(before);

        footer(theme, ix, footerY, iw);
        afterFrame();
    }
}
