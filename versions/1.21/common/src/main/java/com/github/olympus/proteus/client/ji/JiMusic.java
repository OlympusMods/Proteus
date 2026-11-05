package com.github.olympus.proteus.client.ji;

import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.Draw;
import com.github.olympus.proteus.client.ui.FadeCanvas;
import com.github.olympus.proteus.client.ui.Gui;
import com.github.olympus.proteus.client.ui.Theme;
import com.ji.afkcinematic.client.UiWorkScope;
import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.config.ModConfig;
import com.ji.afkcinematic.music.LocalMusicPackManager;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;

final class JiMusic {
    private static final int ROW = 20;
    private static final int GAP = 4;

    private final Supplier<ModConfig> draft;
    private final Supplier<FadeCanvas> canvas;
    private final UiWorkScope work = new UiWorkScope();

    private String status = "";
    private Path lastExport;
    private String exportTip;
    private boolean busy;
    private boolean open;
    private boolean folderLoaded;
    private boolean invalid;

    JiMusic(Supplier<ModConfig> draft, Supplier<FadeCanvas> canvas) {
        this.draft = draft;
        this.canvas = canvas;
    }

    boolean busy() {
        return busy;
    }

    void enter() {
        open = true;
        work.begin();
    }

    void leave() {
        open = false;
        work.begin();
        LocalMusicPackManager.cancelScreenJobs();
        busy = false;
    }

    void status(String value) {
        status = value;
    }

    void prepareFolder() {
        folderLoaded = false;
        invalid = false;
    }

    private boolean accepts(long generation) {
        return open && work.accepts(generation);
    }

    int body(Gui gui, Theme theme, int x, int y, int w, Runnable folderPage) {
        gui.text(Ji.text("music_directory_label"), x + 2, y, theme.textSecondary());
        y += 12;
        String path = LocalMusicPackManager.getMusicFolder(draft.get().customMusicDirectory).toString();
        gui.text(tail(path, w - 4), x + 2, y, theme.textPrimary());
        y += 10 + GAP + 2;

        boolean wide = w >= 260;
        int half = (w - GAP) / 2;
        int column = 0;
        String[] ids = {"open", "change", "path", "refresh", "export", "exports"};
        for (String id : ids) {
            int bx = wide ? x + column * (half + GAP) : x;
            int bw = wide ? half : w;
            boolean enabled = !busy && (!id.equals("export") || lastExport == null && !LocalMusicPackManager.isExportingMusicPack());
            if (gui.button("ji-music-" + id, bx, y, bw, ROW, label(id), ButtonKind.NORMAL, false,
                    enabled, tip(id))) {
                act(id, folderPage);
            }
            column++;
            if (!wide || column == 2) {
                column = 0;
                y += ROW + GAP;
            }
        }
        if (column != 0) {
            y += ROW + GAP;
        }
        if (!status.isEmpty()) {
            for (String line : Draw.wrap(canvas.get(), status, w)) {
                gui.text(line, x + 2, y, theme.textSecondary());
                y += 10;
            }
            y += GAP;
        }
        for (String line : Draw.wrap(canvas.get(), Ji.text("music_draft_notice"), w)) {
            gui.text(line, x + 2, y, theme.textDisabled());
            y += 10;
        }
        return y;
    }

    int folder(Gui gui, Theme theme, int x, int y, int w, Runnable back) {
        gui.text(Ji.text("folder_path"), x + 2, y, theme.textSecondary());
        y += 12;
        if (!folderLoaded) {
            gui.setField("ji-folder", draft.get().customMusicDirectory);
            folderLoaded = true;
        }
        String value = gui.field("ji-folder", x, y, w, ROW, "", 1024);
        if (gui.submitted("ji-folder")) {
            applyFolder(value, back);
        }
        y += ROW + GAP;
        if (invalid) {
            gui.text(Ji.proteus("proteus.ji.invalid_path"), x + 2, y, theme.danger());
            y += 12;
        }
        int half = (w - GAP) / 2;
        if (gui.button("ji-folder-save", x, y, half, ROW, Ji.text("save"), ButtonKind.PRIMARY,
                false, true, null)) {
            applyFolder(value, back);
        }
        if (gui.button("ji-folder-cancel", x + half + GAP, y, half, ROW, Ji.text("cancel"), null)) {
            back.run();
        }
        return y + ROW + GAP;
    }

