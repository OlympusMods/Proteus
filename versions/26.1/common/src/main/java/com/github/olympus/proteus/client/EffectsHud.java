package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.EffectInfo;
import com.github.olympus.proteus.client.ui.EffectVisibility;
import com.github.olympus.proteus.client.ui.EffectsOverlay;
import com.google.common.collect.Ordering;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public final class EffectsHud {
    private static final GuiCanvas CANVAS = new GuiCanvas();
    private static final String MISSING = "minecraft:missing";

    private EffectsHud() {
    }

    public static boolean hud(GuiGraphicsExtractor graphics) {
        if (!EffectsOverlay.hudActive()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return false;
        }
        Screen screen = minecraft.screen;
        if (screen != null && screen.showsActiveEffects()) {
            EffectsOverlay.hudHidden();
            return true;
        }
        CANVAS.bind(graphics, minecraft.font, graphics.guiWidth(), graphics.guiHeight());
        return EffectsOverlay.hud(CANVAS, player, infos(player.getActiveEffects(), true), minecraft.isDemo(),
                minecraft.level.tickRateManager().tickrate());
    }

    public static boolean inventory(GuiGraphicsExtractor graphics, int left, int top, int available, int mouseX,
            int mouseY) {
        if (!EffectsOverlay.inventoryActive()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return false;
        }
        CANVAS.bind(graphics, minecraft.font, graphics.guiWidth(), graphics.guiHeight());
        return EffectsOverlay.inventory(CANVAS, player, infos(player.getActiveEffects(), false), left, top,
                available, mouseX, mouseY, minecraft.level.tickRateManager().tickrate());
    }

    public static void tip(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        CANVAS.bind(graphics, minecraft.font, graphics.guiWidth(), graphics.guiHeight());
        EffectsOverlay.tip(CANVAS);
    }

    private static List<EffectInfo> infos(Collection<MobEffectInstance> active, boolean reverse) {
        Ordering<MobEffectInstance> order = reverse ? Ordering.<MobEffectInstance>natural().reverse()
                : Ordering.<MobEffectInstance>natural();
        List<EffectInfo> result = new ArrayList<>();
        for (MobEffectInstance instance : order.sortedCopy(active)) {
            Holder<MobEffect> holder = instance.getEffect();
            MobEffect effect = holder.value();
            String id = holder.unwrapKey().map(ResourceKey::identifier).map(Object::toString).orElse(MISSING);
            result.add(new EffectInfo(id, effect.getDisplayName().getString(), instance.getAmplifier(),
                    instance.getDuration(), instance.isInfiniteDuration(), instance.isAmbient(),
                    effect.isBeneficial(), instance.showIcon() && EffectVisibility.gui(instance),
                    EffectVisibility.inventory(instance)));
        }
        return result;
    }
}
