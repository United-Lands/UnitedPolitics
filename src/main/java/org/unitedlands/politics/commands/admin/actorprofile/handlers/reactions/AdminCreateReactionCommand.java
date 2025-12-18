package org.unitedlands.politics.commands.admin.actorprofile.handlers.reactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.classes.EventReaction;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.utils.Messenger;

public class AdminCreateReactionCommand extends BaseCommandHandler<UnitedPolitics> {

    public AdminCreateReactionCommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return plugin.getConfig().getStringList("event-keys");
        } else if (args.length == 3) {
            return plugin.getConfig().getConfigurationSection("record-definitions").getKeys(false).stream()
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length != 4) {
            Messenger.sendMessage(sender, messageProvider.getList("messages.usages.reaction.create"), null,
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

        var actorReactions = profile.getEventReactions();

        var eventKey = args[1];
        var reactionRecordKey = args[2];

        double amount = 0;
        try {
            amount = Double.parseDouble(args[3]);
        } catch (Exception ex) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.number-format-error"),
                    Map.of("input", args[3]), messageProvider.get("messages.prefix"));
            return;
        }

        if (actorReactions != null) {
            if (actorReactions.stream().filter(
                    r -> r.getEventKey().equals(eventKey) && r.getReactionReputationKey().equals(reactionRecordKey))
                    .findAny().orElse(null) != null) {
                Messenger.sendMessage(sender, messageProvider.get("messages.errors.actorprofile.reaction-exists"), null,
                        messageProvider.get("messages.prefix"));
            }
        } else {
            actorReactions = new ArrayList<>();
        }

        EventReaction reaction = new EventReaction(eventKey, reactionRecordKey, amount);
        actorReactions.add(reaction);

        profile.setEventReactions(actorReactions);

        if (plugin.getActorProfileManager().addOrUpdateActorProfile(profile)) {
            var cmd = "upa actorprofile edit " + actor.getName();
            Bukkit.dispatchCommand(sender, cmd);
        } else {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.db-save-error"),
                    null, messageProvider.get("messages.prefix"));
        }
    }

}
