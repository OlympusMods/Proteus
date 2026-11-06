package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSelectionList.class)
public abstract class MenuListMixin {
    @Shadow
    protected abstract boolean isSelectedItem(int index);

    @Inject(method = "renderItem", at = @At("HEAD"), require = 0)
    private void proteus$row(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, int index, int left,
            int top, int width, int height, CallbackInfo callback) {
        AbstractSelectionList<?> list = (AbstractSelectionList<?>) (Object) this;
        if (!MenuHud.themedLists() || list instanceof ContainerObjectSelectionList<?>) {
            return;
        }
        int x = left - 2;
        int y = top - 2;
        int h = height + 4;
        boolean over = list.isMouseOver(mouseX, mouseY) && mouseX >= x && mouseX < x + width && mouseY >= y
                && mouseY < y + h;
        boolean chosen = isSelectedItem(index);
        if (over || chosen) {
            MenuHud.row(list.children().get(index), x, y, width, h, true, chosen);
        }
    }

    @Redirect(method = "renderSelection", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"), require = 0)
    private void proteus$outline(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        if (!MenuHud.themedLists()) {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V",
            ordinal = 0), require = 0)
    private void proteus$background(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v,
            int w, int h, int textureW, int textureH) {
        if (!MenuHud.listBackground(x, y, w, h)) {
            graphics.blit(texture, x, y, u, v, w, h, textureW, textureH);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V",
            ordinal = 1), require = 0)
    private void proteus$top(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int w,
            int h, int textureW, int textureH) {
        if (!MenuHud.listEdge(x, y, w, h)) {
            graphics.blit(texture, x, y, u, v, w, h, textureW, textureH);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V",
            ordinal = 2), require = 0)
    private void proteus$bottom(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int w,
            int h, int textureW, int textureH) {
        if (!MenuHud.listEdge(x, y, w, h)) {
            graphics.blit(texture, x, y, u, v, w, h, textureW, textureH);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(Lnet/minecraft/client/renderer/RenderType;"
                    + "IIIIIII)V"), require = 0)
    private void proteus$fade(GuiGraphics graphics, RenderType type, int x0, int y0, int x1, int y1, int z, int from,
            int to) {
        if (!MenuHud.themedLists()) {
            graphics.fillGradient(type, x0, y0, x1, y1, z, from, to);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 0), require = 0)
    private void proteus$track(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        if (!MenuHud.scrollTrack(x0, y0, x1 - x0, y1 - y0)) {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 1), require = 0)
    private void proteus$thumb(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        if (!MenuHud.scrollThumb(x0, y0, x1 - x0, y1 - y0)) {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 2), require = 0)
    private void proteus$thumbLight(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        if (!MenuHud.listScope()) {
            graphics.fill(x0, y0, x1, y1, color);
        }
    }
}
