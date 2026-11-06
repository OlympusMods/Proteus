package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ui.IconShape;
import com.github.olympus.proteus.client.ui.Menus;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class MenuWorldIconMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"))
    private void proteus$round(GuiGraphics graphics, RenderPipeline pipeline,
            ResourceLocation icon, int x, int y, float u, float v, int w, int h,
            int textureW, int textureH) {
        if (w != h || Menus.iconRadius(w) <= 0) {
            graphics.blit(pipeline, icon, x, y, u, v, w, h, textureW, textureH);
            return;
        }
        for (int[] run : IconShape.runs(w, Menus.iconRadius(w))) {
            graphics.blit(pipeline, icon, x + run[0], y + run[1], u + run[0], v + run[1], run[2], run[3], textureW,
                    textureH);
        }
    }
}
