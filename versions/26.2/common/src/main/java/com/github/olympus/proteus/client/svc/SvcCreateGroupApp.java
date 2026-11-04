package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.UiHost;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.GroupType;
import de.maxhenkel.voicechat.gui.group.GroupScreen;
import de.maxhenkel.voicechat.net.ClientServerNetManager;
import de.maxhenkel.voicechat.net.CreateGroupPacket;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.client.gui.screens.Screen;

final class SvcCreateGroupApp extends SvcPanel {
    private GroupType type = GroupType.NORMAL;
    private String name = "";
    private String password = "";
    private boolean wasOut;

    SvcCreateGroupApp(UiHost host, Screen original) {
        super(host, original);
    }

    @Override
    protected String title() {
        return Svc.text("message.voicechat.create_group");
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
        gui().text(Svc.text("message.voicechat.group_name"), x + 2, y, theme.textSecondary());
        y += 12;
        name = gui().field("svc-group-name", x, y, w, ROW, "", Voicechat.MAX_GROUP_NAME_LENGTH);
        y += ROW + GAP + 2;
        gui().text(Svc.text("message.voicechat.optional_password"), x + 2, y, theme.textSecondary());
        y += 12;
        password = gui().field("svc-group-password", x, y, w, ROW, "", Voicechat.MAX_GROUP_NAME_LENGTH);
        y += ROW + GAP + 2;
        String label = Svc.text("message.voicechat.group_type") + ": " + type.getTranslation().getString();
        if (gui().button("svc-group-type", x, y, w, ROW, label, type.getDescription().getString())) {
            GroupType[] values = GroupType.values();
            type = values[(type.ordinal() + 1) % values.length];
        }
        if (gui().submitted("svc-group-name") || gui().submitted("svc-group-password")) {
            create();
        }
    }

    @Override
    protected void footer(Theme theme, int x, int y, int w) {
        if (gui().button("svc-create", x, y, w, ROW, Svc.text("message.voicechat.create"), ButtonKind.PRIMARY, false,
                !name.isEmpty(), null)) {
            create();
        }
    }

    private void create() {
        if (!name.isEmpty()) {
            ClientServerNetManager.sendToServer(new CreateGroupPacket(name, password.isEmpty() ? null : password,
                    type.getType()));
        }
    }
}
