package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.Draw;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.UiHost;
import com.github.olympus.proteus.client.ui.VoiceIcons;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.gui.GroupType;
import de.maxhenkel.voicechat.gui.group.JoinGroupScreen;
import de.maxhenkel.voicechat.gui.tooltips.MuteTooltipSupplier;
import de.maxhenkel.voicechat.gui.volume.PlayerVolumeEntry.AdjustPlayerVolumeEntry;
import de.maxhenkel.voicechat.net.ClientServerNetManager;
import de.maxhenkel.voicechat.net.LeaveGroupPacket;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.screens.Screen;

final class SvcGroupApp extends SvcPanel {
    private static final int CELL = 28;
    private static final int HEAD = 20;
    private static final int SLIDER = 140;

    private final ClientPlayerStateManager state = ClientManager.getPlayerStateManager();
    private final SvcVolumes volumes = new SvcVolumes();

    SvcGroupApp(UiHost host, Screen original) {
        super(host, original);
    }

    @Override
    protected int wide() {
        return 340;
    }

    @Override
    protected String title() {
        ClientGroup group = state.getGroup();
        if (group == null) {
            return "";
        }
        if (group.getType().equals(Group.Type.NORMAL)) {
            return Svc.text("message.voicechat.group_title", group.getName());
        }
        return Svc.text("message.voicechat.group_type_title", group.getName(),
                GroupType.fromType(group.getType()).getTranslation().getString());
    }

    @Override
    protected void onTick() {
        if (state.getGroupID() == null) {
            Svc.open(null);
        }
    }

    @Override
    protected void body(Theme theme, int x, int y, int w, int h) {
        UUID group = state.getGroupID();
        List<PlayerState> members = new ArrayList<>();
        if (group != null) {
            for (PlayerState player : state.getPlayerStates(true)) {
                if (player.hasGroup() && player.getGroup().equals(group)) {
                    members.add(player);
                }
            }
        }
        members.sort(Comparator.comparing(PlayerState::getName));

        ClientVoicechat client = ClientManager.getClient();
        float scroll = gui().scrollBegin("svc-members", x, y, w, h);
        int lw = w - 8;
        int top = y - Math.round(scroll);
        int ry = top;
        for (PlayerState member : members) {
            String id = member.getUuid().toString();
            boolean self = member.getUuid().equals(state.getOwnID());
            gui().rowBackground("svc-member-" + id, x, ry, lw, CELL, false);
            int hx = x + 6;
            int hy = ry + (CELL - HEAD) / 2;
            if (!member.isDisabled() && client != null && client.getTalkCache().isTalking(member.getUuid())) {
                Draw.ring(gui().canvas(), hx - 2, hy - 2, HEAD + 4, HEAD + 4, 2, theme.accentColor());
            }
            gui().canvas().head(member.getUuid(), hx, hy, HEAD);
            int nameX = hx + HEAD + 8;
            int nameW = lw - (nameX - x) - (self ? 8 : SLIDER + 14);
            gui().text(gui().fit(member.getName(), nameW), nameX, gui().textY(ry, CELL), theme.textPrimary());
            if (member.isDisabled()) {
                theme.icon(gui().canvas(), VoiceIcons.SPEAKER_OFF, hx + HEAD - 4, hy + HEAD - 4, theme.danger());
            }
            if (!self) {
                volumes.slider(gui(), id, x + lw - 6 - SLIDER, ry + (CELL - ROW) / 2, SLIDER, ROW,
                        new AdjustPlayerVolumeEntry(member.getUuid(), member.getName()));
            }
            ry += CELL + 2;
        }
        gui().scrollEnd(ry - top);
    }

    @Override
    protected void footer(Theme theme, int x, int y, int w) {
        int left = x;
        boolean voice = MuteTooltipSupplier.canMuteMic();
        String muteText = !voice ? "message.voicechat.mute.disabled_ptt"
                : state.isMuted() ? "message.voicechat.mute.enabled" : "message.voicechat.mute.disabled";
        if (gui().iconToggle("svc-g-mute", x, y, ROW, ROW, state.isMuted() ? VoiceIcons.MIC_OFF : VoiceIcons.MIC,
                state.isMuted(), voice, Svc.text(muteText))) {
            state.setMuted(!state.isMuted());
        }
        x += ROW + GAP;
        String disableText = !state.canEnable() ? "message.voicechat.disable.speaker_unavailable"
                : state.isDisabled() ? "message.voicechat.disable.enabled" : "message.voicechat.disable.disabled";
        if (gui().iconToggle("svc-g-disable", x, y, ROW, ROW,
                state.isDisabled() ? VoiceIcons.SPEAKER_OFF : VoiceIcons.SPEAKER, state.isDisabled(), state.canEnable(),
                Svc.text(disableText))) {
            state.setDisabled(!state.isDisabled());
        }
        x += ROW + GAP;
        boolean show = VoicechatClient.CLIENT_CONFIG.showGroupHud.get();
        boolean hudAvailable = !VoicechatClient.CLIENT_CONFIG.hideIcons.get();
        if (gui().iconToggle("svc-g-hud", x, y, ROW, ROW, VoiceIcons.GROUP, show, hudAvailable,
                Svc.text(show ? "message.voicechat.show_group_hud.enabled"
                        : "message.voicechat.show_group_hud.disabled"))) {
            VoicechatClient.CLIENT_CONFIG.showGroupHud.set(!show).save();
        }
        if (gui().iconToggle("svc-g-leave", left + w - ROW, y, ROW, ROW, VoiceIcons.LEAVE, false,
                true, Svc.text("message.voicechat.leave_group"))) {
            ClientServerNetManager.sendToServer(new LeaveGroupPacket());
            Svc.open(new JoinGroupScreen());
        }
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
}
