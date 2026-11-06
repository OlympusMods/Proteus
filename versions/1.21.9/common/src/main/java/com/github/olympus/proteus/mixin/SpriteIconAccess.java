package com.github.olympus.proteus.mixin;

import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.WidgetSprites;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteIconButton.class)
public interface SpriteIconAccess {
    @Accessor("sprite")
    WidgetSprites proteus$sprite();

    @Accessor("spriteWidth")
    int proteus$spriteWidth();

    @Accessor("spriteHeight")
    int proteus$spriteHeight();
}