    private String tail(String text, int width) {
        FadeCanvas target = canvas.get();
        if (target.textWidth(text) <= width) {
            return text;
        }
        String cut = text;
        while (cut.length() > 1 && target.textWidth("..." + cut) > width) {
            cut = cut.substring(1);
        }
        return "..." + cut;
    }

    private void applyFolder(String value, Runnable back) {
        try {
            if (!value.isBlank()) {
                Path.of(value);
            }
        } catch (RuntimeException error) {
            invalid = true;
            return;
        }
        draft.get().customMusicDirectory = value.strip();
        lastExport = null;
        status = "";
        back.run();
    }

    private String label(String id) {
        return switch (id) {
            case "open" -> Ji.text("open_music_folder");
            case "change" -> Ji.text("change_music_folder");
            case "path" -> Ji.text("folder_path");
            case "refresh" -> Ji.text("refresh_music");
            case "export" -> Ji.text("export_music_pack");
            default -> Ji.text("music_view_exports");
        };
    }

    private String tip(String id) {
        return switch (id) {
            case "export" -> exportTip != null ? exportTip : Ji.tip("music_export_reuse");
            case "path" -> LocalMusicPackManager.getMusicFolder(draft.get().customMusicDirectory).toString();
            default -> null;
        };
    }

    private void act(String id, Runnable folderPage) {
        switch (id) {
            case "open" -> LocalMusicPackManager.openMusicFolder(draft.get().customMusicDirectory);
            case "change" -> choose();
            case "path" -> {
                prepareFolder();
                folderPage.run();
            }
            case "refresh" -> refresh();
            case "export" -> export();
            default -> LocalMusicPackManager.openMusicPackExports();
        }
    }

    private void choose() {
        long generation = work.generation();
        busy = true;
        LocalMusicPackManager.chooseMusicFolderAsync(draft.get().customMusicDirectory, () -> accepts(generation), selected -> {
            if (!accepts(generation)) {
                return;
            }
            busy = false;
            if (selected != null) {
                draft.get().customMusicDirectory = selected;
                lastExport = null;
                status = Ji.text("music_draft_notice");
            }
        });
    }

    String status() {
        return status;
    }

    void refresh() {
        if (!Objects.equals(draft.get().customMusicDirectory, ConfigManager.getConfig().customMusicDirectory)) {
            status = Ji.text("music_save_folder");
            return;
        }
        long generation = work.generation();
        busy = true;
        lastExport = null;
        LocalMusicPackManager.rebuildAndReloadAsync(() -> accepts(generation), count -> {
            if (!accepts(generation)) {
                return;
            }
            busy = false;
            status = count < 0 ? Ji.text("refresh_music_failed") : Ji.text("music_loaded", count);
        });
    }

    private void export() {
        if (busy || lastExport != null || LocalMusicPackManager.isExportingMusicPack()) {
            return;
        }
        long generation = work.generation();
        busy = true;
        status = Ji.text("music_export_preparing");
        LocalMusicPackManager.exportMusicPackAsync(draft.get().customMusicDirectory, () -> accepts(generation), result -> {
            if (!accepts(generation)) {
                return;
            }
            busy = false;
            lastExport = result;
            status = Ji.text(result == null ? "music_export_failed_preserved" : "music_pack_exported");
            if (result != null) {
                exportTip = result + "\n" + Ji.tip("music_export_reuse");
            }
        });
    }
}
