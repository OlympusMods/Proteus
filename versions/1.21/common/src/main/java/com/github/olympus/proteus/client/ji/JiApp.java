package com.github.olympus.proteus.client.ji;

import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.Draw;
import com.github.olympus.proteus.client.ui.RawInput;
import com.github.olympus.proteus.client.ui.RawKey;
import com.github.olympus.proteus.client.ui.RawMouse;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.UiHost;
import com.github.olympus.proteus.client.ui.UiShell;
import com.google.gson.Gson;
import com.ji.afkcinematic.config.CinematicChatVisibility;
import com.ji.afkcinematic.config.ConfigManager;
import com.ji.afkcinematic.config.DamageAction;
import com.ji.afkcinematic.config.ModConfig;
import com.ji.afkcinematic.config.SleepLetterboxMode;
import com.ji.afkcinematic.input.RuntimeShortcuts;
import com.ji.afkcinematic.music.CinematicMusicManager;
import com.ji.afkcinematic.music.LocalMusicPackManager;
import java.util.Objects;
import java.util.function.Consumer;

final class JiApp extends UiShell implements RawInput, JiKeyListener {
    private enum Page {
        MAIN,
        MUSIC,
        FOLDER
    }

    private static final Gson GSON = new Gson();
    private static final int PAD = 10;
    private static final int GAP = 4;
    private static final int ROW = 20;
    private static final int NOTE_TICKS = 60;

    private final JiKeys keys = new JiKeys();
    private final JiMusic music;

    private ModConfig draft;
    private Page page = Page.MAIN;
    private boolean awaiting;
    private boolean saving;
    private String note;
    private int noteTicks;

    JiApp(UiHost host) {
        super(host);
        this.draft = copy(ConfigManager.getConfig());
        this.awaiting = ConfigManager.isLoading();
        this.music = new JiMusic(() -> draft, this::fade);
    }

    private static ModConfig copy(ModConfig source) {
        return GSON.fromJson(GSON.toJson(source), ModConfig.class);
    }

    private boolean enabled() {
        return !awaiting && !saving;
    }

    private String title() {
        return switch (page) {
            case MAIN -> Ji.text("title");
            case MUSIC -> Ji.text("music_manager_title");
            case FOLDER -> Ji.text("folder_path");
        };
    }

    @Override
    protected int windowWidth() {
        return Math.max(260, Math.min(width() - 16, 330));
    }

    @Override
    protected int windowHeight() {
        return Math.max(176, Math.min(height() - top() - 12, 320));
    }

    @Override
    public void resize(int width, int height) {
        music.enter();
        super.resize(width, height);
    }

    @Override
    protected void onTick() {
        if (awaiting && !ConfigManager.isLoading()) {
            awaiting = false;
            draft = copy(ConfigManager.getConfig());
        }
        keys.tick();
        if (noteTicks > 0 && --noteTicks == 0) {
            note = null;
        }
    }

    @Override
    protected boolean onEscape() {
        if (saving) {
            return true;
        }
        if (page == Page.FOLDER) {
            page = Page.MUSIC;
            return true;
        }
        if (page == Page.MUSIC) {
            page = Page.MAIN;
            return true;
        }
        return false;
    }

    @Override
    public void requestClose() {
        if (!saving) {
            super.requestClose();
        }
    }

    @Override
    public boolean rawKey(RawKey key) {
        return keys.press(key.code(), key.escape(), draft);
    }

