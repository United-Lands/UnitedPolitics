package org.unitedlands.politics.commands.admin.actorprofile.handlers.reactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.utils.Messenger;

public class AdminRemoveReactionCommand extends BaseCommandHandler<UnitedPolitics> {

    public AdminRemoveReactionCommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return plugin.getConfig().getStringList("event-keys");
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            Messenger.sendMessage(sender, messageProvider.getList("messages.usages.reaction.remove"), null,
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

        if (actorReactions != null) {
            var reaction = actorReactions.stream().filter(r -> r.getEventKey().equals(eventKey)).findAny().orElse(null);
            if (reaction != null) {

                actorReactions.remove(reaction);
                profile.setEventReactions(actorReactions);

                if (plugin.getActorProfileManager().addOrUpdateActorProfile(profile)) {
                    var cmd = "upa actorprofile edit " + actor.getName();
                    Bukkit.dispatchCommand(sender, cmd);
                } else {
                    Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.db-save-error"),
                            null, messageProvider.get("messages.prefix"));
                }
            } else {
                Messenger.sendMessage(sender, messageProvider.get("messages.errors.actorprofile.reaction-missing"),
                        null, messageProvider.get("messages.prefix"));
                return;
            }
        } else {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.actorprofile.reaction-missing"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

    }

}
