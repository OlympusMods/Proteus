package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.Menus;
import com.github.olympus.proteus.mixin.WidgetAlphaAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

public final class MenuHud {
    private static final GuiCanvas CANVAS = new GuiCanvas();

    private MenuHud() {
    }

    public static void begin(Screen screen, GuiGraphics graphics) {
        try {
            if (Menus.enter(screen)) {
                CANVAS.bind(graphics, Minecraft.getInstance().font, screen.width, screen.height);
            }
        } catch (RuntimeException | LinkageError error) {
            Menus.fail(error);
        }
    }

    public static void backdrop() {
        backdrop(Menus.scope());
    }

    public static void backdrop(Menus.Kind kind) {
        if (kind == Menus.Kind.NONE || Menus.scope() != kind) {
            return;
        }
        try {
            Menus.backdrop(CANVAS);
        } catch (RuntimeException | LinkageError error) {
            Menus.fail(error);
        }
    }

    public static void end() {
        Menus.exit();
    }

    public static boolean button(AbstractButton button) {
        if (!Menus.active()) {
            return false;
        }
        try {
            Menus.button(CANVAS, button, ButtonShape.labelled(button), ((WidgetAlphaAccess) button).proteus$alpha());
            return true;
        } catch (RuntimeException | LinkageError error) {
            Menus.fail(error);
            return false;
        }
    }

    public static boolean icon(AbstractWidget widget) {
        if (!Menus.active()) {
            return false;
        }
        try {
            return Menus.icon(CANVAS, widget, ((WidgetAlphaAccess) widget).proteus$alpha());
        } catch (RuntimeException | LinkageError error) {
            Menus.fail(error);
            return false;
        }
    }

    public static boolean skipsBlur(Screen screen) {
        return Menus.kindOf(screen) == Menus.Kind.PAUSE && !Menus.keepsVanillaBlur();
    }
}
