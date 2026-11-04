package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.UiHost;
import com.github.olympus.proteus.client.ui.VoiceIcons;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumeSlider.AdjustVolumeEntry;
import de.maxhenkel.voicechat.gui.volume.PlayerVolumeEntry.AdjustPlayerVolumeEntry;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.client.gui.screens.Screen;

final class SvcVolumesApp extends SvcPanel {
    private static final int CELL = 28;
    private static final int HEAD = 20;
    private static final int SLIDER = 140;
    private static final UUID NIL = new UUID(0L, 0L);

    private record Entry(String id, String name, UUID head, AdjustVolumeEntry source, String tip) {
    }

    private final SvcVolumes volumes = new SvcVolumes();
    private String search = "";

    SvcVolumesApp(UiHost host, Screen original) {
        super(host, original);
    }

    @Override
    protected int wide() {
        return 340;
    }

    @Override
    protected String title() {
        return Svc.text("gui.voicechat.adjust_volume.title");
    }

    @Override
    protected void body(Theme theme, int x, int y, int w, int h) {
        search = gui().field("svc-volume-search", x, y, w, ROW, Svc.text("message.voicechat.search_hint"), 16);
        y += ROW + GAP;
        h -= ROW + GAP;

        List<Entry> entries = entries(search.toLowerCase(Locale.ROOT));
        if (entries.isEmpty()) {
            gui().centered(Svc.text("message.voicechat.search_empty"), x + w / 2, y + h / 2 - 4, theme.textDisabled());
            return;
        }

        float scroll = gui().scrollBegin("svc-volumes", x, y, w, h);
        int lw = w - 8;
        int top = y - Math.round(scroll);
        int ry = top;
        for (Entry entry : entries) {
            gui().rowBackground("svc-vrow-" + entry.id(), x, ry, lw, CELL, false);
            int hx = x + 6;
            int hy = ry + (CELL - HEAD) / 2;
            if (entry.head() != null && !NIL.equals(entry.head())) {
                gui().canvas().head(entry.head(), hx, hy, HEAD);
            } else {
                theme.icon(gui().canvas(), VoiceIcons.SLIDERS, hx + HEAD / 2, hy + HEAD / 2, theme.textSecondary());
            }
            int nameX = hx + HEAD + 8;
            gui().text(gui().fit(entry.name(), lw - (nameX - x) - SLIDER - 14), nameX, gui().textY(ry, CELL),
                    theme.textPrimary());
            if (entry.tip() != null && gui().over(x, ry, lw - SLIDER - 12, CELL)) {
                gui().tooltip(entry.tip());
            }
            volumes.slider(gui(), entry.id(), x + lw - 6 - SLIDER, ry + (CELL - ROW) / 2, SLIDER, ROW, entry.source());
            ry += CELL + 2;
        }
        gui().scrollEnd(ry - top);
    }

    private List<Entry> entries(String filter) {
        List<Entry> categories = new ArrayList<>();
        for (VolumeCategoryImpl category : ClientManager.getCategoryManager().getCategories()) {
            if (!filter.isEmpty() && !category.getSearchName().toLowerCase(Locale.ROOT).contains(filter)) {
                continue;
            }
            String tip = category.getDescription() == null ? null : category.getDisplayDescription().getString();
            categories.add(new Entry("c:" + category.getId(), category.getDisplayName().getString(), null,
                    new CategoryEntry(category.getId()), tip));
        }
        categories.sort(Comparator.comparing(entry -> entry.name().toLowerCase(Locale.ROOT)));

        Collection<PlayerState> online = ClientManager.getPlayerStateManager().getPlayerStates(false);
        List<PlayerState> players = new ArrayList<>(online);
        if (VoicechatClient.CLIENT_CONFIG.offlinePlayerVolumeAdjustment.get()) {
            for (UUID uuid : VoicechatClient.PLAYER_VOLUME_CONFIG.getVolumes().keySet()) {
                if (uuid.equals(NIL) || online.stream().anyMatch(state -> uuid.equals(state.getUuid()))) {
                    continue;
                }
                String name = VoicechatClient.USERNAME_CACHE.getUsername(uuid);
                if (name != null) {
                    players.add(new PlayerState(uuid, name, false, true));
                }
            }
        }
        List<Entry> people = new ArrayList<>();
        for (PlayerState player : players) {
            if (!filter.isEmpty() && !player.getName().toLowerCase(Locale.ROOT).contains(filter)) {
                continue;
            }
            people.add(new Entry("p:" + player.getUuid(), player.getName(), player.getUuid(),
                    new AdjustPlayerVolumeEntry(player.getUuid(), player.getName()), null));
        }
        people.sort(Comparator.comparing(entry -> entry.name().toLowerCase(Locale.ROOT)));

        List<Entry> all = new ArrayList<>();
        if (filter.isEmpty()) {
            all.add(new Entry("p:other", Svc.text("message.voicechat.other_volume"), NIL,
                    new AdjustPlayerVolumeEntry(NIL, null), Svc.text("message.voicechat.other_volume.description")));
        }
        all.addAll(categories);
        all.addAll(people);
        return all;
    }

    @Override
    protected void afterFrame() {
        if (!gui().dragging()) {
            volumes.apply();
        }
    }

    @Override
    public void removed() {
        volumes.apply();
    }

    private record CategoryEntry(String category) implements AdjustVolumeEntry {
        @Override
        public void save(double value) {
            VoicechatClient.CATEGORY_VOLUME_CONFIG.setVolume(category, value);
            VoicechatClient.CATEGORY_VOLUME_CONFIG.save();
        }

        @Override
        public double get() {
            return VoicechatClient.CATEGORY_VOLUME_CONFIG.getVolume(category);
        }

        @Override
        public double getAudioLevel() {
            ClientVoicechat client = ClientManager.getClient();
            return client == null ? AudioUtils.LOWEST_DB : client.getTalkCache().getCategoryAudioLevel(category);
        }
    }
}
