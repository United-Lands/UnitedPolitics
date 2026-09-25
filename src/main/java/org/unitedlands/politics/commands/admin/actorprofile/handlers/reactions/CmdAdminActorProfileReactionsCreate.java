package org.unitedlands.politics.commands.admin.actorprofile.handlers.reactions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.classes.EventReaction;
import org.unitedlands.politics.classes.configs.RecordDefinitionConfig;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.utils.United;

@UnitedSubCommand(
    parent = CmdAdminActorProfileReactions.class,
    name = "create",
    usage = "/upa actorprofile reactions create <actor> <event> <reaction> <amount>",
    catchAll = true
)
public class CmdAdminActorProfileReactionsCreate implements UnitedCommandExecutor {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return UnitedPolitics.instance().getConfig().getStringList("event-keys");
        } else if (args.length == 3) {
            return RecordDefinitionConfig.get().recordDefinitions().keys().stream().toList();
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length != 4) {
            sendUsage(sender);
            return;
        }

        IGeopolObjectWrapper actor = GeopolUtils.findGeopolObject(args[0]);
        if (actor == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found");
            return;
        }

        var profile = ActorProfileManager.instance().getActorProfile(actor.getUUID());
        if (profile == null) {
            United.messenger().send(sender, "errors.actorprofile.no-profile");
            return;
        }

        var actorReactions = profile.getEventReactions();

        var eventKey = args[1];
        var reactionRecordKey = args[2];

        double amount = 0;
        try {
            amount = Double.parseDouble(args[3]);
        } catch (Exception ex) {
            United.messenger().send(sender, "errors.general.number-format-error", args[3]);
            return;
        }

        if (actorReactions != null) {
            if (actorReactions.stream().filter(
                    r -> r.getEventKey().equals(eventKey) && r.getReactionReputationKey().equals(reactionRecordKey))
                    .findAny().orElse(null) != null) {
                United.messenger().send(sender, "errors.actorprofile.reaction-exists");
            }
        } else {
            actorReactions = new ArrayList<>();
        }

        EventReaction reaction = new EventReaction(eventKey, reactionRecordKey, amount);
        actorReactions.add(reaction);

        profile.setEventReactions(actorReactions);

        if (ActorProfileManager.instance().addOrUpdateActorProfile(profile)) {
            var cmd = "upa actorprofile edit " + actor.getName();
            Bukkit.dispatchCommand(sender, cmd);
        } else {
            United.messenger().send(sender, "errors.general.db-save-error");
        }
    }

}
