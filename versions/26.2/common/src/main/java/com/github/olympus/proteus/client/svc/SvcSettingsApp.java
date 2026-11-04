package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.Motion;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.UiHost;
import com.github.olympus.proteus.client.ui.UiShell;
import com.github.olympus.proteus.client.ui.VoiceIcons;
import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.audiodevice.SelectMicrophoneScreen;
import de.maxhenkel.voicechat.gui.audiodevice.SelectSpeakerScreen;
import de.maxhenkel.voicechat.gui.widgets.MicTestButton;
import de.maxhenkel.voicechat.natives.RNNoiseManager;
import de.maxhenkel.voicechat.natives.SpeexManager;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.client.VolumeManager;
import de.maxhenkel.voicechat.voice.client.speaker.AudioType;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

final class SvcSettingsApp extends UiShell implements MicTestButton.MicListener {
    private static final int PAD = 10;
    private static final int GAP = 4;
    private static final int ROW = 20;
    private static final int LABEL = 90;

    private final Screen original;
    private final MicTestButton test = new MicTestButton(0, 0, false, this);

    private float volume = -1f;
    private float gain = -1f;
    private float threshold = -1f;
    private boolean volumeDirty;
    private boolean gainDirty;
    private boolean thresholdDirty;
    private boolean listening;
    private volatile double micLevel;
    private float shownLevel;
    private long lastLevel = System.nanoTime();

    SvcSettingsApp(UiHost host, Screen original) {
        super(host);
        this.original = original;
        VoiceIcons.register();
    }

    @Override
    protected int windowWidth() {
        return Math.max(260, Math.min(width() - 16, 300));
    }

    @Override
    protected int windowHeight() {
        return Math.max(176, Math.min(height() - top() - 12, 268));
    }

