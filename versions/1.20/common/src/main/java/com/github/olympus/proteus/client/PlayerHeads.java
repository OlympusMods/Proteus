package com.github.olympus.proteus.client;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

final class PlayerHeads {
    private PlayerHeads() {
    }

    static void draw(GuiGraphics graphics, UUID player, int x, int y, int size) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo info = connection == null ? null : connection.getPlayerInfo(player);
        ResourceLocation skin = info == null ? DefaultPlayerSkin.getDefaultSkin(player) : info.getSkinLocation();
        PlayerFaceRenderer.draw(graphics, skin, x, y, size);
    }
}
