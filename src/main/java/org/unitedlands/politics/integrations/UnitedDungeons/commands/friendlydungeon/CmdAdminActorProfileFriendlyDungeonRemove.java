package org.unitedlands.politics.integrations.UnitedDungeons.commands.friendlydungeon;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.dungeons.classes.Dungeon;
import org.unitedlands.dungeons.managers.DungeonManager;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.unitedlands.classes.GeopolObject;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

@UnitedSubCommand(
    parent = CmdAdminActorProfileFriendlyDungeon.class,
    name = "remove",
    usage = "/upa actorprofile friendlydungeon remove <actor> <dungeon>",
    catchAll = true,
    requirePlugins = { "UnitedDungeons" }
)
public class CmdAdminActorProfileFriendlyDungeonRemove implements UnitedCommandExecutor {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return DungeonManager.instance().getDungeonNames();
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sendUsage(sender);
            return;
        }

        GeopolObject actor = UnitedLandsDataManager.instance().getGeopolObject(args[0]);
        if (actor == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[0]);
            return;
        }

        var profile = ActorProfileManager.instance().getActorProfile(actor.getUuid());
        if (profile == null) {
            United.messenger().send(sender, "errors.actorprofile.no-profile");
            return;
        }

        Dungeon dungeon = DungeonManager.instance().getDungeon(args[1]);
        if (dungeon == null) {
            United.messenger().send(sender, "errors.actorprofile.dungeon-not-found", args[1]);
            return;
        }

        var friendlyDungeons = profile.getFriendlyDungeons();
        if (friendlyDungeons != null) {
            var dungeonEntry = friendlyDungeons.stream().filter(r -> r.equals(dungeon.getUuid())).findAny().orElse(null);
            if (dungeonEntry != null) {

                friendlyDungeons.remove(dungeon.getUuid());
                profile.setFriendlyDungeons(friendlyDungeons);

                if (ActorProfileManager.instance().updateActorProfile(profile)) {
                    var cmd = "upa actorprofile edit " + actor.getName();
                    Bukkit.dispatchCommand(sender, cmd);
                } else {
                    United.messenger().send(sender, "errors.general.db-save-error");
                }
            } else {
                United.messenger().send(sender, "errors.actorprofile.dungeon-missing");
            }
        } else {
            United.messenger().send(sender, "errors.actorprofile.dungeon-missing");
        }

    }

}
