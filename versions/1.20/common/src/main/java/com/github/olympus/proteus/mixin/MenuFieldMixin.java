package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.MenuHud;
import com.github.olympus.proteus.client.ui.Menus;
import com.github.olympus.proteus.client.ui.Themes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
public abstract class MenuFieldMixin {
    private static final int VANILLA_TEXT = 0xE0E0E0;

    @Shadow
    private int textColor;
    @Shadow
    private boolean bordered;

    private boolean proteus$drawn;
    private int proteus$color;

    @Inject(method = "renderWidget", at = @At("HEAD"))
    private void proteus$field(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        EditBox self = (EditBox) (Object) this;
        proteus$drawn = bordered && self.isVisible() && MenuHud.field(self, self.isFocused());
        proteus$color = textColor;
        if (proteus$drawn && (textColor & 0xFFFFFF) == VANILLA_TEXT && !Menus.darkChrome()) {
            textColor = Themes.current().textPrimary();
        }
    }

    @Redirect(method = "renderWidget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/EditBox;isBordered()Z"))
    private boolean proteus$skipBorder(EditBox box) {
        return !proteus$drawn && bordered;
    }

    @Inject(method = "renderWidget", at = @At("RETURN"))
    private void proteus$restore(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo callback) {
        textColor = proteus$color;
        proteus$drawn = false;
    }
}
