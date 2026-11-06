package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.ChatSkin;
import com.github.olympus.proteus.client.ui.ChatSkin.Box;
import com.github.olympus.proteus.client.ui.ChatSkin.Sample;
import com.github.olympus.proteus.client.ui.ChatSkin.Spacing;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.util.FormattedCharSequence;

public final class ChatHooks {
    private static final GuiCanvas CANVAS = new GuiCanvas();
    private static final int MAX_LINES = 64;
    private static final Map<GuiMessage.Line, Slot> SLOTS = new IdentityHashMap<>();
    private static boolean warned;
    private static boolean layout;
    private static boolean armed;
    private static Pending pending;

    private record Slot(int groups, boolean top, Box box, Spacing spacing, boolean hidden) {
    }

    private record Pending(Box box, float fade, double backgroundOpacity, Sample sample) {
    }

    private ChatHooks() {
    }

    public static void begin(List<GuiMessage.Line> lines, int scroll, int perPage) {
        SLOTS.clear();
        layout = false;
        armed = false;
        pending = null;
        if (!ChatSkin.active()) {
            return;
        }
        try {
            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            int rowHeight = (int) (font.lineHeight * (minecraft.options.chatLineSpacing().get() + 1.0));
            int count = Math.min(lines.size() - scroll, perPage);
            Spacing spacing = ChatSkin.spacing();
            int height = perPage * rowHeight;
            int first = 0;
            int groups = 0;
            while (first < count) {
                GuiMessage parent = lines.get(scroll + first).parent();
                int end = first;
                int width = 0;
                while (end < count && lines.get(scroll + end).parent() == parent) {
                    width = Math.max(width, lineWidth(font, lines.get(scroll + end)));
                    end++;
                }
                Box box = ChatSkin.bubble(first, end - first, rowHeight, groups, width, spacing);
                boolean hidden = !ChatSkin.fits(box, height);
                for (int i = first; i < end; i++) {
                    SLOTS.put(lines.get(scroll + i), new Slot(groups, i == end - 1, box, spacing, hidden));
                }
                if (hidden) {
                    break;
                }
                groups++;
                first = end;
            }
            layout = true;
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    private static int lineWidth(Font font, GuiMessage.Line line) {
        int width = font.width(line.content());
        if (line.tag() != null && line.tag().icon() != null) {
            width = Math.max(width, line.getTagIconLeft(font) + line.tag().icon().width);
        }
        return width;
    }

    public static void background(int chatBottom, GuiMessage.Line line, float alpha, float backgroundOpacity) {
        armed = false;
        pending = null;
        if (!layout || !ChatSkin.active()) {
            return;
        }
        armed = true;
        Slot slot = SLOTS.get(line);
        if (slot == null || slot.hidden() || !slot.top()) {
            return;
        }
        try {
            Font font = Minecraft.getInstance().font;
            Box box = slot.box();
            Sample sample = ChatSkin.message(line.content(), box.w(), font.lineHeight);
            pending = new Pending(new Box(box.x(), box.y() + chatBottom, box.w(), box.h()), alpha, backgroundOpacity,
                    sample);
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    public static boolean lineStart(ChatComponent.ChatGraphicsAccess access, GuiMessage.Line line) {
        if (!layout || !ChatSkin.active()) {
            return false;
        }
        Slot slot = SLOTS.get(line);
        if (slot == null || slot.hidden()) {
            return true;
        }
        float dy = ChatSkin.lift(slot.groups(), slot.spacing());
        access.updatePose(pose -> pose.translate(0f, dy));
        return false;
    }

    public static void lineEnd(ChatComponent.ChatGraphicsAccess access, GuiMessage.Line line) {
        Slot slot = layout ? SLOTS.get(line) : null;
        if (slot != null && !slot.hidden() && ChatSkin.active()) {
            float dy = ChatSkin.lift(slot.groups(), slot.spacing());
            access.updatePose(pose -> pose.translate(0f, -dy));
        }
    }

    public static void fill(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color,
            boolean closed) {
        if (armed) {
            armed = false;
            Pending bubble = pending;
            pending = null;
            if (bubble != null && ChatSkin.active()) {
                try {
                    Minecraft minecraft = Minecraft.getInstance();
                    CANVAS.bind(graphics, minecraft.font, graphics.guiWidth(), graphics.guiHeight());
                    ChatSkin.bubble(CANVAS, bubble.box(), bubble.fade(), bubble.backgroundOpacity(), bubble.sample(),
                            closed);
                } catch (RuntimeException exception) {
                    failed(exception);
                }
            }
            if (ChatSkin.active()) {
                return;
            }
        }
        if (!themed(graphics, x0, y0, x1, y1, color, false)) {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }

    public static void suggestBox(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1) {
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

    public static void suggestRow(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color) {
        if (color == -1 || !suggesting()) {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }

    public static void suggest(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color) {
        if (suggesting()) {
            suggestBox(graphics, x0, y0, x1, y1);
        } else {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }

    private static boolean suggesting() {
        return ChatSkin.active() && ChatScreens.open();
    }

    public static void input(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color) {
        if (!themed(graphics, x0, y0, x1, y1, color, true)) {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }

    private static boolean themed(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color,
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

    public static void born(List<GuiMessage.Line> lines, GuiMessage message) {
        if (!ChatSkin.active()) {
            return;
        }
        try {
            for (int i = 0; i < lines.size() && i < MAX_LINES; i++) {
                GuiMessage.Line line = lines.get(i);
                if (line.parent() != message) {
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
        layout = false;
        armed = false;
        pending = null;
        if (!warned) {
            warned = true;
            ProteusClient.LOGGER.warn("Proteus: could not draw the chat, showing the normal one", exception);
        }
    }
}
