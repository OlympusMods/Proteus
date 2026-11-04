package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.RawInput;
import com.github.olympus.proteus.client.ui.RawKey;
import com.github.olympus.proteus.client.ui.RawMouse;
import com.github.olympus.proteus.client.ui.Draw;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.UiHost;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import de.maxhenkel.voicechat.gui.onboarding.ActivationOnboardingScreen;
import de.maxhenkel.voicechat.gui.onboarding.OnboardingManager;
import de.maxhenkel.voicechat.gui.onboarding.OnboardingScreenBase;
import de.maxhenkel.voicechat.gui.onboarding.PttOnboardingScreen;
import de.maxhenkel.voicechat.gui.onboarding.SkipOnboardingScreen;
import de.maxhenkel.voicechat.gui.onboarding.VoiceActivationOnboardingScreen;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.screens.Screen;

final class SvcOnboardingApp extends SvcPanel implements RawInput {
    enum Page {
        INTRODUCTION,
        SPEAKER,
        MICROPHONE,
        ACTIVATION,
        VOICE,
        PTT,
        FINAL,
        SKIP
    }

    private final OnboardingScreenBase screen;
    private final Screen previous;
    private final Page page;
    private final SvcMicControls mic = new SvcMicControls();
    private final SvcDeviceList devices;

    SvcOnboardingApp(UiHost host, OnboardingScreenBase screen, Screen previous, Page page) {
        super(host, screen);
        this.screen = screen;
        this.previous = previous;
        this.page = page;
        this.devices = page == Page.SPEAKER || page == Page.MICROPHONE ? new SvcDeviceList(page == Page.MICROPHONE)
                : null;
    }

    @Override
    protected String title() {
        return switch (page) {
            case INTRODUCTION -> Svc.text("message.voicechat.onboarding.introduction.title",
                    CommonCompatibilityManager.INSTANCE.getModName());
            case SPEAKER -> Svc.text("message.voicechat.onboarding.speaker");
            case MICROPHONE -> Svc.text("message.voicechat.onboarding.microphone");
            case ACTIVATION -> Svc.text("message.voicechat.onboarding.activation.title");
            case VOICE -> Svc.text("message.voicechat.onboarding.voice.title");
            case PTT -> Svc.text("message.voicechat.onboarding.ptt.title");
            case FINAL -> Svc.text("message.voicechat.onboarding.final");
            case SKIP -> Svc.text("message.voicechat.onboarding.skip.title");
        };
    }

    private String description() {
        return switch (page) {
            case INTRODUCTION -> Svc.text("message.voicechat.onboarding.introduction.description");
            case ACTIVATION -> Svc.text("message.voicechat.onboarding.activation") + "\n\n"
                    + Svc.text("message.voicechat.onboarding.activation.ptt",
                            Svc.text("message.voicechat.onboarding.activation.ptt.name")) + "\n\n"
                    + Svc.text("message.voicechat.onboarding.activation.voice",
                            Svc.text("message.voicechat.onboarding.activation.voice.name"));
            case VOICE -> Svc.text("message.voicechat.onboarding.voice.description");
            case PTT -> Svc.text("message.voicechat.onboarding.ptt.description");
            case SKIP -> Svc.text("message.voicechat.onboarding.skip.description");
            case FINAL -> finalDescription();
            default -> "";
        };
    }

    private static String finalDescription() {
        String text = Svc.text("message.voicechat.onboarding.final.description.success",
                KeyEvents.KEY_VOICE_CHAT.getTranslatedKeyMessage().getString()) + "\n\n";
        if (VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get() == MicrophoneActivationType.PTT) {
            text += Svc.text("message.voicechat.onboarding.final.description.ptt",
                    KeyEvents.KEY_PTT.getTranslatedKeyMessage().getString());
        } else {
            text += Svc.text("message.voicechat.onboarding.final.description.voice",
                    KeyEvents.KEY_MUTE.getTranslatedKeyMessage().getString());
        }
        return text + "\n\n" + Svc.text("message.voicechat.onboarding.final.description.configuration");
    }

    @Override
    protected int footerHeight() {
        return page == Page.INTRODUCTION || page == Page.ACTIVATION ? 2 * ROW + GAP : ROW;
    }

