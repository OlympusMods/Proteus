package com.github.olympus.proteus.client.ui;

import java.util.List;
import net.minecraft.network.chat.Component;

final class HubApp implements UiApp {
    private static final int BAR_Y = 6;
    private static final int BAR_H = 22;
    private static final int INSET = BAR_Y + BAR_H + 2;
    private static final int CARD_MIN = 92;
    private static final int CARD_MAX = 124;
    private static final int GAP = 4;
    private static final int ARROW = 20;

    private final UiHost host;
    private final UiHost inner;
    private final Gui gui;
    private final FadeCanvas fade = new FadeCanvas();
    private final UiPrefs prefs = UiPrefs.get();
    private final Theme modern = new ModernTheme();
    private final Theme classic = new ClassicTheme();
    private final List<Hub.Tab> tabs;

    private int current;
    private UiApp app;
    private int page = -1;
    private int width;
    private int height;
    private int mouseX;
    private int mouseY;
    private long lastNanos = System.nanoTime();
    private double clock;
    private boolean closing;

    HubApp(UiHost host, List<Hub.Tab> tabs, String id) {
        this.host = host;
        this.tabs = tabs;
        this.gui = new Gui(host);
        this.inner = new UiHost() {
            @Override
            public void close() {
                host.close();
            }

            @Override
            public void playClick() {
                host.playClick();
            }

            @Override
            public boolean inWorld() {
                return host.inWorld();
            }

            @Override
            public int topInset() {
                return INSET;
            }
        };
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).id().equals(id)) {
                current = i;
            }
        }
        this.app = tabs.get(current).app().apply(inner);
    }

    private void select(int index) {
        if (index == current) {
            return;
        }
        app.removed();
        current = index;
        app = tabs.get(index).app().apply(inner);
        app.resize(width, height);
        app.skipOpening();
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        app.resize(width, height);
    }

    @Override
    public void tick() {
        app.tick();
    }

    @Override
    public void requestClose() {
        closing = true;
        app.requestClose();
    }

    @Override
    public void render(Canvas target, int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastNanos) / 1_000_000_000f);
        lastNanos = now;
        clock += dt;

        boolean onBar = overBar(mouseX, mouseY);
        app.render(target, onBar ? -1 : mouseX, onBar ? -1 : mouseY);

        Theme theme = prefs.classic() ? classic : modern;
        theme.accent(prefs.accentColor());
        boolean animated = theme.animated() && prefs.animations();
        float shown = app.visibility();
        int shift = animated ? Math.round((1f - shown) * -6f) : 0;
        fade.bind(target, shown);
        fade.shift(shift);
        gui.begin(fade, theme, mouseX, mouseY - shift, dt, clock, !animated);
        gui.lock(closing || shown < 0.5f);
        renderBar();
        gui.end();
        fade.unshift();
        fade.unbind();
    }

    private int barWidth() {
        return Math.max(260, Math.min(width - 16, 440));
    }

    private boolean overBar(int x, int y) {
        int barW = barWidth();
        int barX = (width - barW) / 2;
        return x >= barX && x < barX + barW && y >= BAR_Y && y < BAR_Y + BAR_H;
    }

    private void renderBar() {
        int count = tabs.size();
        int barW = barWidth();
        int barX = (width - barW) / 2;
        boolean fits = count * CARD_MIN + (count - 1) * GAP <= barW;
        int perPage;
        int cardW;
        int start;
        if (fits) {
            perPage = count;
            cardW = Math.min(CARD_MAX, (barW - (count - 1) * GAP) / count);
            start = barX + (barW - (count * cardW + (count - 1) * GAP)) / 2;
            page = 0;
        } else {
            int avail = barW - 2 * (ARROW + GAP);
            perPage = Math.max(1, (avail + GAP) / (CARD_MIN + GAP));
            cardW = (avail - (perPage - 1) * GAP) / perPage;
            start = barX + ARROW + GAP;
        }
        int pages = (count + perPage - 1) / perPage;
        if (page < 0) {
            page = current / perPage;
        }
        page = Math.max(0, Math.min(pages - 1, page));
        if (!fits) {
            String where = text("proteus.hub.page", page + 1, pages);
            if (gui.iconButton("hub-previous", barX, BAR_Y + 1, ARROW, BAR_H - 2, "left", ButtonKind.NORMAL,
                    page > 0, where)) {
                page--;
            }
            if (gui.iconButton("hub-next", barX + barW - ARROW, BAR_Y + 1, ARROW, BAR_H - 2, "right",
                    ButtonKind.NORMAL, page < pages - 1, where)) {
                page++;
            }
        }
        int first = page * perPage;
        for (int i = first; i < Math.min(count, first + perPage); i++) {
            Hub.Tab tab = tabs.get(i);
            int x = start + (i - first) * (cardW + GAP);
            if (gui.tabCard("hub:" + tab.id(), x, BAR_Y, cardW, BAR_H, tab.title().get(), tab.icon(), i == current,
                    null)) {
                select(i);
            }
        }
    }

    @Override
    public void mousePressed(int button) {
        if (overBar(mouseX, mouseY)) {
            gui.press(button);
        } else {
            app.mousePressed(button);
        }
    }

    @Override
    public void mouseReleased(int button) {
        gui.release(button);
        app.mouseReleased(button);
    }

    @Override
    public void mouseScrolled(double amount) {
        if (overBar(mouseX, mouseY)) {
            page += amount > 0 ? -1 : 1;
        } else {
            app.mouseScrolled(amount);
        }
    }

    @Override
    public boolean keyPressed(int key, int modifiers) {
        return app.keyPressed(key, modifiers);
    }

    @Override
    public boolean charTyped(int codepoint) {
        return app.charTyped(codepoint);
    }

    @Override
    public boolean vanillaBackdrop(boolean inWorld) {
        return app.vanillaBackdrop(inWorld);
    }

    @Override
    public void removed() {
        app.removed();
    }

    @Override
    public void closed() {
        app.closed();
    }

    @Override
    public float visibility() {
        return app.visibility();
    }

    private static String text(String key, Object... arguments) {
        return Component.translatable(key, arguments).getString();
    }
}
