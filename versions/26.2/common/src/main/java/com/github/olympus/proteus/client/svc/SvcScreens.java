package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ProteusScreen;
import com.github.olympus.proteus.client.ui.UiApp;
import com.github.olympus.proteus.client.ui.UiHost;
import de.maxhenkel.voicechat.gui.CreateGroupScreen;
import de.maxhenkel.voicechat.gui.EnterPasswordScreen;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import de.maxhenkel.voicechat.gui.audiodevice.SelectMicrophoneScreen;
import de.maxhenkel.voicechat.gui.audiodevice.SelectSpeakerScreen;
import de.maxhenkel.voicechat.gui.group.GroupScreen;
import de.maxhenkel.voicechat.gui.group.JoinGroupScreen;
import de.maxhenkel.voicechat.gui.onboarding.ActivationOnboardingScreen;
import de.maxhenkel.voicechat.gui.onboarding.FinalOnboardingScreen;
import de.maxhenkel.voicechat.gui.onboarding.IntroductionOnboardingScreen;
import de.maxhenkel.voicechat.gui.onboarding.MicOnboardingScreen;
import de.maxhenkel.voicechat.gui.onboarding.OnboardingScreenBase;
import de.maxhenkel.voicechat.gui.onboarding.PttOnboardingScreen;
import de.maxhenkel.voicechat.gui.onboarding.SkipOnboardingScreen;
import de.maxhenkel.voicechat.gui.onboarding.SpeakerOnboardingScreen;
import de.maxhenkel.voicechat.gui.onboarding.VoiceActivationOnboardingScreen;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumesScreen;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

final class SvcScreens {
    private static final Map<Screen, ClientGroup> GROUPS = new WeakHashMap<>();

    private SvcScreens() {
    }

    static void remember(Screen screen, ClientGroup group) {
        GROUPS.put(screen, group);
    }

    static Screen replace(Screen screen) {
        Function<UiHost, UiApp> app = app(screen, previous());
        if (app == null) {
            return screen;
        }
        return new ProteusScreen(screen.getTitle(), parent(), app);
    }

    private static Function<UiHost, UiApp> app(Screen screen, Screen previous) {
        if (screen instanceof VoiceChatScreen) {
            return SvcMenuApp::new;
        }
        if (screen instanceof VoiceChatSettingsScreen) {
            return host -> new SvcSettingsApp(host, screen);
        }
        if (screen instanceof JoinGroupScreen) {
            return host -> new SvcJoinGroupApp(host, screen);
        }
        if (screen instanceof CreateGroupScreen) {
            return host -> new SvcCreateGroupApp(host, screen);
        }
        if (screen instanceof EnterPasswordScreen) {
            ClientGroup group = GROUPS.get(screen);
            return group == null ? null : host -> new SvcPasswordApp(host, screen, group);
        }
        if (screen instanceof GroupScreen) {
            return host -> new SvcGroupApp(host, screen);
        }
        if (screen instanceof AdjustVolumesScreen) {
            return host -> new SvcVolumesApp(host, screen);
        }
        if (screen instanceof SelectMicrophoneScreen) {
            return host -> new SvcDevicesApp(host, screen, previous, true);
        }
        if (screen instanceof SelectSpeakerScreen) {
            return host -> new SvcDevicesApp(host, screen, previous, false);
        }
        if (screen instanceof OnboardingScreenBase onboarding) {
            SvcOnboardingApp.Page page = page(screen);
            if (page == null) {
                return null;
            }
            Screen back = previous instanceof OnboardingScreenBase ? previous : null;
            return host -> new SvcOnboardingApp(host, onboarding, back, page);
        }
        return null;
    }

    private static SvcOnboardingApp.Page page(Screen screen) {
        if (screen instanceof IntroductionOnboardingScreen) {
            return SvcOnboardingApp.Page.INTRODUCTION;
        }
        if (screen instanceof SpeakerOnboardingScreen) {
            return SvcOnboardingApp.Page.SPEAKER;
        }
        if (screen instanceof MicOnboardingScreen) {
            return SvcOnboardingApp.Page.MICROPHONE;
        }
        if (screen instanceof ActivationOnboardingScreen) {
            return SvcOnboardingApp.Page.ACTIVATION;
        }
        if (screen instanceof VoiceActivationOnboardingScreen) {
            return SvcOnboardingApp.Page.VOICE;
        }
        if (screen instanceof PttOnboardingScreen) {
            return SvcOnboardingApp.Page.PTT;
        }
        if (screen instanceof FinalOnboardingScreen) {
            return SvcOnboardingApp.Page.FINAL;
        }
        if (screen instanceof SkipOnboardingScreen) {
            return SvcOnboardingApp.Page.SKIP;
        }
        return null;
    }

    private static Screen previous() {
        Screen current = Minecraft.getInstance().gui.screen();
        if (current instanceof ProteusScreen proteus && proteus.app() instanceof SvcPage page) {
            return page.original();
        }
        return null;
    }

    private static Screen parent() {
        Screen current = Minecraft.getInstance().gui.screen();
        return current instanceof ProteusScreen proteus ? proteus.parent() : current;
    }
}
