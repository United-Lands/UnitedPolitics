package org.unitedlands.politics.integrations.UnitedDungeons.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.dungeons.UnitedDungeons;
import org.unitedlands.dungeons.classes.Dungeon;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.utils.Logger;
import org.unitedlands.utils.Messenger;

public class AdminAddHostileDungeonSubcommand extends BaseCommandHandler<UnitedPolitics> {

    public AdminAddHostileDungeonSubcommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 2) {
            Logger.log("Dungeons: " + String.join(",", UnitedDungeons.getInstance().getDungeonManager().getDungeonNames()));
            return UnitedDungeons.getInstance().getDungeonManager().getDungeonNames();
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            Messenger.sendMessage(sender, messageProvider.getList("messages.usages.hostiledungeon.add"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        IGeopolObjectWrapper actor = GeopolUtils.findGeopolObject(args[0]);
        if (actor == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.geopol-obj-not-found"),
                    Map.of("obj-name", args[0]), messageProvider.get("messages.prefix"));
            return;
        }

        var profile = plugin.getActorProfileManager().getActorProfile(actor.getUUID());
        if (profile == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.actorprofile.no-profile"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        Dungeon dungeon = UnitedDungeons.getInstance().getDungeonManager().getDungeon(args[1]);
        if (dungeon == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.actorprofile.dungeon-not-found"),
                    Map.of("obj-name", args[1]), messageProvider.get("messages.prefix"));
            return;
        }

        var actorHostileDungeons = profile.getHostileDungeons();
        if (actorHostileDungeons != null) {
            if (actorHostileDungeons.stream().filter(r -> r.equals(dungeon.getUuid())).findAny().orElse(null) != null) {
                Messenger.sendMessage(sender, messageProvider.get("messages.errors.actorprofile.dungeon-exists"), null, messageProvider.get("messages.prefix"));
            }
        } else {
            actorHostileDungeons = new HashSet<>();
        }

        actorHostileDungeons.add(dungeon.getUuid());
        profile.setHostileDungeons(actorHostileDungeons);

        if (plugin.getActorProfileManager().addOrUpdateActorProfile(profile)) {
            var cmd = "upa actorprofile edit " + actor.getName();
            Bukkit.dispatchCommand(sender, cmd);
        } else {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.db-save-error"),
                    null, messageProvider.get("messages.prefix"));
        }
    }

}
