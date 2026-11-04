package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.Gui;
import com.github.olympus.proteus.client.ui.Motion;
import com.github.olympus.proteus.client.ui.VoiceIcons;
import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.MicTestButton;
import de.maxhenkel.voicechat.natives.RNNoiseManager;
import de.maxhenkel.voicechat.natives.SpeexManager;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import de.maxhenkel.voicechat.voice.client.VolumeManager;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class SvcMicControls implements MicTestButton.MicListener {
    private static final int GAP = 4;
    private static final int ROW = 20;

    private final MicTestButton test = new MicTestButton(0, 0, false, this);

    private float gain = -1f;
    private float threshold = -1f;
    private boolean gainDirty;
    private boolean thresholdDirty;
    private boolean listening;
    private volatile double micLevel;
    private float shownLevel;
    private long lastFrame = System.nanoTime();

    boolean listening() {
        return listening;
    }

    boolean testing() {
        return test.isMicActive();
    }

    void frame() {
        test.updateLastRender();
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastFrame) / 1_000_000_000f);
        lastFrame = now;
        shownLevel = test.isMicActive() ? Math.max((float) micLevel, shownLevel - dt * 1.2f) : 0f;
    }

    void stopTest() {
        test.stop();
    }

    void stop() {
        applyPending();
        test.stop();
    }

    private void load() {
        if (gain >= 0f) {
            return;
        }
        var config = VoicechatClient.CLIENT_CONFIG;
        gain = (float) ((config.microphoneGain.get() - VolumeManager.MIN_GAIN)
                / (VolumeManager.MAX_GAIN - VolumeManager.MIN_GAIN));
        threshold = (float) AudioUtils.dbToPerc(config.voiceActivationThreshold.get());
    }

    int gain(Gui gui, int x, int y, int w) {
        load();
        var config = VoicechatClient.CLIENT_CONFIG;
        boolean agcAvailable = SpeexManager.canUseAgc();
        boolean agc = config.agc.get();
        int gainX = x;
        int gainW = w;
        if (agcAvailable) {
            int agcW = 80;
            String label = Svc.text("message.voicechat.gain", Svc.text(agc ? "message.voicechat.gain.auto"
                    : "message.voicechat.gain.manual"));
            if (gui.button("svc-agc", x, y, agcW, ROW, label, ButtonKind.NORMAL, !agc, true,
                    agc ? null : Svc.text("message.voicechat.gain.manual.warning"))) {
                config.agc.set(!agc).save();
            }
            gainX = x + agcW + 1;
            gainW = w - agcW - 1;
        }
        long db = Math.round(gain * (VolumeManager.MAX_GAIN - VolumeManager.MIN_GAIN) + VolumeManager.MIN_GAIN);
        boolean active = !(agcAvailable && agc);
        float changed = gui.valueSlider("svc-gain", gainX, y, gainW, ROW,
                Svc.text("message.voicechat.microphone_gain", db), gain, 0f, active,
                db > 10 && active ? Svc.text("message.voicechat.microphone_gain.warning", 10) : null);
        if (changed != gain) {
            gain = changed;
            gainDirty = true;
        }
        return y + ROW + GAP;
    }

    int denoiser(Gui gui, int x, int y, int w) {
        var config = VoicechatClient.CLIENT_CONFIG;
        boolean denoiser = config.denoiser.get();
        boolean can = RNNoiseManager.canUseDenoiser();
        String state = Svc.text(denoiser && can ? "message.voicechat.denoiser.on" : "message.voicechat.denoiser.off");
        if (gui.button("svc-denoiser", x, y, w, ROW, Svc.text("message.voicechat.denoiser", state), ButtonKind.NORMAL,
                denoiser && can, can, null)) {
            config.denoiser.set(!denoiser).save();
        }
        return y + ROW + GAP;
    }

    void testToggle(Gui gui, int x, int y) {
        boolean ready = test.active;
        boolean on = test.isMicActive();
        String tip = Svc.text(!ready ? "message.voicechat.mic_test_unavailable"
                : on ? "message.voicechat.mic_test.enabled" : "message.voicechat.mic_test.disabled");
        if (gui.iconToggle("svc-test", x, y, ROW, ROW, on ? VoiceIcons.MIC : VoiceIcons.MIC_OFF, on, ready, tip)) {
            test.onPress(null);
        }
    }

    int activation(Gui gui, int x, int y, int w) {
        var config = VoicechatClient.CLIENT_CONFIG;
        MicrophoneActivationType type = config.microphoneActivationType.get();
        testToggle(gui, x, y);
        if (gui.button("svc-activation", x + ROW + 1, y, w - ROW - 1, ROW,
                Svc.text("message.voicechat.activation_type", type.getText()), null)) {
            MicrophoneActivationType[] values = MicrophoneActivationType.values();
            config.microphoneActivationType.set(values[(type.ordinal() + 1) % values.length]).save();
            listening = false;
        }
        return y + ROW + GAP;
    }

    int level(Gui gui, int x, int y, int w) {
        gui.progress(x, y, w, 8, shownLevel);
        return y + 8 + GAP;
    }

    int vad(Gui gui, int x, int y, int w) {
        var config = VoicechatClient.CLIENT_CONFIG;
        boolean vad = config.vad.get();
        boolean can = RNNoiseManager.canUseDenoiser();
        String state = Svc.text(vad && can ? "message.voicechat.voice_activation_detection.auto"
                : "message.voicechat.voice_activation_detection.manual");
        if (gui.button("svc-vad", x, y, w, ROW, Svc.text("message.voicechat.voice_activation_detection", state),
                ButtonKind.NORMAL, vad && can, can, null)) {
            config.vad.set(!vad).save();
        }
        return y + ROW + GAP;
    }

    int threshold(Gui gui, int x, int y, int w) {
        load();
        var config = VoicechatClient.CLIENT_CONFIG;
        if (RNNoiseManager.canUseDenoiser() && config.vad.get()) {
            return y;
        }
        long db = Math.round(AudioUtils.percToDb(threshold));
        String tip = threshold >= 1f ? Svc.text("message.voicechat.voice_activation.disabled") : null;
        float changed = gui.valueSlider("svc-threshold", x, y, w, ROW, Svc.text("message.voicechat.voice_activation", db),
                threshold, shownLevel, true, tip);
        if (changed != threshold) {
            threshold = changed;
            thresholdDirty = true;
        }
        return y + ROW + GAP;
    }

    int keybind(Gui gui, int x, int y, int w) {
        KeyMapping mapping = KeyEvents.KEY_PTT;
        Component key = mapping.getTranslatedKeyMessage();
        String shown = listening ? "> " + key.getString() + " <" : key.getString();
        String label = Svc.text("message.voicechat.activation_type.ptt") + ": " + shown;
        if (gui.button("svc-ptt", x, y, w, ROW, label, ButtonKind.NORMAL, listening, true,
                Svc.text("message.voicechat.press_to_reassign_key"))) {
            listening = !listening;
        }
        return y + ROW + GAP;
    }

    boolean key(KeyEvent event) {
        if (!listening) {
            return false;
        }
        KeyEvents.KEY_PTT.setKey(event.isEscape() ? InputConstants.UNKNOWN : InputConstants.getKey(event));
        KeyMapping.resetMapping();
        Minecraft.getInstance().options.save();
        listening = false;
        return true;
    }

    boolean mouse(MouseButtonEvent event) {
        if (!listening) {
            return false;
        }
        if (event.button() != 0) {
            KeyEvents.KEY_PTT.setKey(InputConstants.Type.MOUSE.getOrCreate(event.button()));
            KeyMapping.resetMapping();
            Minecraft.getInstance().options.save();
        }
        listening = false;
        return true;
    }

    void applyPending() {
        var config = VoicechatClient.CLIENT_CONFIG;
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

    @Override
    public void onMicValue(double dB) {
        micLevel = Motion.clamp((float) AudioUtils.dbToPerc(dB), 0f, 1f);
    }

    @Override
    public void onStop() {
        micLevel = 0;
    }
}
