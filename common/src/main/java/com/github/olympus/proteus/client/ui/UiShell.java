package com.github.olympus.proteus.client.ui;

import com.github.olympus.proteus.client.ui.theme.Effects;
import com.github.olympus.proteus.client.ui.theme.Moment;
import com.github.olympus.proteus.client.ui.theme.MotionSpec;
import org.lwjgl.glfw.GLFW;

public abstract class UiShell implements UiApp {
    private final UiHost host;
    private final Gui gui;
    private final FadeCanvas fade = new FadeCanvas();
    private final UiPrefs prefs = UiPrefs.get();

    private int width;
    private int height;
    private float openRaw;
    private float open;
    private float contentFade;
    private boolean closing;
    private boolean closeReady;
    private boolean closed;
    private boolean animated;
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

    protected final int top() {
        return host.topInset();
    }

    protected int windowWidth() {
        return Math.max(260, Math.min(width - 16, 440));
    }

    protected int windowHeight() {
        return Math.max(176, Math.min(height - top() - 12, 330));
    }

    protected final int windowX(int windowWidth) {
        return (width - windowWidth) / 2;
    }

    protected final int windowY(int windowHeight) {
        return top() + Math.max(0, (height - top() - windowHeight) / 2);
    }

    protected final void restartContentFade() {
        contentFade = 0f;
        gui.blur();
    }

    protected final float beginContent() {
        float before = fade.alpha();
        if (!animated) {
            fade.push();
            return before;
        }
        Moment content = gui.motion().get("content");
        Effects effects = content.effects();
        float shown = content.value(contentFade);
        fade.push();
        fade.translate(Math.round(effects.dx(contentFade, shown, 0f)), Math.round(effects.dy(contentFade, shown, 0f)));
        if (effects.transforms()) {
            transform(effects.degrees(contentFade, shown), effects.scale(contentFade, shown));
        }
        fade.alpha(before * effects.alpha(contentFade, shown));
        return before;
    }

    private void transform(float degrees, float scale) {
        float cx = width / 2f;
        float cy = height / 2f;
        fade.translate(cx, cy);
        fade.rotate(degrees);
        fade.scale(scale, scale);
        fade.translate(-cx, -cy);
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
        if (!Themes.current().vanillaBackdrop()) {
            return false;
        }
        return prefs.blur() || !inWorld;
    }

    @Override
    public float visibility() {
        return open;
    }

    @Override
    public void skipOpening() {
        openRaw = 1f;
        open = 1f;
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

        Theme theme = Themes.current();
        MotionSpec motion = theme.motion();
        Moment window = motion.get("window");
        Moment content = motion.get("content");
        animated = motion.enabled() && prefs.animations();
        float openTarget = closing ? 0f : 1f;
        openRaw = animated ? window.advance(openRaw, openTarget, dt, motion.speed()) : openTarget;
        float eased = window.value(openRaw);
        open = Motion.clamp(eased, 0f, 1f);
        contentFade = animated ? content.advance(contentFade, 1f, dt, motion.speed()) : 1f;
        if (closing && openRaw <= 0.02f) {
            closeReady = true;
        }
        Effects effects = window.effects();
        int shift = animated ? Math.round(effects.dy(openRaw, eased, 0f)) : 0;
        boolean transformed = animated && effects.transforms();

        fade.bind(target, animated ? effects.alpha(openRaw, eased) : 1f);
        theme.backdrop(fade, width, height, host.inWorld());
        fade.shift(shift);
        if (transformed) {
            fade.push();
            transform(effects.degrees(openRaw, eased), effects.scale(openRaw, eased));
        }
        gui.begin(fade, theme, mouseX, mouseY - shift, dt, clock, !animated);
        gui.lock(closing);

        renderContent(theme);

        gui.end();
        if (transformed) {
            fade.pop();
        }
        fade.unshift();
        fade.unbind();
    }
}
