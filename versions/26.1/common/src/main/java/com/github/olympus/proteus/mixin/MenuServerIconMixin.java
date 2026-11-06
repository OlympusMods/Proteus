package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ui.IconShape;
import com.github.olympus.proteus.client.ui.Menus;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public abstract class MenuServerIconMixin {
    private static final int SIZE = 32;

    @Inject(method = "extractIcon", at = @At("HEAD"), cancellable = true)
    private void proteus$round(GuiGraphicsExtractor graphics, int x, int y, Identifier icon, CallbackInfo callback) {
        if (Menus.iconRadius(SIZE) <= 0) {
            return;
        }
        for (int[] run : IconShape.runs(SIZE, Menus.iconRadius(SIZE))) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, icon, x + run[0], y + run[1], (float) run[0], (float) run[1],
                run[2], run[3], SIZE, SIZE);
        }
        callback.cancel();
    }
}
