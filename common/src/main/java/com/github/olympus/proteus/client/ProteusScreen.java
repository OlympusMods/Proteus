package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.Hub;
import com.github.olympus.proteus.client.ui.UiApp;
import com.github.olympus.proteus.client.ui.UiHost;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public final class ProteusScreen extends Screen implements UiHost {
    private final Screen parent;
    private final UiApp ui;
    private final GuiCanvas canvas = new GuiCanvas();

    public ProteusScreen(Component title, Screen parent, Function<UiHost, UiApp> app) {
        super(title);
        this.parent = parent;
        this.ui = app.apply(this);
    }

    public static Screen open(Screen parent, String tab) {
        return new ProteusScreen(Component.translatable("proteus.hub.title"), parent, Hub.app(tab));
    }

    @Override
    protected void init() {
        ui.resize(width, height);
    }

    @Override
    public void tick() {
        ui.tick();
    }

    @Override
    public void onClose() {
        ui.requestClose();
    }

    @Override
    public void removed() {
        ui.removed();
    }

    @Override
    public void close() {
        ui.closed();
        minecraft.setScreen(parent);
    }

    @Override
    public void playClick() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean inWorld() {
        return minecraft != null && minecraft.level != null;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (ui.vanillaBackdrop(inWorld())) {
            super.renderBackground(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        canvas.bind(graphics, font, width, height);
        ui.render(canvas, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ui.mousePressed(button);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        ui.mouseReleased(button);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        ui.mouseScrolled(scrollY);
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        return ui.keyPressed(key, modifiers) || super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        return ui.charTyped(character);
    }
}
