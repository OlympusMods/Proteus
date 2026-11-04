package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.Canvas;
import com.github.olympus.proteus.client.ui.Draw;
import com.github.olympus.proteus.client.ui.Glass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class GuiCanvas implements Canvas {
    private static final Map<String, ItemStack> STACKS = new HashMap<>();

    private GuiGraphics graphics;
    private Font font;
    private int width;
    private int height;

    void bind(GuiGraphics graphics, Font font, int width, int height) {
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
        graphics.drawString(font, text, x, y, argb, shadow);
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
        graphics.pose().pushPose();
    }

    @Override
    public void pop() {
        graphics.pose().popPose();
    }

    @Override
    public void translate(float x, float y) {
        graphics.pose().translate(x, y, 0f);
    }

    @Override
    public void scale(float x, float y) {
        graphics.pose().scale(x, y, 1f);
    }

    @Override
    public void rotate(float degrees) {
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(degrees));
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
                ResourceLocation id = name.indexOf(':') < 0
                        ? ResourceLocation.withDefaultNamespace(name)
                        : ResourceLocation.parse(name);
                Item item = BuiltInRegistries.ITEM.get(id);
                stack = new ItemStack(item == Items.AIR ? Items.MUSIC_DISC_CAT : item);
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
            graphics.renderFakeItem(stack, x, y);
        }
    }

    @Override
    public void sprite(String path, int x, int y, int w, int h) {
        int colon = path.indexOf(':');
        ResourceLocation location = colon < 0
                ? ResourceLocation.fromNamespaceAndPath("proteus", path)
                : ResourceLocation.fromNamespaceAndPath(path.substring(0, colon), path.substring(colon + 1));
        RenderSystem.enableBlend();
        graphics.blitSprite(location, x, y, w, h);
        RenderSystem.disableBlend();
    }

    @Override
    public void head(UUID player, int x, int y, int size) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo info = connection == null ? null : connection.getPlayerInfo(player);
        PlayerSkin skin = info == null ? DefaultPlayerSkin.get(player) : info.getSkin();
        PlayerFaceRenderer.draw(graphics, skin, x, y, size);
    }

    @Override
    public boolean exists(String namespace, String path) {
        return Minecraft.getInstance().getResourceManager()
                .getResource(ResourceLocation.fromNamespaceAndPath(namespace, path)).isPresent();
    }

    @Override
    public void tiled(String namespace, String path, int x, int y, int w, int h) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(namespace, path);
        graphics.blit(location, x, y, 0f, 0f, w, h, 32, 32);
    }

    @Override
    public void glass(int x, int y, int w, int h, Glass glass) {
        if (w <= 0 || h <= 0 || glass.opacity() <= 0f) {
            return;
        }
        if (!GlassRenderer.draw(graphics, x, y, w, h, glass)) {
            Draw.round(this, x, y, w, h, glass.radius(), glass.fallbackColor());
        }
    }
}
