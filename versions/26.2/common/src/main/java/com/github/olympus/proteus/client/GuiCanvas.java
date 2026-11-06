package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.Canvas;
import com.github.olympus.proteus.client.ui.Draw;
import com.github.olympus.proteus.client.ui.Glass;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class GuiCanvas implements Canvas {
    private static final Map<String, ItemStack> STACKS = new HashMap<>();

    private static final Object PIPELINE = pipeline();
    private static final Method BLIT_SPRITE = method("blitSprite", 6);
    private static final Method BLIT = method("blit", 10);
    private static final Method BLIT_TINTED = tinted();

    private GuiGraphicsExtractor graphics;
    private Font font;
    private int width;
    private int height;

    public void bind(GuiGraphicsExtractor graphics, Font font, int width, int height) {
        this.graphics = graphics;
        this.font = font;
        this.width = width;
        this.height = height;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public void fill(int x, int y, int w, int h, int argb) {
        graphics.fill(x, y, x + w, y + h, argb);
    }

    @Override
    public void gradient(int x, int y, int w, int h, int topArgb, int bottomArgb) {
        graphics.fillGradient(x, y, x + w, y + h, topArgb, bottomArgb);
    }

    @Override
    public void text(String text, int x, int y, int argb, boolean shadow) {
        graphics.text(font, text, x, y, argb, shadow);
    }

    @Override
    public int textWidth(String text) {
        return font.width(text);
    }

    @Override
    public int lineHeight() {
        return font.lineHeight;
    }

    @Override
    public void push() {
        graphics.pose().pushMatrix();
    }

    @Override
    public void pop() {
        graphics.pose().popMatrix();
    }

    @Override
    public void translate(float x, float y) {
        graphics.pose().translate(x, y);
    }

    @Override
    public void scale(float x, float y) {
        graphics.pose().scale(x, y);
    }

    @Override
    public void rotate(float degrees) {
        graphics.pose().rotate((float) Math.toRadians(degrees));
    }

    @Override
    public void clip(int x, int y, int w, int h) {
        graphics.enableScissor(x, y, x + w, y + h);
    }

    @Override
    public void unclip() {
        graphics.disableScissor();
    }

    private static ItemStack stack(String name) {
        ItemStack stack = STACKS.get(name);
        if (stack == null) {
            try {
                stack = new ItemStack(BuiltInRegistries.ITEM
                        .getOptional(name.indexOf(':') < 0 ? Identifier.withDefaultNamespace(name) : Identifier.parse(name))
                        .orElse(Items.MUSIC_DISC_CAT));
            } catch (RuntimeException exception) {
                return null;
            }
            STACKS.put(name, stack);
        }
        return stack;
    }

    @Override
    public boolean hasItem(String name) {
        return stack(name) != null;
    }

    @Override
    public void item(String name, int x, int y) {
        ItemStack stack = stack(name);
        if (stack != null) {
            graphics.fakeItem(stack, x, y);
        }
    }

    @Override
    public void sprite(String path, int x, int y, int w, int h) {
        int colon = path.indexOf(':');
        Identifier location = colon < 0
                ? Identifier.fromNamespaceAndPath("proteus", path)
                : Identifier.fromNamespaceAndPath(path.substring(0, colon), path.substring(colon + 1));
        try {
            BLIT_SPRITE.invoke(graphics, PIPELINE, location, x, y, w, h);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not draw the sprite " + path, exception);
        }
    }

    @Override
    public void effectIcon(String effect, int x, int y, int size, float alpha) {
        Identifier sprite = Identifier.parse(effect).withPrefix("mob_effect/");
        int color = (Math.round(Math.max(0f, Math.min(1f, alpha)) * 255f) << 24) | 0xFFFFFF;
        try {
            BLIT_TINTED.invoke(graphics, PIPELINE, sprite, x, y, size, size, color);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not draw the effect icon " + effect, exception);
        }
    }

    @Override
    public void head(UUID player, int x, int y, int size) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        PlayerInfo info = connection == null ? null : connection.getPlayerInfo(player);
        PlayerSkin skin = info == null ? DefaultPlayerSkin.get(player) : info.getSkin();
        PlayerFaceExtractor.extractRenderState(graphics, skin, x, y, size);
    }

    @Override
    public boolean exists(String namespace, String path) {
        return Minecraft.getInstance().getResourceManager()
                .getResource(Identifier.fromNamespaceAndPath(namespace, path)).isPresent();
    }

    @Override
    public void tiled(String namespace, String path, int x, int y, int w, int h) {
        Identifier location = Identifier.fromNamespaceAndPath(namespace, path);
        try {
            BLIT.invoke(graphics, PIPELINE, location, x, y, 0f, 0f, w, h, 32, 32);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not draw the texture " + path, exception);
        }
    }

    private static Object pipeline() {
        try {
            return Class.forName("net.minecraft.client.renderer.RenderPipelines").getField("GUI_TEXTURED").get(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not find the GUI textured pipeline", exception);
        }
    }

    private static Method tinted() {
        for (Method candidate : GuiGraphicsExtractor.class.getMethods()) {
            Class<?>[] types = candidate.getParameterTypes();
            if (candidate.getName().equals("blitSprite") && types.length == 7
                    && types[0].getSimpleName().equals("RenderPipeline") && types[1] == Identifier.class
                    && types[6] == int.class) {
                return candidate;
            }
        }
        throw new IllegalStateException("GuiGraphicsExtractor.blitSprite with a color was not found");
    }

    private static Method method(String name, int parameters) {
        for (Method candidate : GuiGraphicsExtractor.class.getMethods()) {
            Class<?>[] types = candidate.getParameterTypes();
            if (candidate.getName().equals(name) && types.length == parameters
                    && types[0].getSimpleName().equals("RenderPipeline") && types[1] == Identifier.class) {
                return candidate;
            }
        }
        throw new IllegalStateException("GuiGraphicsExtractor." + name + " was not found");
    }

    @Override
    public void glass(int x, int y, int w, int h, Glass glass) {
        if (w <= 0 || h <= 0 || glass.opacity() <= 0f) {
            return;
        }
        if (!GlassRenderer.submit(graphics, x, y, w, h, glass, width, height)) {
            Draw.round(this, x, y, w, h, glass.radius(), glass.fallbackColor());
        }
    }
}
