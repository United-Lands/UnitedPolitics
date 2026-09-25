package org.unitedlands.politics.integrations.UnitedDungeons.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.dungeons.UnitedDungeons;
import org.unitedlands.dungeons.classes.Dungeon;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.utils.Messenger;

public class AdminRemoveFriendlyDungeonSubcommand extends BaseCommandHandler<UnitedPolitics> {

    public AdminRemoveFriendlyDungeonSubcommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return UnitedDungeons.getInstance().getDungeonManager().getDungeonNames();
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            Messenger.sendMessage(sender, messageProvider.getList("usages.hostiledungeon.remove"), null,
                    messageProvider.get("prefix"));
            return;
        }

        IGeopolObjectWrapper actor = GeopolUtils.findGeopolObject(args[0]);
        if (actor == null) {
            Messenger.sendMessage(sender, messageProvider.get("errors.general.geopol-obj-not-found"),
                    Map.of("obj-name", args[0]));
            return;
        }

        var profile = ActorProfileManager.instance().getActorProfile(actor.getUUID());
        if (profile == null) {
            Messenger.sendMessage(sender, messageProvider.get("errors.actorprofile.no-profile"), null,
                    messageProvider.get("prefix"));
            return;
        }

        Dungeon dungeon = UnitedDungeons.getInstance().getDungeonManager().getDungeon(args[1]);
        if (dungeon == null) {
            Messenger.sendMessage(sender, messageProvider.get("errors.actorprofile.dungeon-not-found"),
                    Map.of("obj-name", args[1]));
            return;
        }

        var friendlyDungeons = profile.getFriendlyDungeons();
        if (friendlyDungeons != null) {
            var dungeonEntry = friendlyDungeons.stream().filter(r -> r.equals(dungeon.getUuid())).findAny().orElse(null);
            if (dungeonEntry != null) {

                friendlyDungeons.remove(dungeon.getUuid());
                profile.setFriendlyDungeons(friendlyDungeons);

                if (ActorProfileManager.instance().addOrUpdateActorProfile(profile)) {
                    var cmd = "upa actorprofile edit " + actor.getName();
                    Bukkit.dispatchCommand(sender, cmd);
                } else {
                    Messenger.sendMessage(sender, messageProvider.get("errors.general.db-save-error"),
                            null);
                }

            } else {
                Messenger.sendMessage(sender, messageProvider.get("errors.actorprofile.dungeon-missing"), null,
                        messageProvider.get("prefix"));
            }
        } else {
            Messenger.sendMessage(sender, messageProvider.get("errors.actorprofile.dungeon-missing"), null,
                    messageProvider.get("prefix"));
            return;
        }

    }

}
