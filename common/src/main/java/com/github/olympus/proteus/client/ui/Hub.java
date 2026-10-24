package com.github.olympus.proteus.client.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

public final class Hub {
    public static final String PROTEUS = "proteus";

    public record Tab(String id, Supplier<String> title, String icon, Function<UiHost, UiApp> app) {
    }

    private static final List<Tab> TABS = new CopyOnWriteArrayList<>();

    static {
        register(PROTEUS, () -> Component.translatable("proteus.hub.title").getString(), "painting", ProteusUi::new);
    }

    private Hub() {
    }

    public static void register(String id, Supplier<String> title, String icon, Function<UiHost, UiApp> app) {
        TABS.removeIf(tab -> tab.id().equals(id));
        TABS.add(new Tab(id, title, icon, app));
    }

    public static List<Tab> tabs() {
        List<Tab> list = new ArrayList<>(TABS);
        list.sort(Comparator.comparing(tab -> tab.id().equals(PROTEUS)));
        return list;
    }

    public static Function<UiHost, UiApp> app(String id) {
        return host -> new HubApp(host, tabs(), id);
    }
}
