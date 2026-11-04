package com.github.olympus.proteus.client;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

final class ToastFields {
    private static final Map<String, Optional<Field>> FIELDS = new ConcurrentHashMap<>();

    private ToastFields() {
    }

    static <T> T read(Object owner, Class<T> type, int index) {
        Optional<Field> field = FIELDS.computeIfAbsent(owner.getClass().getName() + "/" + type.getName() + "/" + index,
                key -> find(owner.getClass(), type, index));
        if (field.isEmpty()) {
            return null;
        }
        try {
            return type.cast(field.get().get(owner));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }

    static <T> T last(Object owner, Class<T> type) {
        T found = null;
        for (int i = 0; ; i++) {
            T next = read(owner, type, i);
            if (next == null) {
                return found;
            }
            found = next;
        }
    }

    private static Optional<Field> find(Class<?> owner, Class<?> type, int index) {
        int seen = 0;
        for (Field field : owner.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || !type.isAssignableFrom(field.getType())) {
                continue;
            }
            if (seen++ == index) {
                field.setAccessible(true);
                return Optional.of(field);
            }
        }
        return Optional.empty();
    }

    static String itemName(ItemStack stack) {
        return stack == null || stack.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    static String text(List<?> lines, int from) {
        StringBuilder text = new StringBuilder();
        for (int i = from; i < lines.size(); i++) {
            if (!(lines.get(i) instanceof FormattedCharSequence line)) {
                continue;
            }
            if (!text.isEmpty()) {
                text.append(' ');
            }
            line.accept((position, style, codepoint) -> {
                text.appendCodePoint(codepoint);
                return true;
            });
        }
        return text.toString();
    }
}
