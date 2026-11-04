package com.github.olympus.proteus.client.ui;

public interface UiApp {
    void resize(int width, int height);

    void tick();

    void requestClose();

    void render(Canvas canvas, int mouseX, int mouseY);

    void mousePressed(int button);

    void mouseReleased(int button);

    void mouseScrolled(double amount);

    boolean keyPressed(int key, int modifiers);

    boolean charTyped(int codepoint);

    boolean vanillaBackdrop(boolean inWorld);

    default void removed() {
    }

    default void closed() {
    }

    default float visibility() {
        return 1f;
    }

    default void skipOpening() {
    }
}