    @Override
    public boolean rawMouse(RawMouse mouse) {
        if (keys.listening()) {
            keys.cancel(draft);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int key) {
        return keys.release(key, draft);
    }

    @Override
    public void removed() {
        keys.cancel(draft);
        music.leave();
    }

    @Override
    protected void renderContent(Theme theme) {
        int pw = windowWidth();
        int ph = windowHeight();
        int px = windowX(pw);
        int py = windowY(ph);
        theme.window(fade(), px, py, pw, ph);

        int ix = px + PAD;
        int iw = pw - 2 * PAD;
        int y = py + PAD;
        gui().centered(gui().fit(title(), iw), px + pw / 2, y + 5, theme.titleColor());
        y += 26;

        boolean footer = page != Page.FOLDER;
        int footerY = py + ph - PAD - ROW;
        int bodyHeight = (footer ? footerY - GAP : py + ph - PAD) - y;

        float before = beginContent();
        String id = "ji-scroll-" + page.name().toLowerCase();
        float scroll = gui().scrollBegin(id, ix, y, iw, bodyHeight);
        int x = ix + 4;
        int w = iw - 12;
        int top = y - Math.round(scroll);
        int end = switch (page) {
            case MAIN -> main(theme, x, top, w);
            case MUSIC -> music.body(gui(), theme, x, top, w, () -> page = Page.FOLDER);
            case FOLDER -> music.folder(gui(), theme, x, top, w, () -> page = Page.MUSIC);
        };
        gui().scrollEnd(end - top);
        endContent(before);

        if (footer) {
            footer(theme, ix, footerY, iw);
        }
    }

    private void footer(Theme theme, int x, int y, int w) {
        if (page == Page.MUSIC) {
            if (gui().button("ji-music-done", x, y, w, ROW, Ji.proteus("proteus.ui.done"), ButtonKind.PRIMARY, false,
                    true, null)) {
                page = Page.MAIN;
            }
            return;
        }
        int third = (w - 2 * GAP) / 3;
        if (gui().button("ji-reset", x, y, third, ROW, Ji.text("reset_defaults"), ButtonKind.NORMAL, false, enabled(),
                null)) {
            keys.stop();
            draft = new ModConfig();
            draft.recalculate();
            music.prepareFolder();
        }
        boolean writable = !ConfigManager.isWriteProtected();
        if (gui().button("ji-save", x + third + GAP, y, third, ROW, saveLabel(), ButtonKind.PRIMARY, false,
                enabled() && writable, saveTip(writable))) {
            save();
        }
        if (gui().button("ji-cancel", x + 2 * (third + GAP), y, third, ROW, Ji.text("cancel"), ButtonKind.NORMAL, false,
                !saving, null)) {
            requestClose();
        }
    }

    private String saveLabel() {
        if (saving) {
            return Ji.text("saving");
        }
        if (ConfigManager.isWriteProtected()) {
            return Ji.text("save_protected");
        }
        return note != null ? note : Ji.text(width() < 380 ? "save_short" : "save");
    }

    private String saveTip(boolean writable) {
        if (!writable) {
            return Ji.tip("protected_notice");
        }
        return note != null ? Ji.tip("shortcut_conflict") : null;
    }

    private void say(String text) {
        note = text;
        noteTicks = NOTE_TICKS;
    }

    private void save() {
        if (saving || ConfigManager.isLoading()) {
            return;
        }
        ModConfig current = ConfigManager.getConfig();
        if (!RuntimeShortcuts.conflicts(draft).isEmpty()
                && !RuntimeShortcuts.bindings(draft).equals(RuntimeShortcuts.bindings(current))) {
            say(Ji.text("shortcut_conflict"));
            return;
        }
        boolean directoryChanged = !Objects.equals(current.customMusicDirectory, draft.customMusicDirectory);
        boolean musicChanged = current.musicMode != draft.musicMode || current.enableMusic != draft.enableMusic
                || current.extendedMusic != draft.extendedMusic || directoryChanged;
        draft.recalculate();
        saving = true;
        ConfigManager.trySetConfigAsync(draft).thenAccept(success -> {
            saving = false;
            if (!success) {
                say(Ji.text("save_failed"));
                return;
            }
            if (!directoryChanged) {
                if (musicChanged) {
                    CinematicMusicManager.onSettingsChanged();
                }
                requestClose();
                return;
            }
            saving = true;
            LocalMusicPackManager.rebuildAndReloadAsync(count -> {
                if (count >= 0) {
                    saving = false;
                    CinematicMusicManager.onSettingsChanged();
                    requestClose();
                    return;
                }
                ConfigManager.trySetConfigAsync(current).thenAccept(restored -> {
                    saving = false;
                    say(Ji.text("refresh_music_failed"));
                });
            });
        });
    }

    private int main(Theme theme, int x, int y, int w) {
        if (ConfigManager.isWriteProtected()) {
            for (String line : Draw.wrap(fade(), Ji.text("protected_notice"), w)) {
                gui().text(line, x + 2, y, theme.danger());
                y += 10;
            }
            y += GAP;
        }

        y = heading(Ji.text("category.general"), x, y);
        y = toggle("ji-enabled", x, y, w, Ji.text("enabled"), draft.modEnabled, Ji.tip("enabled"),
                () -> draft.modEnabled = !draft.modEnabled);
        y = cycle("ji-damage", x, y, w,
                Ji.text("damage_action") + ": " + Ji.text("damage_action." + draft.damageAction.name().toLowerCase()),
                Ji.tip("damage_action"),
                () -> draft.damageAction = DamageAction.values()[(draft.damageAction.ordinal() + 1)
                        % DamageAction.values().length]);
        y = toggle("ji-chat", x, y, w, Ji.text("chat_visibility"),
                draft.chatVisibility == CinematicChatVisibility.VISIBLE,
                Ji.tip("chat_visibility"), () -> draft.chatVisibility = draft.chatVisibility.next());

        y = heading(Ji.text("category.afk"), x, y + GAP);
        y = slider("ji-afk", x, y, w, Ji.text("afk_threshold", draft.afkThresholdSeconds), Ji.tip("afk_threshold"),
                (draft.afkThresholdSeconds - 10f) / 590f, value -> draft.afkThresholdSeconds = 10 + Math.round(value * 590f));
        int cycles = draft.isUnlimitedCycles() ? 20 : draft.maxCycles - 1;
        Object cyclesLabel = draft.isUnlimitedCycles() ? Ji.text("unlimited") : Integer.toString(draft.maxCycles);
        y = slider("ji-cycles", x, y, w, Ji.text("max_cycles", cyclesLabel), Ji.tip("max_cycles"), cycles / 20f, value -> {
            int index = Math.round(value * 20f);
            draft.maxCycles = index == 20 ? ModConfig.UNLIMITED_CYCLES : index + 1;
        });
        y = cycle("ji-persistent", x, y, w,
                Ji.text("persistent_cinematics") + ": " + Ji.text("persistent_mode." + draft.persistentMode.name().toLowerCase()),
                Ji.tip("persistent_cinematics"), () -> draft.persistentMode = draft.persistentMode.next());

        y = heading(Ji.text("category.camera"), x, y + GAP);
        y = slider("ji-duration", x, y, w, Ji.text("shot_duration", draft.shotDurationSeconds), Ji.tip("shot_duration"),
                (draft.shotDurationSeconds - 5f) / 55f, value -> draft.shotDurationSeconds = 5 + Math.round(value * 55f));
        y = slider("ji-speed", x, y, w, Ji.text("camera_speed", String.format("%.1f", draft.cameraSpeed)),
                Ji.tip("camera_speed"), (draft.cameraSpeed - 0.1f) / 2.9f, value -> draft.cameraSpeed = 0.1f + value * 2.9f);
        y = slider("ji-mix", x, y, w,
                Ji.text("shot_mix", draft.characterShotPercentage, 100 - draft.characterShotPercentage), Ji.tip("shot_mix"),
                draft.characterShotPercentage / 100f, value -> draft.characterShotPercentage = Math.round(value * 10f) * 10);
        y = toggle("ji-rotation", x, y, w, Ji.text("camera_rotation"), draft.cameraRotationEnabled,
                Ji.tip("camera_rotation"), () -> draft.cameraRotationEnabled = !draft.cameraRotationEnabled);
        y = toggle("ji-letterbox", x, y, w, Ji.text("letterbox"), draft.enableLetterbox, Ji.tip("letterbox"),
                () -> draft.enableLetterbox = !draft.enableLetterbox);

        y = heading(Ji.text("category.sleep"), x, y + GAP);
        y = toggle("ji-sleep", x, y, w, Ji.text("sleep_letterbox"),
                draft.sleepLetterboxMode != SleepLetterboxMode.DISABLED,
                Ji.tip("sleep_letterbox"), () -> draft.sleepLetterboxMode = draft.sleepLetterboxMode.next());
        y = toggle("ji-fishing", x, y, w, Ji.text("fishing_cinematic"), draft.fishingCinematicEnabled,
                Ji.tip("fishing_cinematic"), () -> draft.fishingCinematicEnabled = !draft.fishingCinematicEnabled);
        if (draft.fishingCinematicEnabled) {
            y = slider("ji-fishing-delay", x, y, w, Ji.text("fishing_threshold", draft.fishingCinematicThresholdSeconds),
                    Ji.tip("fishing_threshold"), draft.fishingCinematicThresholdSeconds / 120f,
                    value -> draft.fishingCinematicThresholdSeconds = Math.round(value * 120f));
        }

        y = heading(Ji.text("category.music"), x, y + GAP);
        y = toggle("ji-music", x, y, w, Ji.text("music"), draft.enableMusic, Ji.tip("music"),
                () -> draft.enableMusic = !draft.enableMusic);
        y = toggle("ji-extended", x, y, w, Ji.text("extended_music"), draft.extendedMusic, Ji.tip("extended_music"),
                () -> draft.extendedMusic = !draft.extendedMusic);
        y = cycle("ji-mode", x, y, w,
                Ji.text("music_mode") + ": " + Ji.text("music_mode." + draft.musicMode.name().toLowerCase()),
                Ji.tip("music_mode"), () -> draft.musicMode = draft.musicMode.next());
        y = slider("ji-volume", x, y, w, Ji.text("music_volume", (int) (draft.cinematicMusicVolume * 100)),
                Ji.tip("music_volume"), draft.cinematicMusicVolume, value -> draft.cinematicMusicVolume = value);
        if (draft.musicMode.includesCustom()) {
            int half = (w - GAP) / 2;
            if (gui().button("ji-manage", x, y, half, ROW, Ji.text("manage_music"), ButtonKind.NORMAL, false, enabled(),
                    Ji.tip("manage_music"))) {
                page = Page.MUSIC;
            }
            if (gui().button("ji-refresh", x + half + GAP, y, half, ROW, Ji.text("refresh_music"), ButtonKind.NORMAL, false,
                    enabled() && !music.busy(), Ji.tip("refresh_music"))) {
                music.refresh();
            }
            y += ROW + GAP;
            if (!music.status().isEmpty()) {
                for (String line : Draw.wrap(fade(), music.status(), w)) {
                    gui().text(line, x + 2, y, theme.textSecondary());
                    y += 10;
                }
                y += GAP;
            }
        }

        y = heading(Ji.text("category.controls"), x, y + GAP);
        for (JiKeys.Slot slot : JiKeys.Slot.values()) {
            if (gui().button("ji-key-" + slot.name().toLowerCase(), x, y, w, ROW, keys.label(slot, draft),
                    ButtonKind.NORMAL, keys.listening(slot), enabled(), Ji.tip(slot.key))) {
                keys.start(slot, draft);
            }
            y += ROW + GAP;
        }
        return y;
    }

    private int toggle(String id, int x, int y, int w, String label, boolean value, String tip, Runnable flip) {
        if (gui().switchRow(id, x, y, w, ROW, label, value, enabled(), tip, Ji.proteus("proteus.state.on"),
                Ji.proteus("proteus.state.off"))) {
            flip.run();
        }
        return y + ROW + GAP;
    }

    private int cycle(String id, int x, int y, int w, String label, String tip, Runnable next) {
        if (gui().button(id, x, y, w, ROW, label, ButtonKind.NORMAL, false, enabled(), tip)) {
            next.run();
        }
        return y + ROW + GAP;
    }

    private int slider(String id, int x, int y, int w, String label, String tip, float value, Consumer<Float> apply) {
        float next = gui().valueSlider(id, x, y, w, ROW, label, value, 0f, enabled(), tip);
        if (next != value) {
            apply.accept(next);
        }
        return y + ROW + GAP;
    }
}
