package com.github.olympus.proteus.mixin.malilib;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccess {
    @Accessor("scissorStack")
    GuiGraphics.ScissorStack proteus$scissors();
}
