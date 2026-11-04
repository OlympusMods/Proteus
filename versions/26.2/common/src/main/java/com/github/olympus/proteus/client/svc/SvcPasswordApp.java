package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.UiHost;
import de.maxhenkel.voicechat.gui.group.GroupScreen;
import de.maxhenkel.voicechat.net.ClientServerNetManager;
import de.maxhenkel.voicechat.net.JoinGroupPacket;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.client.gui.screens.Screen;

final class SvcPasswordApp extends SvcPanel {
    private final ClientGroup target;
    private String password = "";
    private boolean wasOut;

    SvcPasswordApp(UiHost host, Screen original, ClientGroup target) {
        super(host, original);
        this.target = target;
    }

    @Override
    protected String title() {
        return Svc.text("message.voicechat.enter_group_password");
    }

    @Override
    protected void onTick() {
        ClientGroup group = ClientManager.getPlayerStateManager().getGroup();
        if (group == null) {
            wasOut = true;
        } else if (wasOut) {
            Svc.open(new GroupScreen(group));
        }
    }

    @Override
    protected void body(Theme theme, int x, int y, int w, int h) {
        gui().text(Svc.text("message.voicechat.password"), x + 2, y, theme.textSecondary());
        y += 12;
        password = gui().field("svc-group-join-password", x, y, w, ROW, "", 32);
        if (gui().submitted("svc-group-join-password")) {
            join();
        }
    }

    @Override
    protected void footer(Theme theme, int x, int y, int w) {
        if (gui().button("svc-join", x, y, w, ROW, Svc.text("message.voicechat.join_group"), ButtonKind.PRIMARY, false,
                !password.isEmpty(), null)) {
            join();
        }
    }

    private void join() {
        if (!password.isEmpty()) {
            ClientServerNetManager.sendToServer(new JoinGroupPacket(target.getId(), password));
        }
    }
}
