package com.github.olympus.proteus.client.ji;

import com.github.olympus.proteus.client.ProteusScreen;
import com.ji.afkcinematic.config.ConfigScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public final class JiConfigScreen extends ConfigScreen {
    private final ProteusScreen view;

    public JiConfigScreen(Screen parent) {
        super(parent);
        this.view = new ProteusScreen(getTitle(), parent, JiApp::new);
    }

    @Override
    protected void init() {
        JiInit.init(view, minecraft, width, height);
    }

    @Override
    public void tick() {
        view.tick();
    }

    @Override
    public void removed() {
        view.removed();
    }

    @Override
    public void onClose() {
        view.onClose();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        view.renderBackground(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        view.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return view.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return view.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return view.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        return view.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        return view.app() instanceof JiKeyListener listener && listener.keyReleased(key);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        return view.charTyped(character, modifiers);
    }
}
