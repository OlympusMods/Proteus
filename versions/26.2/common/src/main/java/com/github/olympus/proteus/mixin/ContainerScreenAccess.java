package com.github.olympus.proteus.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenAccess {
    @Accessor("leftPos")
    int proteus$left();

    @Accessor("topPos")
    int proteus$top();

    @Accessor("imageWidth")
    int proteus$imageWidth();
}
