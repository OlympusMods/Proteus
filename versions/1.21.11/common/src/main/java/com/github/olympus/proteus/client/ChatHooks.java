package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.ChatLayout;
import com.github.olympus.proteus.client.ui.ChatSkin;
import java.util.List;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.FormattedCharSequence;

public final class ChatHooks {
    private static final GuiCanvas CANVAS = new GuiCanvas();
    private static final ChatLayout LAYOUT = new ChatLayout();
    private static final int MAX_LINES = 64;
    private static final int TAG_GAP = 4;
    private static final int LINE_EDGE = -4;
    private static boolean warned;
    private static ChatLayout.Row current;

    private ChatHooks() {
    }

    public static void line(GuiMessage.Line line) {
        current = LAYOUT.ready() && ChatSkin.active() ? LAYOUT.row(line.content()) : null;
    }

    public static void begin(List<GuiMessage.Line> lines, int scroll, int perPage) {
        LAYOUT.clear();
        current = null;
        if (!ChatSkin.active()) {
            return;
        }
        try {
            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            int count = Math.min(lines.size() - scroll, perPage);
            if (count <= 0) {
                return;
            }
            int rowHeight = (int) (font.lineHeight * (minecraft.options.chatLineSpacing().get() + 1.0));
            Object[] keys = new Object[count];
            int[] widths = new int[count];
            boolean[] starts = new boolean[count];
            for (int i = 0; i < count; i++) {
                GuiMessage.Line line = lines.get(scroll + i);
                keys[i] = line.content();
                widths[i] = lineWidth(font, line);
                starts[i] = line.endOfEntry();
            }
            LAYOUT.build(keys, widths, starts, rowHeight, perPage * rowHeight, ChatSkin.spacing());
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    private static int lineWidth(Font font, GuiMessage.Line line) {
        int width = font.width(line.content());
        if (line.tag() != null && line.tag().icon() != null) {
            width = Math.max(width, width + TAG_GAP + line.tag().icon().width);
        }
        return width;
    }

    public static boolean lineStart(ChatComponent.ChatGraphicsAccess access, GuiMessage.Line line) {
        if (!LAYOUT.ready() || !ChatSkin.active()) {
            return false;
        }
        ChatLayout.Row row = LAYOUT.row(line.content());
        if (row == null) {
            return false;
        }
        if (row.hidden()) {
            return true;
        }
        float dy = row.lift();
        access.updatePose(pose -> pose.translate(0f, dy));
        return false;
    }

    public static void lineEnd(ChatComponent.ChatGraphicsAccess access, GuiMessage.Line line) {
        ChatLayout.Row row = LAYOUT.ready() && ChatSkin.active() ? LAYOUT.row(line.content()) : null;
        if (row != null && !row.hidden()) {
            float dy = row.lift();
            access.updatePose(pose -> pose.translate(0f, -dy));
        }
    }

    public static void fill(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color, boolean closed) {
        if (!placed(graphics, x0, y0, x1, y1, color, closed) && !themed(graphics, x0, y0, x1, y1, color, false)) {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }

    private static boolean placed(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color, boolean closed) {
        if (!LAYOUT.ready() || !ChatSkin.active() || (color & 0xFFFFFF) != 0 || x1 - x0 <= 2 || x0 != LINE_EDGE) {
            return false;
        }
        try {
            ChatLayout.Row row = current;
            if (row == null) {
                return false;
            }
            if (!row.hidden() && row.first()) {
                Minecraft minecraft = Minecraft.getInstance();
                Font font = minecraft.font;
                int chatBottom = y1 + row.index() * (y1 - y0);
                ChatSkin.Box box = row.box();
                ChatSkin.Box placed = new ChatSkin.Box(box.x(), box.y() + chatBottom, box.w(), box.h());
                CANVAS.bind(graphics, font, graphics.guiWidth(), graphics.guiHeight());
                ChatSkin.Sample sample = ChatSkin.message(row.key(), placed.w(), font.lineHeight);
                ChatSkin.bubble(CANVAS, placed, color, minecraft.options.textBackgroundOpacity().get(), sample,
                        closed);
            }
            return true;
        } catch (RuntimeException exception) {
            failed(exception);
            return false;
        }
    }

    public static void suggestBox(GuiGraphics graphics, int x0, int y0, int x1, int y1) {
        if (!suggesting()) {
            return;
        }
        try {
            Minecraft minecraft = Minecraft.getInstance();
            CANVAS.bind(graphics, minecraft.font, graphics.guiWidth(), graphics.guiHeight());
            ChatSkin.suggest(CANVAS, x0, y0, x1, y1);
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    public static void suggestRow(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        if (color == -1 || !suggesting()) {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }

    public static void suggest(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        if (suggesting()) {
            suggestBox(graphics, x0, y0, x1, y1);
        } else {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }

    private static boolean suggesting() {
        return ChatSkin.active() && ChatScreens.open();
    }

    public static void input(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        if (!themed(graphics, x0, y0, x1, y1, color, true)) {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }

    private static boolean themed(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color,
            boolean input) {
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

    public static void text(ActiveTextCollector collector, TextAlignment alignment, int x, int y,
            ActiveTextCollector.Parameters parameters, FormattedCharSequence line, boolean slide) {
        ChatSkin.Sample sample = ChatSkin.Sample.NONE;
        if (ChatSkin.active()) {
            try {
                Font font = Minecraft.getInstance().font;
                sample = ChatSkin.message(line, font.width(line), font.lineHeight);
            } catch (RuntimeException exception) {
                failed(exception);
            }
        }
        if (sample == ChatSkin.Sample.NONE) {
            collector.accept(alignment, x, y, parameters, line);
            return;
        }
        int dx = slide ? Math.round(sample.dx()) : 0;
        int dy = slide ? Math.round(sample.dy()) : 0;
        collector.accept(alignment, x + dx, y + dy, parameters.withOpacity(parameters.opacity() * sample.alpha()),
                line);
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
        LAYOUT.clear();
        current = null;
        if (!warned) {
            warned = true;
            ProteusClient.LOGGER.warn("Proteus: could not draw the chat, showing the normal one", exception);
        }
    }
}
