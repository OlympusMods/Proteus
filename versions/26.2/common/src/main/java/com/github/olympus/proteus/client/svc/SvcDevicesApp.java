package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.UiHost;
import net.minecraft.client.gui.screens.Screen;

final class SvcDevicesApp extends SvcPanel {
    private final boolean microphone;
    private final Screen previous;
    private final SvcDeviceList list;

    SvcDevicesApp(UiHost host, Screen original, Screen previous, boolean microphone) {
        super(host, original);
        this.previous = previous;
        this.microphone = microphone;
        this.list = new SvcDeviceList(microphone);
    }

    @Override
    protected String title() {
        return Svc.text(microphone ? "gui.voicechat.select_microphone.title" : "gui.voicechat.select_speaker.title");
    }

    @Override
    protected void body(Theme theme, int x, int y, int w, int h) {
        list.render(gui(), theme, x, y, w, h);
    }

    @Override
    protected void footer(Theme theme, int x, int y, int w) {
        if (gui().button("svc-back", x, y, w, ROW, Svc.text("message.voicechat.back"), ButtonKind.PRIMARY, false, true,
                null)) {
            if (previous != null) {
                Svc.open(previous);
            } else {
                requestClose();
            }
        }
    }

    @Override
    public void removed() {
        list.stop();
    }
}
