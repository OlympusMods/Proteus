package com.github.olympus.proteus.client.ji;

import com.github.olympus.proteus.client.ProteusScreen;
import com.ji.afkcinematic.config.ConfigScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

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
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        view.extractBackground(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        view.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return view.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return view.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return view.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return view.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        return view.app() instanceof JiKeyListener listener && listener.keyReleased(event.key());
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return view.charTyped(event);
    }
}
