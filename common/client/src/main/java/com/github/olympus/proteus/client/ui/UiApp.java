package com.github.olympus.proteus.client.ui;

/** What a mod hands to ProteusScreen: the screen forwards drawing and input to it. */
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

    /** The screen was taken off the display, for whatever reason. */
    default void removed() {
    }

    /** The close animation finished and the screen is going back to its parent. */
    default void closed() {
    }
}
