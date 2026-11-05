package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.Hub;
import com.github.olympus.proteus.client.ui.RawInput;
import com.github.olympus.proteus.client.ui.RawKey;
import com.github.olympus.proteus.client.ui.RawMouse;
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
    private final TextInput textInput = new TextInput(this);

    public ProteusScreen(Component title, Screen parent, Function<UiHost, UiApp> app) {
        super(title);
        this.parent = parent;
        this.ui = app.apply(this);
    }

    public UiApp app() {
        return ui;
    }

    public Screen parent() {
        return parent;
    }

    private static Map<String, Integer> codes() {
        Map<String, Integer> codes = new HashMap<>();
        for (String name : new String[] {"KEY_BACKSPACE", "KEY_DELETE", "KEY_HOME", "KEY_END", "KEY_SPACE", "KEY_Z", "KEY_Y", "MOUSE_BUTTON_LEFT",
                "MOUSE_BUTTON_RIGHT"}) {
            try {
                codes.put(name, InputConstants.class.getField(name).getInt(null));
            } catch (ReflectiveOperationException exception) {
                codes.put(name, -1);
            }
        }
        return codes;
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
        textInput.release();
        ui.removed();
    }

    @Override
    public void close() {
        ui.closed();
        minecraft.setScreenAndShow(parent);
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
        textInput.sync(ui.typing());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (ui instanceof RawInput raw && raw.rawMouse(new RawMouse(event.button()))) {
            return true;
        }
        ui.mousePressed(button(event.button()));
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        ui.mouseReleased(button(event.button()));
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        ui.mouseScrolled(scrollY);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (ui instanceof RawInput raw
                && raw.rawKey(new RawKey(event.key(), InputConstants.getKey(event), event.isEscape()))) {
            return true;
        }
        int key = translate(event);
        boolean control = shortcut(event) || event.hasControlDown();
        int modifiers = (control ? GLFW.GLFW_MOD_CONTROL : 0) | (event.hasShiftDown() ? GLFW.GLFW_MOD_SHIFT : 0);
        return key != 0 && ui.keyPressed(key, modifiers) || super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return ui.charTyped(event.codepoint());
    }

    private static int button(int button) {
        if (button == CODES.get("MOUSE_BUTTON_LEFT")) {
            return 0;
        }
        if (button == CODES.get("MOUSE_BUTTON_RIGHT")) {
            return 1;
        }
        return -1;
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
        if (event.isUp()) {
            return GLFW.GLFW_KEY_UP;
        }
        if (event.isDown()) {
            return GLFW.GLFW_KEY_DOWN;
        }
        if (event.isCycleFocus()) {
            return GLFW.GLFW_KEY_TAB;
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
        if (key == CODES.get("KEY_SPACE")) {
            return GLFW.GLFW_KEY_SPACE;
        }
        if (key == CODES.get("KEY_BACKSPACE")) {
            return GLFW.GLFW_KEY_BACKSPACE;
        }
        if (key == CODES.get("KEY_DELETE")) {
            return GLFW.GLFW_KEY_DELETE;
        }
        if (key == CODES.get("KEY_Z")) {
            return GLFW.GLFW_KEY_Z;
        }
        if (key == CODES.get("KEY_Y")) {
            return GLFW.GLFW_KEY_Y;
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
