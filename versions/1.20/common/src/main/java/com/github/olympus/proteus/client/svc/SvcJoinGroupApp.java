package com.github.olympus.proteus.client.svc;

import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.UiHost;
import com.github.olympus.proteus.client.ui.VoiceIcons;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.gui.CreateGroupScreen;
import de.maxhenkel.voicechat.gui.EnterPasswordScreen;
import de.maxhenkel.voicechat.gui.GroupType;
import de.maxhenkel.voicechat.gui.group.GroupScreen;
import de.maxhenkel.voicechat.net.ClientServerNetManager;
import de.maxhenkel.voicechat.net.JoinGroupPacket;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.screens.Screen;

final class SvcJoinGroupApp extends SvcPanel {
    private static final int CELL = 28;
    private static final int HEAD = 12;
    private static final int MEMBERS_SHOWN = 10;

    private boolean wasOut;

    SvcJoinGroupApp(UiHost host, Screen original) {
        super(host, original);
    }

    @Override
    protected String title() {
        return Svc.text("message.voicechat.join_create_group");
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
        Map<UUID, List<PlayerState>> members = new HashMap<>();
        for (PlayerState state : ClientManager.getPlayerStateManager().getPlayerStates(true)) {
            if (state.hasGroup()) {
                members.computeIfAbsent(state.getGroup(), key -> new ArrayList<>()).add(state);
            }
        }
        List<ClientGroup> groups = new ArrayList<>();
        for (ClientGroup group : ClientManager.getGroupManager().getGroups()) {
            if (!group.isHidden()) {
                groups.add(group);
            }
        }
        groups.sort(Comparator.comparing(ClientGroup::getName));

        if (groups.isEmpty()) {
            String none = Svc.text("message.voicechat.no_groups");
            gui().centered(none, x + w / 2, y + h / 2 - 4, theme.textDisabled());
            return;
        }

        float scroll = gui().scrollBegin("svc-groups", x, y, w, h);
        int lw = w - 8;
        int top = y - Math.round(scroll);
        int ry = top;
        for (ClientGroup group : groups) {
            List<PlayerState> inside = members.getOrDefault(group.getId(), new ArrayList<>());
            inside.sort(Comparator.comparing(PlayerState::getName));
            String id = group.getId().toString();
            gui().rowBackground("svc-group-" + id, x, ry, lw, CELL, false);
            int textX = x + 8;
            if (group.hasPassword()) {
                theme.icon(gui().canvas(), VoiceIcons.LOCK, x + 12, ry + CELL / 2, theme.textSecondary());
                textX += 14;
            }
            int headsW = Math.min(inside.size(), 6) * (HEAD + 1);
            gui().text(gui().fit(group.getName(), lw - (textX - x) - headsW - 14), textX, gui().textY(ry, CELL),
                    theme.textPrimary());
            int hx = x + lw - 8 - HEAD;
            for (int i = 0; i < Math.min(inside.size(), 6); i++) {
                gui().canvas().head(inside.get(i).getUuid(), hx - i * (HEAD + 1), ry + (CELL - HEAD) / 2, HEAD);
            }
            if (gui().hit("svc-group-hit-" + id, x, ry, lw, CELL, true, tooltip(group, inside))) {
                if (group.hasPassword()) {
                    EnterPasswordScreen screen = new EnterPasswordScreen(group);
                    SvcScreens.remember(screen, group);
                    Svc.open(screen);
                } else {
                    ClientServerNetManager.sendToServer(new JoinGroupPacket(group.getId(), null));
                }
            }
            ry += CELL + 2;
        }
        gui().scrollEnd(ry - top);
    }

    private static String tooltip(ClientGroup group, List<PlayerState> inside) {
        StringBuilder text = new StringBuilder();
        if (group.getType().equals(Group.Type.NORMAL)) {
            text.append(Svc.text("message.voicechat.group_title", group.getName()));
        } else {
            text.append(Svc.text("message.voicechat.group_type_title", group.getName(),
                    GroupType.fromType(group.getType()).getTranslation().getString()));
        }
        text.append('\n');
        if (inside.isEmpty()) {
            text.append(Svc.text("message.voicechat.no_group_members"));
        } else {
            text.append(Svc.text("message.voicechat.group_members"));
            for (int i = 0; i < inside.size(); i++) {
                if (i >= MEMBERS_SHOWN) {
                    text.append('\n').append(Svc.text("message.voicechat.more_members", inside.size() - MEMBERS_SHOWN));
                    break;
                }
                text.append("\n  ").append(inside.get(i).getName());
            }
        }
        return text.toString();
    }

    @Override
    protected void footer(Theme theme, int x, int y, int w) {
        if (gui().button("svc-create-group", x, y, w, ROW, Svc.text("message.voicechat.create_group_button"), null)) {
            Svc.open(new CreateGroupScreen());
        }
    }
}
