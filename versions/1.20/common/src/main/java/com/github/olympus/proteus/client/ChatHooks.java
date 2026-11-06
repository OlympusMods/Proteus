package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.ChatSkin;
import com.github.olympus.proteus.client.ui.Colors;
import java.util.List;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.FormattedCharSequence;

public final class ChatHooks {
    private static final GuiCanvas CANVAS = new GuiCanvas();
    private static final int MAX_LINES = 64;
    private static boolean warned;

    private ChatHooks() {
    }

    public static void fill(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        if (!themed(graphics, x0, y0, x1, y1, color, false)) {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }

    public static void fill(GuiGraphics graphics, int x0, int y0, int x1, int y1, int z, int color) {
        if (!themed(graphics, x0, y0, x1, y1, color, false)) {
            graphics.fill(x0, y0, x1, y1, z, color);
        }
    }

    public static void input(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        if (!themed(graphics, x0, y0, x1, y1, color, true)) {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }

    private static boolean themed(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color, boolean input) {
        if (!ChatSkin.active()) {
            return false;
        }
        try {
            Minecraft minecraft = Minecraft.getInstance();
            CANVAS.bind(graphics, minecraft.font, graphics.guiWidth(), graphics.guiHeight());
            if (input) {
                ChatSkin.input(CANVAS, x0, y0, x1, y1);
                return true;
            }
            return ChatSkin.fill(CANVAS, x0, y0, x1, y1, color, minecraft.options.textBackgroundOpacity().get());
        } catch (RuntimeException exception) {
            failed(exception);
            return false;
        }
    }

    public static int text(GuiGraphics graphics, Font font, FormattedCharSequence line, int x, int y, int color) {
        ChatSkin.Sample sample = ChatSkin.Sample.NONE;
        boolean slide = false;
        if (ChatSkin.active()) {
            try {
                sample = ChatSkin.message(line, font.width(line), font.lineHeight);
                slide = !(Minecraft.getInstance().screen instanceof ChatScreen);
            } catch (RuntimeException exception) {
                failed(exception);
            }
        }
        if (sample == ChatSkin.Sample.NONE) {
            return graphics.drawString(font, line, x, y, color);
        }
        int dx = slide ? Math.round(sample.dx()) : 0;
        int dy = slide ? Math.round(sample.dy()) : 0;
        return graphics.drawString(font, line, x + dx, y + dy, Colors.alpha(color, sample.alpha()));
    }

    public static void born(List<GuiMessage.Line> lines, int tick) {
        if (!ChatSkin.active()) {
            return;
        }
        try {
            for (int i = 0; i < lines.size() && i < MAX_LINES; i++) {
                GuiMessage.Line line = lines.get(i);
                if (line.addedTime() != tick) {
                    break;
                }
                ChatSkin.born(line.content());
            }
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    private static void failed(RuntimeException exception) {
        ChatSkin.fail();
        if (!warned) {
            warned = true;
            ProteusClient.LOGGER.warn("Proteus: could not draw the chat, showing the normal one", exception);
        }
    }
}
