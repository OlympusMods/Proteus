package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.RawInput;
import com.github.olympus.proteus.client.ui.RawKey;
import com.github.olympus.proteus.client.ui.RawMouse;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.UiHost;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.audiodevice.SelectMicrophoneScreen;
import de.maxhenkel.voicechat.gui.audiodevice.SelectSpeakerScreen;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.client.speaker.AudioType;
import net.minecraft.client.gui.screens.Screen;

final class SvcSettingsApp extends SvcPanel implements RawInput {
    private final SvcMicControls mic = new SvcMicControls();

    private float volume = -1f;
    private boolean volumeDirty;

    SvcSettingsApp(UiHost host, Screen original) {
        super(host, original);
    }

    @Override
    protected String title() {
        return Svc.text("gui.voicechat.voice_chat_settings.title");
    }

    @Override
    protected int windowHeight() {
        return Math.max(176, Math.min(height() - top() - 12, 268));
    }

    @Override
    protected boolean onEscape() {
        return mic.listening();
    }

    @Override
    protected void body(Theme theme, int ix, int cy, int iw, int ch) {
        mic.frame();
        float scroll = gui().scrollBegin("svc-settings-scroll", ix, cy, iw, ch);
        ix += 4;
        int lw = iw - 8;
        int top = cy - Math.round(scroll);
        int y = top;

        var config = VoicechatClient.CLIENT_CONFIG;
        float maxVolume = maxVolume();
        if (volume < 0f) {
            volume = config.voiceChatVolume.get().floatValue() / maxVolume;
        }
        String volumeLabel = Svc.text("message.voicechat.voice_chat_volume", Math.round(volume * maxVolume * 100f) + "%");
        float changed = gui().valueSlider("svc-volume", ix, y, lw, ROW, volumeLabel, volume, 0f, true, null);
        if (changed != volume) {
            volume = changed;
            volumeDirty = true;
        }
        y += ROW + GAP;

        y = mic.gain(gui(), ix, y, lw);
        y = mic.denoiser(gui(), ix, y, lw);
        y = mic.activation(gui(), ix, y, lw);
        y = mic.level(gui(), ix, y, lw);

        MicrophoneActivationType type = config.microphoneActivationType.get();
        if (type == MicrophoneActivationType.PTT) {
            y = mic.keybind(gui(), ix, y, lw);
        } else if (type == MicrophoneActivationType.VOICE) {
            y = mic.vad(gui(), ix, y, lw);
            y = mic.threshold(gui(), ix, y, lw);
        }

        AudioType audio = config.audioType.get();
        if (gui().button("svc-audio-type", ix, y, lw, ROW, Svc.text("message.voicechat.audio_type", audio.getText()),
                null)) {
            AudioType[] values = AudioType.values();
            config.audioType.set(values[(audio.ordinal() + 1) % values.length]).save();
            mic.stopTest();
            ClientVoicechat client = ClientManager.getClient();
            if (client != null) {
                client.reloadAudio();
            }
        }
        y += ROW + GAP;

        int half = (lw - GAP) / 2;
        if (gui().button("svc-microphone", ix, y, half, ROW, Svc.text("message.voicechat.select_microphone"), null)) {
            Svc.open(new SelectMicrophoneScreen(original()));
        }
        if (gui().button("svc-speaker", ix + half + GAP, y, half, ROW, Svc.text("message.voicechat.select_speaker"),
                null)) {
            Svc.open(new SelectSpeakerScreen(original()));
        }
        y += ROW + GAP;

        gui().scrollEnd(y - top);
    }

    @Override
    protected void afterFrame() {
        if (!gui().dragging()) {
            applyPending();
        }
    }

    private void applyPending() {
        mic.applyPending();
        if (volumeDirty) {
            volumeDirty = false;
            VoicechatClient.CLIENT_CONFIG.voiceChatVolume.set((double) (volume * maxVolume())).save();
        }
    }

    private static float maxVolume() {
        float max = VoicechatClient.CLIENT_CONFIG.voiceChatVolume.getMax().floatValue();
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return max;
        }
        SoundManager sounds = client.getSoundManager();
        return sounds == null ? max : Math.min(max, sounds.getMaxGain());
    }

    @Override
    public boolean rawKey(RawKey key) {
        return mic.key(key);
    }

    @Override
    public boolean rawMouse(RawMouse mouse) {
        return mic.mouse(mouse);
    }

    @Override
    public void removed() {
        applyPending();
        mic.stop();
    }

    @Override
    public void closed() {
        mic.stopTest();
    }
}
