package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.ToastKind;
import com.github.olympus.proteus.client.ui.ToastOverlay;
import com.github.olympus.proteus.client.ui.UiPrefs;
import java.util.List;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

public final class VanillaToasts {
    private static boolean warned;

    private VanillaToasts() {
    }

    public static boolean replace(Toast toast) {
        try {
            return show(toast);
        } catch (RuntimeException exception) {
            if (!warned) {
                warned = true;
                ProteusClient.LOGGER.warn("Proteus: could not redraw a vanilla toast, showing the normal one",
                        exception);
            }
            return false;
        }
    }

    private static boolean show(Toast toast) {
        UiPrefs prefs = UiPrefs.get();
        Class<?> type = toast.getClass();
        if (type == AdvancementToast.class && prefs.replaces(ToastKind.ADVANCEMENTS)) {
            return advancement(toast);
        }
        if (type == RecipeToast.class && prefs.replaces(ToastKind.RECIPES)) {
            return recipe(toast);
        }
        if (type == TutorialToast.class && prefs.replaces(ToastKind.TUTORIAL)) {
            return tutorial(toast);
        }
        if (type == SystemToast.class && prefs.replaces(ToastKind.SYSTEM)) {
            return system(toast);
        }
        return false;
    }

    private static boolean advancement(Toast toast) {
        AdvancementHolder holder = ToastFields.read(toast, AdvancementHolder.class, 0);
        DisplayInfo display = holder == null ? null : holder.value().display().orElse(null);
        if (display == null) {
            return false;
        }
        AdvancementType frame = display.getType();
        show(frame.getDisplayName().getString(), display.getTitle().getString(),
                ToastFields.itemName(display.getIcon()));
        if (frame == AdvancementType.CHALLENGE) {
            play(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
        }
        return true;
    }

    private static boolean recipe(Toast toast) {
        List<?> recipes = ToastFields.read(toast, List.class, 0);
        if (recipes == null) {
            return false;
        }
        String icon = recipes.isEmpty() ? "" : ToastFields.itemName(ToastFields.last(recipes.get(0), ItemStack.class));
        show(Component.translatable("recipe.toast.title").getString(),
                Component.translatable("recipe.toast.description").getString(), icon);
        return true;
    }

    private static boolean tutorial(Toast toast) {
        Component title = ToastFields.read(toast, Component.class, 0);
        if (title != null) {
            Component message = ToastFields.read(toast, Component.class, 1);
            show(title.getString(), message == null ? "" : message.getString(), "");
            return true;
        }
        List<?> lines = ToastFields.read(toast, List.class, 0);
        if (lines == null || lines.isEmpty()) {
            return false;
        }
        show(ToastFields.text(lines.subList(0, 1), 0), ToastFields.text(lines, 1), "");
        return true;
    }

    private static boolean system(Toast toast) {
        Component title = ToastFields.read(toast, Component.class, 0);
        List<?> first = ToastFields.read(toast, List.class, 0);
        if (first == null) {
            return false;
        }
        if (title != null) {
            show(title.getString(), ToastFields.text(first, 0), "");
            return true;
        }
        List<?> second = ToastFields.read(toast, List.class, 1);
        if (second == null) {
            return false;
        }
        show(ToastFields.text(first, 0), ToastFields.text(second, 0), "");
        return true;
    }

    private static void show(String title, String body, String icon) {
        ToastOverlay.queue(title, body, icon);
        play(SoundEvents.UI_TOAST_IN);
    }

    private static void play(SoundEvent sound) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F, 1.0F));
    }
}
