package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.ChatLayout;
import com.github.olympus.proteus.client.ui.ChatSkin;
import com.github.olympus.proteus.client.ui.Colors;
import java.util.List;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;

public final class ChatHooks {
    private static final GuiCanvas CANVAS = new GuiCanvas();
    private static final ChatLayout LAYOUT = new ChatLayout();
    private static final int MAX_LINES = 64;
    private static final int TAG_GAP = 4;
    private static final int TAG_EDGE = -4;
    private static boolean warned;
    private static int rowHeight = 9;
    private static boolean inChat;
    private static boolean busy;
    private static ChatLayout.Row current;

    private ChatHooks() {
    }

    public static boolean inChat() {
        return inChat;
    }

    public static void enter() {
        inChat = ChatSkin.active();
    }

    public static void leave() {
        inChat = false;
        current = null;
    }

    public static void line(GuiMessage.Line line) {
        current = inChat && LAYOUT.ready() ? LAYOUT.row(line.content()) : null;
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
            rowHeight = (int) (font.lineHeight * (minecraft.options.chatLineSpacing().get() + 1.0));
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

    public static boolean fill(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        if (!inChat || busy) {
            return false;
        }
        busy = true;
        try {
            return placed(graphics, x0, y0, x1, y1, color) || themed(graphics, x0, y0, x1, y1, color, false);
        } catch (RuntimeException exception) {
            failed(exception);
            return false;
        } finally {
            busy = false;
        }
    }

    private static boolean placed(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        if (!LAYOUT.ready() || !ChatSkin.active()) {
            return false;
        }
        int width = x1 - x0;
        if ((color & 0xFFFFFF) == 0 && width > 2 && x0 == TAG_EDGE) {
            ChatLayout.Row row = current;
            if (row == null) {
                return false;
            }
            if (!row.hidden() && row.first()) {
                drawBubble(graphics, row, y0, y1, color);
            }
            return true;
        }
        if (current != null && width == 2 && x0 == TAG_EDGE) {
            if (!current.hidden()) {
                int dy = Math.round(current.lift());
                graphics.fill(x0, y0 + dy, x1, y1 + dy, color);
            }
            return true;
        }
        return false;
    }

    private static void drawBubble(GuiGraphics graphics, ChatLayout.Row row, int y0, int y1, int color) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int chatBottom = y1 + row.index() * (y1 - y0);
        ChatSkin.Box box = row.box();
        ChatSkin.Box placed = new ChatSkin.Box(box.x(), box.y() + chatBottom, box.w(), box.h());
        CANVAS.bind(graphics, font, graphics.guiWidth(), graphics.guiHeight());
        ChatSkin.Sample sample = ChatSkin.message(row.key(), placed.w(), font.lineHeight);
        ChatSkin.bubble(CANVAS, placed, color, minecraft.options.textBackgroundOpacity().get(), sample,
                !ChatScreens.open());
    }

    public static double chatY(double y) {
        if (!LAYOUT.ready() || !ChatSkin.active()) {
            return y;
        }
        return LAYOUT.locate(y, rowHeight);
    }

    public static boolean tagHidden() {
        return inChat && current != null && current.hidden() && LAYOUT.ready();
    }

    public static int tagLift() {
        return inChat && current != null && LAYOUT.ready() ? Math.round(current.lift()) : 0;
    }

    public static void input(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        if (!themed(graphics, x0, y0, x1, y1, color, true)) {
            graphics.fill(x0, y0, x1, y1, color);
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

    private static boolean themed(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color, boolean input) {
        if (!ChatSkin.active()) {
            return false;
        }
        boolean outer = !busy;
        busy = true;
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
        } finally {
            if (outer) {
                busy = false;
            }
        }
    }

    public static boolean text(GuiGraphics graphics, Font font, FormattedCharSequence line, int x, int y, int color) {
        if (!inChat || busy) {
            return false;
        }
        busy = true;
        try {
            ChatLayout.Row row = LAYOUT.ready() ? LAYOUT.row(line) : null;
            if (row != null && row.hidden()) {
                return true;
            }
            int lift = row == null ? 0 : Math.round(row.lift());
            ChatSkin.Sample sample = ChatSkin.message(line, font.width(line), font.lineHeight);
            if (sample == ChatSkin.Sample.NONE && lift == 0) {
                return false;
            }
            boolean slide = !ChatScreens.open();
            int dx = slide ? Math.round(sample.dx()) : 0;
            int dy = slide ? Math.round(sample.dy()) : 0;
            graphics.drawString(font, line, x + dx, y + lift + dy, Colors.alpha(color, sample.alpha()));
            return true;
        } catch (RuntimeException exception) {
            failed(exception);
            return false;
        } finally {
            busy = false;
        }
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
        inChat = false;
        LAYOUT.clear();
        current = null;
        if (!warned) {
            warned = true;
            ProteusClient.LOGGER.warn("Proteus: could not draw the chat, showing the normal one", exception);
        }
    }
}
