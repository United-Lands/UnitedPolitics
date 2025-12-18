package org.unitedlands.politics.commands.admin.actorprofile.handlers.rivals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.utils.Messenger;

public class AdminAddRivalCommand extends BaseCommandHandler<UnitedPolitics> {

    public AdminAddRivalCommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 2) {
            var names1 = plugin.getGeopolWrapper().getNations().stream().map(INationWrapper::getName)
                    .collect(Collectors.toList());
            var names2 = plugin.getGeopolWrapper().getTowns().stream().map(ITownWrapper::getName)
                    .collect(Collectors.toList());
            names1.addAll(names2);
            return names1;
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            Messenger.sendMessage(sender, messageProvider.getList("messages.usages.rival.add"), null,
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

        IGeopolObjectWrapper rival = GeopolUtils.findGeopolObject(args[1]);
        if (rival == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.geopol-obj-not-found"),
                    Map.of("obj-name", args[1]), messageProvider.get("messages.prefix"));
            return;
        }

        var actorRivals = profile.getRivals();
        if (actorRivals != null) {
            if (actorRivals.stream().filter(r -> r.equals(rival.getUUID())).findAny().orElse(null) != null) {
                Messenger.sendMessage(sender, messageProvider.get("messages.errors.actorprofile.rival-exists"), null, messageProvider.get("messages.prefix"));
            }
        } else {
            actorRivals = new HashSet<>();
        }

        actorRivals.add(rival.getUUID());
        profile.setRivals(actorRivals);

        if (plugin.getActorProfileManager().addOrUpdateActorProfile(profile)) {
            var cmd = "upa actorprofile edit " + actor.getName();
            Bukkit.dispatchCommand(sender, cmd);
        } else {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.db-save-error"),
                    null, messageProvider.get("messages.prefix"));
        }
    }

}
