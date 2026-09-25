package org.unitedlands.politics.commands.admin.actorprofile.handlers.reactions;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.utils.United;

@UnitedSubCommand(
    parent = CmdAdminActorProfileReactions.class,
    name = "remove",
    usage = "/upa actorprofile reactions create <actor> <event>",
    catchAll = true
)
public class CmdAdminActorProfileReactionsRemove implements UnitedCommandExecutor {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return UnitedPolitics.instance().getConfig().getStringList("event-keys");
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sendUsage(sender);
            return;
        }

        IGeopolObjectWrapper actor = GeopolUtils.findGeopolObject(args[0]);
        if (actor == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[0]);
            return;
        }

        var profile = ActorProfileManager.instance().getActorProfile(actor.getUUID());
        if (profile == null) {
            United.messenger().send(sender, "errors.actorprofile.no-profile");
            return;
        }

        var actorReactions = profile.getEventReactions();

        var eventKey = args[1];

        if (actorReactions != null) {
            var reaction = actorReactions.stream().filter(r -> r.getEventKey().equals(eventKey)).findAny().orElse(null);
            if (reaction != null) {

                actorReactions.remove(reaction);
                profile.setEventReactions(actorReactions);

                if (ActorProfileManager.instance().addOrUpdateActorProfile(profile)) {
                    var cmd = "upa actorprofile edit " + actor.getName();
                    Bukkit.dispatchCommand(sender, cmd);
                } else {
                    United.messenger().send(sender, "errors.general.db-save-error");
                }
            } else {
                United.messenger().send(sender, "errors.actorprofile.reaction-missing");
                return;
            }
        } else {
            United.messenger().send(sender, "errors.actorprofile.reaction-missing");
            return;
        }

    }

}
