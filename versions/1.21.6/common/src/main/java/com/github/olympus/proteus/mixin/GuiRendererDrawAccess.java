package com.github.olympus.proteus.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.render.TextureSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.render.GuiRenderer$Draw")
public interface GuiRendererDrawAccess {
    @Accessor("pipeline")
    RenderPipeline proteus$pipeline();

    @Accessor("textureSetup")
    TextureSetup proteus$textureSetup();
}