    @Override
    protected boolean onEscape() {
        return listening;
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
        gui().centered(text("gui.voicechat.voice_chat_settings.title"), px + pw / 2, y + 5, theme.titleColor());
        y += 26;

        int footerY = py + ph - PAD - ROW;
        int contentHeight = footerY - GAP - y;

        test.updateLastRender();
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastLevel) / 1_000_000_000f);
        lastLevel = now;
        float live = (float) micLevel;
        shownLevel = test.isMicActive() ? Math.max(live, shownLevel - dt * 1.2f) : 0f;

        float before = beginContent();
        renderRows(theme, ix, y, iw, contentHeight);
        endContent(before);

        if (gui().button("svc-done", ix, footerY, iw, ROW, text("proteus.ui.done"), ButtonKind.PRIMARY, false, true,
                null)) {
            requestClose();
        }

        if (!gui().dragging()) {
            applyPending();
        }
    }

    private void renderRows(Theme theme, int ix, int cy, int iw, int ch) {
        float scroll = gui().scrollBegin("svc-settings-scroll", ix, cy, iw, ch);
        ix += 4;
        int lw = iw - 8;
        int top = cy - Math.round(scroll);
        int y = top;

        var config = VoicechatClient.CLIENT_CONFIG;
        float maxVolume = maxVolume();
        if (volume < 0f) {
            volume = config.voiceChatVolume.get().floatValue() / maxVolume;
            gain = (float) ((config.microphoneGain.get() - VolumeManager.MIN_GAIN)
                    / (VolumeManager.MAX_GAIN - VolumeManager.MIN_GAIN));
            threshold = (float) AudioUtils.dbToPerc(config.voiceActivationThreshold.get());
        }

        String volumeLabel = text("message.voicechat.voice_chat_volume", Math.round(volume * maxVolume * 100f) + "%");
        float changed = gui().valueSlider("svc-volume", ix, y, lw, ROW, volumeLabel, volume, 0f, true, null);
        if (changed != volume) {
            volume = changed;
            volumeDirty = true;
        }
        y += ROW + GAP;

        boolean agcAvailable = SpeexManager.canUseAgc();
        boolean agc = config.agc.get();
        int gainX = ix;
        int gainW = lw;
        if (agcAvailable) {
            int agcW = 80;
            String agcLabel = text("message.voicechat.gain", text(agc ? "message.voicechat.gain.auto"
                    : "message.voicechat.gain.manual"));
            if (gui().button("svc-agc", ix, y, agcW, ROW, agcLabel, ButtonKind.NORMAL, !agc, true,
                    agc ? null : text("message.voicechat.gain.manual.warning"))) {
                config.agc.set(!agc).save();
            }
            gainX = ix + agcW + 1;
            gainW = lw - agcW - 1;
        }
        long gainDb = Math.round(gain * (VolumeManager.MAX_GAIN - VolumeManager.MIN_GAIN) + VolumeManager.MIN_GAIN);
        boolean gainActive = !(agcAvailable && agc);
        changed = gui().valueSlider("svc-gain", gainX, y, gainW, ROW, text("message.voicechat.microphone_gain", gainDb),
                gain, 0f, gainActive, gainDb > 10 && gainActive
                        ? text("message.voicechat.microphone_gain.warning", 10) : null);
        if (changed != gain) {
            gain = changed;
            gainDirty = true;
        }
        y += ROW + GAP;

        boolean denoiser = config.denoiser.get();
        boolean canDenoise = RNNoiseManager.canUseDenoiser();
        if (gui().button("svc-denoiser", ix, y, lw, ROW, text("message.voicechat.denoiser", text(
                denoiser && canDenoise ? "message.voicechat.denoiser.on" : "message.voicechat.denoiser.off")),
                ButtonKind.NORMAL, denoiser && canDenoise, canDenoise, null)) {
            config.denoiser.set(!denoiser).save();
        }
        y += ROW + GAP;

        MicrophoneActivationType type = config.microphoneActivationType.get();
        boolean testReady = test.active;
        if (gui().iconToggle("svc-test", ix, y, ROW, ROW, test.isMicActive() ? VoiceIcons.MIC : VoiceIcons.MIC_OFF,
                test.isMicActive(), testReady,
                text(!testReady ? "message.voicechat.mic_test_unavailable" : test.isMicActive()
                        ? "message.voicechat.mic_test.enabled" : "message.voicechat.mic_test.disabled"))) {
            test.onPress(null);
        }
        if (gui().button("svc-activation", ix + ROW + 1, y, lw - ROW - 1, ROW,
                text("message.voicechat.activation_type", type.getText()), null)) {
            MicrophoneActivationType[] values = MicrophoneActivationType.values();
            config.microphoneActivationType.set(values[(type.ordinal() + 1) % values.length]).save();
            listening = false;
        }
        y += ROW + GAP;

        gui().progress(ix, y, lw, 8, shownLevel);
        y += 8 + GAP;

        if (type == MicrophoneActivationType.PTT) {
            y = renderKeybind(ix, y, lw);
        } else if (type == MicrophoneActivationType.VOICE) {
            boolean vad = config.vad.get();
            if (gui().button("svc-vad", ix, y, lw, ROW, text("message.voicechat.voice_activation_detection",
                    text(vad && canDenoise ? "message.voicechat.voice_activation_detection.auto"
                            : "message.voicechat.voice_activation_detection.manual")),
                    ButtonKind.NORMAL, vad && canDenoise, canDenoise, null)) {
                config.vad.set(!vad).save();
            }
            y += ROW + GAP;
            if (!canDenoise || !vad) {
                long db = Math.round(AudioUtils.percToDb(threshold));
                String tip = threshold >= 1f ? text("message.voicechat.voice_activation.disabled") : null;
                changed = gui().valueSlider("svc-threshold", ix, y, lw, ROW,
                        text("message.voicechat.voice_activation", db), threshold, shownLevel, true, tip);
                if (changed != threshold) {
                    threshold = changed;
                    thresholdDirty = true;
                }
                y += ROW + GAP;
            }
        }

        AudioType audio = config.audioType.get();
        if (gui().button("svc-audio-type", ix, y, lw, ROW, text("message.voicechat.audio_type", audio.getText()),
                null)) {
            AudioType[] values = AudioType.values();
            config.audioType.set(values[(audio.ordinal() + 1) % values.length]).save();
            test.stop();
            ClientVoicechat client = ClientManager.getClient();
            if (client != null) {
                client.reloadAudio();
            }
        }
        y += ROW + GAP;

        int half = (lw - GAP) / 2;
        if (gui().button("svc-microphone", ix, y, half, ROW, text("message.voicechat.select_microphone"), null)) {
            Minecraft.getInstance().gui.setScreen(new SelectMicrophoneScreen(original));
        }
        if (gui().button("svc-speaker", ix + half + GAP, y, half, ROW, text("message.voicechat.select_speaker"),
                null)) {
            Minecraft.getInstance().gui.setScreen(new SelectSpeakerScreen(original));
        }
        y += ROW + GAP;

        gui().scrollEnd(y - top);
    }

    private int renderKeybind(int ix, int y, int lw) {
        KeyMapping mapping = KeyEvents.KEY_PTT;
        Component key = mapping.getTranslatedKeyMessage();
        String shown = listening ? "> " + key.getString() + " <" : key.getString();
        String label = text("message.voicechat.activation_type.ptt") + ": " + shown;
        if (gui().button("svc-ptt", ix, y, lw, ROW, label, ButtonKind.NORMAL, listening, true,
                text("message.voicechat.press_to_reassign_key"))) {
            listening = !listening;
        }
        return y + ROW + GAP;
    }

    private void applyPending() {
        var config = VoicechatClient.CLIENT_CONFIG;
        if (volumeDirty) {
            volumeDirty = false;
            config.voiceChatVolume.set((double) (volume * maxVolume())).save();
        }
        if (gainDirty) {
            gainDirty = false;
            config.microphoneGain.set(gain * (VolumeManager.MAX_GAIN - VolumeManager.MIN_GAIN)
                    + VolumeManager.MIN_GAIN).save();
        }
        if (thresholdDirty) {
            thresholdDirty = false;
            config.voiceActivationThreshold.set(AudioUtils.percToDb(threshold)).save();
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
    public boolean keyPressed(int key, int mods) {
        if (listening) {
            Minecraft minecraft = Minecraft.getInstance();
            KeyEvents.KEY_PTT.setKey(key == GLFW.GLFW_KEY_ESCAPE ? InputConstants.UNKNOWN
                    : InputConstants.Type.KEYSYM.getOrCreate(key));
            KeyMapping.resetMapping();
            minecraft.options.save();
            listening = false;
            return true;
        }
        return super.keyPressed(key, mods);
    }

    @Override
    public void mousePressed(int button) {
        if (listening && button == 1) {
            KeyEvents.KEY_PTT.setKey(InputConstants.Type.MOUSE.getOrCreate(button));
            KeyMapping.resetMapping();
            Minecraft.getInstance().options.save();
            listening = false;
            return;
        }
        super.mousePressed(button);
    }

    @Override
    public void onMicValue(double dB) {
        micLevel = Motion.clamp((float) AudioUtils.dbToPerc(dB), 0f, 1f);
    }

    @Override
    public void onStop() {
        micLevel = 0;
    }

    @Override
    public void removed() {
        applyPending();
        test.stop();
    }

    @Override
    public void closed() {
        test.stop();
    }

    private static String text(String key, Object... arguments) {
        return Component.translatable(key, arguments).getString();
    }
}
