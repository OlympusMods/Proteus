package com.github.olympus.proteus.client;

import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;

final class TypingProbe extends EditBox {
    private final BooleanSupplier typing;

    TypingProbe(Font font, BooleanSupplier typing) {
        super(font, 0, 0, 0, 0, CommonComponents.EMPTY);
        this.typing = typing;
    }

    @Override
    public boolean isFocused() {
        return typing.getAsBoolean();
    }
}
