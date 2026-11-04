package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.Gui;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.VoiceIcons;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.MicTestButton;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.client.TestSoundPlayer;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import java.util.ArrayList;
import java.util.List;

final class SvcDeviceList {
    private static final int CELL = 28;
    private static final int ROW = 20;

    private final boolean microphone;
    private final List<String> devices = new ArrayList<>();
    private final MicTestButton test;
    private volatile boolean speakerBusy;

    SvcDeviceList(boolean microphone) {
        this.microphone = microphone;
        this.test = microphone ? new MicTestButton(0, 0, true) : null;
        devices.add("");
        devices.addAll(microphone ? MicrophoneManager.deviceNames() : SoundManager.getAllSpeakers());
    }

    void stop() {
        if (test != null) {
            test.stop();
        }
    }

    private String current() {
        return (microphone ? VoicechatClient.CLIENT_CONFIG.microphone : VoicechatClient.CLIENT_CONFIG.speaker).get();
    }

    private void select(String device) {
        stop();
        (microphone ? VoicechatClient.CLIENT_CONFIG.microphone : VoicechatClient.CLIENT_CONFIG.speaker).set(device)
                .save();
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            client.reloadAudio();
        }
    }

    void render(Gui gui, Theme theme, int x, int y, int w, int h) {
        if (test != null) {
            test.updateLastRender();
        }
        String selectedDevice = current();
        float scroll = gui.scrollBegin("svc-devices", x, y, w, h);
        int lw = w - 8;
        int top = y - Math.round(scroll);
        int ry = top;
        for (String device : devices) {
            boolean selected = selectedDevice.equals(device);
            String id = "svc-device-" + device;
            gui.rowBackground(id, x, ry, lw, CELL, selected);
            if (selected) {
                theme.icon(gui.canvas(), "check", x + 12, ry + CELL / 2, theme.textPrimary());
            }
            String name = device.isEmpty()
                    ? Svc.text(microphone ? "message.voicechat.default_microphone" : "message.voicechat.default_speaker")
                    : SoundManager.cleanDeviceName(device);
            int reserved = selected ? ROW + 14 : 8;
            gui.text(gui.fit(name, lw - 28 - reserved), x + 24, gui.textY(ry, CELL), theme.textPrimary());
            if (gui.hit(id + "-hit", x, ry, lw - (selected ? ROW + 12 : 0), CELL, !selected, null)) {
                select(device);
            }
            if (selected) {
                testButton(gui, x + lw - 6 - ROW, ry + (CELL - ROW) / 2);
            }
            ry += CELL + 2;
        }
        gui.scrollEnd(ry - top);
    }

    private void testButton(Gui gui, int x, int y) {
        if (microphone) {
            boolean on = test.isMicActive();
            String tip = Svc.text(!test.active ? "message.voicechat.mic_test_unavailable"
                    : on ? "message.voicechat.mic_test.enabled" : "message.voicechat.mic_test.disabled");
            if (gui.iconToggle("svc-device-test", x, y, ROW, ROW, on ? VoiceIcons.MIC : VoiceIcons.MIC_OFF, on,
                    test.active, tip)) {
                Svc.press(test);
            }
            return;
        }
        if (gui.iconToggle("svc-device-test", x, y, ROW, ROW, VoiceIcons.SPEAKER, speakerBusy, !speakerBusy,
                Svc.text("message.voicechat.test_speaker"))) {
            speakerBusy = true;
            TestSoundPlayer.playTestSound(() -> speakerBusy = false);
        }
    }
}