    @Override
    protected void body(Theme theme, int x, int y, int w, int h) {
        mic.frame();
        if (devices != null) {
            devices.render(gui(), theme, x, y, w, h);
            return;
        }
        float scroll = gui().scrollBegin("svc-onboarding", x, y, w, h);
        x += 4;
        int lw = w - 8;
        int top = y - Math.round(scroll);
        y = top;
        for (String line : Draw.wrap(fade(), description(), lw)) {
            gui().text(line, x, y, theme.textPrimary());
            y += 11;
        }
        y += GAP * 2;
        if (page == Page.VOICE) {
            y = mic.gain(gui(), x, y, lw);
            y = mic.denoiser(gui(), x, y, lw);
            mic.testToggle(gui(), x, y);
            y = mic.vad(gui(), x + ROW + 1, y, lw - ROW - 1);
            y = mic.threshold(gui(), x, y, lw);
        } else if (page == Page.PTT) {
            String hint = Svc.text("message.voicechat.onboarding.ptt.button_description");
            gui().centered(hint, x + lw / 2, y, theme.textSecondary());
            y += 14;
            y = mic.keybind(gui(), x + 40, y, lw - 80);
        }
        gui().scrollEnd(y - top);
    }

    @Override
    protected void footer(Theme theme, int x, int y, int w) {
        int half = (w - GAP) / 2;
        String back = Svc.text(previous != null ? "message.voicechat.onboarding.back"
                : "message.voicechat.onboarding.cancel");
        String next = Svc.text("message.voicechat.onboarding.next");
        switch (page) {
            case INTRODUCTION -> {
                if (gui().button("svc-skip", x, y, w, ROW, Svc.text("message.voicechat.onboarding.introduction.skip"),
                        null)) {
                    Svc.open(new SkipOnboardingScreen(screen));
                }
                y += ROW + GAP;
                pair(x, y, half, back, next, ButtonKind.PRIMARY, () -> Svc.open(screen.getNextScreen()));
            }
            case ACTIVATION -> {
                String ptt = Svc.text("message.voicechat.onboarding.activation.ptt.name");
                String voice = Svc.text("message.voicechat.onboarding.activation.voice.name");
                if (gui().button("svc-activation-ptt", x, y, half, ROW, ptt, null)) {
                    VoicechatClient.CLIENT_CONFIG.microphoneActivationType.set(MicrophoneActivationType.PTT).save();
                    Svc.open(new PttOnboardingScreen(screen));
                }
                if (gui().button("svc-activation-voice", x + half + GAP, y, half, ROW, voice, null)) {
                    VoicechatClient.CLIENT_CONFIG.microphoneActivationType.set(MicrophoneActivationType.VOICE).save();
                    Svc.open(new VoiceActivationOnboardingScreen(screen));
                }
                y += ROW + GAP;
                if (gui().button("svc-onboarding-back", x, y, w, ROW, back, null)) {
                    goBack();
                }
            }
            case FINAL -> pair(x, y, half, back, Svc.text("message.voicechat.onboarding.final.finish_setup"),
                    ButtonKind.PRIMARY, OnboardingManager::finishOnboarding);
            case SKIP -> pair(x, y, half, back, Svc.text("message.voicechat.onboarding.confirm"), ButtonKind.PRIMARY,
                    OnboardingManager::finishOnboarding);
            default -> pair(x, y, half, back, next, ButtonKind.PRIMARY, () -> Svc.open(screen.getNextScreen()));
        }
    }

    private void pair(int x, int y, int half, String back, String forward, ButtonKind kind, Runnable action) {
        if (gui().button("svc-onboarding-back", x, y, half, ROW, back, null)) {
            goBack();
        }
        if (gui().button("svc-onboarding-next", x + half + GAP, y, half, ROW, forward, kind, false, true, null)) {
            action.run();
        }
    }

    private void goBack() {
        if (previous != null) {
            Svc.open(previous);
        } else {
            requestClose();
        }
    }

    @Override
    protected boolean onEscape() {
        if (mic.listening()) {
            return true;
        }
        if (page == Page.FINAL) {
            OnboardingManager.finishOnboarding();
            return true;
        }
        return false;
    }

    @Override
    protected void afterFrame() {
        if (!gui().dragging()) {
            mic.applyPending();
        }
    }

    @Override
    public boolean rawKey(RawKey key) {
        if (mic.key(key)) {
            return true;
        }
        if (page == Page.FINAL && KeyEvents.KEY_VOICE_CHAT != null
                && ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_VOICE_CHAT).getValue()
                == key.code()) {
            OnboardingManager.finishOnboarding();
            Svc.open(new VoiceChatScreen());
            return true;
        }
        return false;
    }

    @Override
    public boolean rawMouse(RawMouse mouse) {
        return mic.mouse(mouse);
    }

    @Override
    public void removed() {
        mic.stop();
        if (devices != null) {
            devices.stop();
        }
    }
}
