package com.github.olympus.proteus.mixin;

import com.github.olympus.proteus.client.ProteusClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Example mixin, kept so every loader project already has its mixin setup working. */
@Mixin(Minecraft.class)
public abstract class ClientTickMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void proteus$tick(CallbackInfo callback) {
        ProteusClient.tick((Minecraft) (Object) this);
    }
}
