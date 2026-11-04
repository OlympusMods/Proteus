package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.RawInput;
import com.github.olympus.proteus.client.ui.RawKey;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.UiHost;
import com.github.olympus.proteus.client.ui.UiShell;
import com.github.olympus.proteus.client.ui.VoiceIcons;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import de.maxhenkel.voicechat.gui.group.GroupScreen;
import de.maxhenkel.voicechat.gui.group.JoinGroupScreen;
import de.maxhenkel.voicechat.gui.tooltips.MuteTooltipSupplier;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumesScreen;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.voice.client.AudioRecorder;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import de.maxhenkel.voicechat.voice.common.ClientGroup;

final class SvcMenuApp extends UiShell implements RawInput {
    private static final int PAD = 10;
    private static final int GAP = 4;
    private static final int ROW = 20;

    private final ClientPlayerStateManager state = ClientManager.getPlayerStateManager();

    SvcMenuApp(UiHost host) {
        super(host);
        VoiceIcons.register();
    }

    @Override
    protected int windowWidth() {
        return Math.min(width() - 16, 230);
    }

    @Override
    protected int windowHeight() {
        return Math.min(height() - top() - 12, 2 * PAD + 22 + ROW + 8 + ROW + 12);
    }

    @Override
    protected void renderContent(Theme theme) {
        ClientVoicechat client = ClientManager.getClient();
        int pw = windowWidth();
        int ph = windowHeight();
        int px = windowX(pw);
        int py = windowY(ph);
        theme.window(fade(), px, py, pw, ph);

        int ix = px + PAD;
        int iw = pw - 2 * PAD;
        int y = py + PAD;

        gui().centered(Svc.text("gui.voicechat.voice_chat.title"), px + pw / 2, y + 5, theme.titleColor());
        y += 22;

        int half = (iw - GAP) / 2;
        if (gui().button("svc-settings", ix, y, half, ROW, Svc.text("message.voicechat.settings"), null)) {
            Svc.open(new VoiceChatSettingsScreen());
        }
        boolean groups = client != null && client.getConnection() != null
                && client.getConnection().getData().groupsEnabled();
        if (gui().button("svc-group", ix + half + GAP, y, half, ROW, Svc.text("message.voicechat.group"),
                ButtonKind.NORMAL, false, groups, null)) {
            ClientGroup group = state.getGroup();
            Svc.open(group != null ? new GroupScreen(group) : new JoinGroupScreen());
        }

        int by = py + ph - PAD - ROW;
        int x = ix;
        boolean muteTip = MuteTooltipSupplier.canMuteMic();
        String muteText = !muteTip ? "message.voicechat.mute.disabled_ptt"
                : state.isMuted() ? "message.voicechat.mute.enabled" : "message.voicechat.mute.disabled";
        if (gui().iconToggle("svc-mute", x, by, ROW, ROW, state.isMuted() ? VoiceIcons.MIC_OFF : VoiceIcons.MIC,
                state.isMuted(), true, Svc.text(muteText))) {
            state.setMuted(!state.isMuted());
        }
        x += ROW + GAP;
        String disableText = !state.canEnable() ? "message.voicechat.disable.speaker_unavailable"
                : state.isDisabled() ? "message.voicechat.disable.enabled" : "message.voicechat.disable.disabled";
        if (gui().iconToggle("svc-disable", x, by, ROW, ROW,
                state.isDisabled() ? VoiceIcons.SPEAKER_OFF : VoiceIcons.SPEAKER, state.isDisabled(),
                state.canEnable(), Svc.text(disableText))) {
            state.setDisabled(!state.isDisabled());
        }
        x += ROW + GAP;
        if (gui().iconToggle("svc-volumes", x, by, ROW, ROW, VoiceIcons.SLIDERS, false, true,
                Svc.text("message.voicechat.adjust_volumes"))) {
            Svc.open(new AdjustVolumesScreen());
        }

        int rx = ix + iw - ROW;
        boolean hidden = VoicechatClient.CLIENT_CONFIG.hideIcons.get();
        if (gui().iconToggle("svc-hide", rx, by, ROW, ROW, hidden ? VoiceIcons.EYE_OFF : VoiceIcons.EYE, hidden, true,
                Svc.text(hidden ? "message.voicechat.hide_icons.enabled" : "message.voicechat.hide_icons.disabled"))) {
            VoicechatClient.CLIENT_CONFIG.hideIcons.set(!hidden).save();
        }
        AudioRecorder recorder = client == null ? null : client.getRecorder();
        boolean canRecord = client != null && VoicechatClient.CLIENT_CONFIG.useNatives.get()
                && (recorder != null || (client.getConnection() != null
                && client.getConnection().getData().allowRecording()));
        if (canRecord) {
            rx -= ROW + GAP;
            if (gui().iconToggle("svc-record", rx, by, ROW, ROW, VoiceIcons.RECORD, recorder != null, true,
                    Svc.text(recorder != null ? "message.voicechat.recording.enabled"
                            : "message.voicechat.recording.disabled"))) {
                client.toggleRecording();
            }
        }
        if (recorder != null) {
            int left = x + ROW + GAP;
            int width = rx - GAP - left;
            String time = recorder.getDuration();
            gui().centered(time, left + width / 2, gui().textY(by, ROW), theme.danger());
            if (gui().over(left, by, width, ROW)) {
                gui().tooltip(Svc.text("message.voicechat.storage_size", recorder.getStorage()));
            }
        }
    }

    @Override
    public boolean rawKey(RawKey key) {
        if (KeyEvents.KEY_VOICE_CHAT != null
                && ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_VOICE_CHAT).getValue()
                == key.code()) {
            requestClose();
            return true;
        }
        return false;
    }
}
