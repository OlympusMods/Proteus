package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import com.github.olympus.proteus.client.ui.Menus;
import com.github.olympus.proteus.client.ui.Themes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
public abstract class MenuFieldMixin {
    private static final int VANILLA_TEXT = -2039584;

    @Shadow
    private int textColor;
    @Shadow
    private boolean bordered;

    private boolean proteus$drawn;
    private int proteus$color;

    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"))
    private void proteus$field(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        EditBox self = (EditBox) (Object) this;
        proteus$drawn = bordered && self.isVisible() && MenuHud.field(self, self.isFocused());
        proteus$color = textColor;
        if (proteus$drawn && textColor == VANILLA_TEXT && !Menus.darkChrome()) {
            textColor = Themes.current().textPrimary();
        }
    }

    @Redirect(method = "extractWidgetRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/EditBox;isBordered()Z"))
    private boolean proteus$skipBorder(EditBox box) {
        return !proteus$drawn && box.isBordered();
    }

    @Inject(method = "extractWidgetRenderState", at = @At("RETURN"))
    private void proteus$restore(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        textColor = proteus$color;
        proteus$drawn = false;
    }
}
