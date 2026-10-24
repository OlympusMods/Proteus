package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.UiApp;
import com.github.olympus.proteus.client.ui.UiHost;
import java.util.function.Function;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public final class ProteusScreen extends Screen implements UiHost {
    private static final Map<String, Integer> CODES = codes();

    private final Screen parent;
    private final UiApp ui;
    private final GuiCanvas canvas = new GuiCanvas();

    public ProteusScreen(Component title, Screen parent, Function<UiHost, UiApp> app) {
        super(title);
        this.parent = parent;
        this.ui = app.apply(this);
    }

    private static Map<String, Integer> codes() {
        Map<String, Integer> codes = new HashMap<>();
        for (String name : new String[] {"KEY_BACKSPACE", "KEY_DELETE", "KEY_HOME", "KEY_END"}) {
            try {
                codes.put(name, InputConstants.class.getField(name).getInt(null));
            } catch (ReflectiveOperationException exception) {
                codes.put(name, -1);
            }
        }
        return codes;
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
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (ui.vanillaBackdrop(inWorld())) {
            super.extractBackground(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        canvas.bind(graphics, font, width, height);
        ui.render(canvas, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        ui.mousePressed(event.button());
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        ui.mouseReleased(event.button());
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        ui.mouseScrolled(scrollY);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = translate(event);
        int modifiers = shortcut(event) ? GLFW.GLFW_MOD_CONTROL : 0;
        return key != 0 && ui.keyPressed(key, modifiers) || super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return ui.charTyped(event.codepoint());
    }

    private static boolean shortcut(KeyEvent event) {
        return event.isSelectAll() || event.isCopy() || event.isPaste() || event.isCut();
    }

    private static int translate(KeyEvent event) {
        if (event.isEscape()) {
            return GLFW.GLFW_KEY_ESCAPE;
        }
        if (event.isConfirmation()) {
            return GLFW.GLFW_KEY_ENTER;
        }
        if (event.isLeft()) {
            return GLFW.GLFW_KEY_LEFT;
        }
        if (event.isRight()) {
            return GLFW.GLFW_KEY_RIGHT;
        }
        if (event.isSelectAll()) {
            return GLFW.GLFW_KEY_A;
        }
        if (event.isCopy()) {
            return GLFW.GLFW_KEY_C;
        }
        if (event.isPaste()) {
            return GLFW.GLFW_KEY_V;
        }
        if (event.isCut()) {
            return GLFW.GLFW_KEY_X;
        }
        int key = event.key();
        if (key == CODES.get("KEY_BACKSPACE")) {
            return GLFW.GLFW_KEY_BACKSPACE;
        }
        if (key == CODES.get("KEY_DELETE")) {
            return GLFW.GLFW_KEY_DELETE;
        }
        if (key == CODES.get("KEY_HOME")) {
            return GLFW.GLFW_KEY_HOME;
        }
        if (key == CODES.get("KEY_END")) {
            return GLFW.GLFW_KEY_END;
        }
        return 0;
    }
}
