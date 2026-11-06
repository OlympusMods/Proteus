package com.github.olympus.proteus.mixin.malilib;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ButtonBase.class, remap = false)
public interface ButtonBaseAccess {
    @Accessor("enabled")
    boolean proteus$enabled();

    @Accessor("hovered")
    boolean proteus$hovered();
}
