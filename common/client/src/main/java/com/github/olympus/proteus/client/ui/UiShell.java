package com.github.olympus.proteus.client.ui;

import org.lwjgl.glfw.GLFW;

public abstract class UiShell implements UiApp {
    private final UiHost host;
    private final Gui gui;
    private final FadeCanvas fade = new FadeCanvas();
    private final UiPrefs prefs = UiPrefs.get();
    private final Theme modern = new ModernTheme();
    private final Theme classic = new ClassicTheme();

    private int width;
    private int height;
    private float open;
    private float contentFade;
    private boolean closing;
    private boolean closeReady;
    private boolean closed;
    private long lastNanos = System.nanoTime();
    private double clock;

    protected UiShell(UiHost host) {
        this.host = host;
        this.gui = new Gui(host);
    }

    protected abstract void renderContent(Theme theme);

    protected void onTick() {
    }

    protected boolean onEscape() {
        return false;
    }

    protected final Gui gui() {
        return gui;
    }

    protected final FadeCanvas fade() {
        return fade;
    }

    protected final UiPrefs prefs() {
        return prefs;
    }

    protected final UiHost host() {
        return host;
    }

    protected final int width() {
        return width;
    }

    protected final int height() {
        return height;
    }

    protected final double clock() {
        return clock;
    }

    protected int windowWidth() {
        return Math.max(260, Math.min(width - 16, 440));
    }

    protected int windowHeight() {
        return Math.max(176, Math.min(height - 12, 330));
    }

    protected final void restartContentFade() {
        contentFade = 0f;
        gui.blur();
    }

    protected final float beginContent() {
        float before = fade.alpha();
        fade.push();
        fade.translate(0f, Math.round((1f - Motion.easeOutCubic(contentFade)) * 8f));
        fade.alpha(before * Motion.easeOutCubic(contentFade));
        return before;
    }

    protected final void endContent(float before) {
        fade.alpha(before);
        fade.pop();
    }

    protected final int heading(String label, int x, int y) {
        gui.text(label, x + 2, y + 1, gui.theme().textSecondary());
        return y + 13;
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean vanillaBackdrop(boolean inWorld) {
        if (prefs.classic()) {
            return false;
        }
        return prefs.blur() || !inWorld;
    }

    @Override
    public void tick() {
        if (closeReady && !closed) {
            closed = true;
            host.close();
            return;
        }
        onTick();
    }

    @Override
    public void requestClose() {
        closing = true;
    }

    @Override
    public void mousePressed(int button) {
        gui.press(button);
    }

    @Override
    public void mouseReleased(int button) {
        gui.release(button);
    }

    @Override
    public void mouseScrolled(double amount) {
        gui.scrolled(amount);
    }

    @Override
    public boolean keyPressed(int key, int mods) {
        if (gui.keyPressed(key, mods)) {
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (!onEscape()) {
                requestClose();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(int codepoint) {
        return gui.charTyped(codepoint);
    }

    @Override
    public void render(Canvas target, int mouseX, int mouseY) {
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastNanos) / 1_000_000_000f);
        lastNanos = now;
        clock += dt;

        Theme theme = prefs.classic() ? classic : modern;
        theme.accent(prefs.accentColor());
        boolean animated = theme.animated() && prefs.animations();
        float openTarget = closing ? 0f : 1f;
        open = animated ? Motion.approach(open, openTarget, dt, 14f) : openTarget;
        contentFade = animated ? Motion.approach(contentFade, 1f, dt, 12f) : 1f;
        if (closing && open <= 0.02f) {
            closeReady = true;
        }
        int shift = animated ? Math.round((1f - open) * 10f) : 0;

        fade.bind(target, open);
        theme.backdrop(fade, width, height, host.inWorld());
        fade.shift(shift);
        gui.begin(fade, theme, mouseX, mouseY - shift, dt, clock, !animated);
        gui.lock(closing);

        renderContent(theme);

        gui.end();
        fade.unshift();
        fade.unbind();
    }
}
