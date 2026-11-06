package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.Menus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

public final class MenuHud {
    private static final GuiCanvas CANVAS = new GuiCanvas();

    private MenuHud() {
    }

    public static void begin(Screen screen, GuiGraphicsExtractor graphics) {
        try {
            if (Menus.enter(screen)) {
                CANVAS.bind(graphics, Minecraft.getInstance().font, screen.width, screen.height);
                Menus.backdrop(CANVAS);
            }
        } catch (RuntimeException | LinkageError error) {
            Menus.fail(error);
        }
    }

    public static void end() {
        Menus.exit();
    }

    public static boolean active() {
        return Menus.active();
    }

    public static boolean button(AbstractWidget widget, boolean label) {
        if (!Menus.active()) {
            return false;
        }
        try {
            Menus.button(CANVAS, widget, label);
            return true;
        } catch (RuntimeException | LinkageError error) {
            Menus.fail(error);
            return false;
        }
    }

    public static boolean skipsBlur(Screen screen) {
        return Menus.kindOf(screen) == Menus.Kind.PAUSE && !Menus.keepsVanillaBlur();
    }
}
