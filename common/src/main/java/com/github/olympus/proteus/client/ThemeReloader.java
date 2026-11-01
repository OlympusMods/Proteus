package com.github.olympus.proteus.client;

import com.github.olympus.proteus.client.ui.ThemeRegistry;
import com.github.olympus.proteus.client.ui.Themes;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public final class ThemeReloader implements ResourceManagerReloadListener {
    private static final String FOLDER = "proteus/themes";

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        Consumer<String> warn = message -> ProteusClient.LOGGER.warn("Theme {}", message);
        Map<String, String> sources = new HashMap<>();
        Map<Identifier, Resource> found = manager.listResources(FOLDER,
                location -> location.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : found.entrySet()) {
            Identifier location = entry.getKey();
            String path = location.getPath();
            String name = path.substring(FOLDER.length() + 1, path.length() - ".json".length());
            String id = location.getNamespace() + ":" + name;
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                sources.put(id, reader.lines().collect(Collectors.joining("\n")));
            } catch (IOException exception) {
                warn.accept(location + ": " + exception.getMessage());
            }
        }
        Themes.reload(ThemeRegistry.parseAll(sources, warn), warn);
        GlassRenderer.reload();
        ProteusClient.LOGGER.info("Loaded {} themes", Themes.all().size());
    }
}
