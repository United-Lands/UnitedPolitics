package org.unitedlands.politics.commands.admin.reputation.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.models.ReputationScoreEntry;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.utils.Messenger;

public class AdminRemoveReputationCommand extends BaseCommandHandler<UnitedPolitics> {

    public AdminRemoveReputationCommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 1 || args.length == 2) {
            var names1 = plugin.getGeopolWrapper().getNations().stream().map(INationWrapper::getName)
                    .collect(Collectors.toList());
            var names2 = plugin.getGeopolWrapper().getTowns().stream().map(ITownWrapper::getName)
                    .collect(Collectors.toList());
            names1.addAll(names2);
            return names1;
        } else if (args.length == 3) {
            IGeopolObjectWrapper subject = GeopolUtils.findGeopolObject(args[0]);
            IGeopolObjectWrapper target = GeopolUtils.findGeopolObject(args[1]);
            if (subject != null && target != null)
                return plugin.getReputationManager().getReputationKeys(subject.getUUID(),
                        target.getUUID());

        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            Messenger.sendMessage(sender, messageProvider.getList("messages.usages.reputation.remove"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        IGeopolObjectWrapper observer = GeopolUtils.findGeopolObject(args[0]);
        if (observer == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.geopol-obj-not-found"),
                    Map.of("obj-name", args[0]), messageProvider.get("messages.prefix"));
            return;
        }

        IGeopolObjectWrapper subject = GeopolUtils.findGeopolObject(args[1]);
        if (subject == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.geopol-obj-not-found"),
                    Map.of("obj-name", args[1]), messageProvider.get("messages.prefix"));
            return;
        }

        var key = args[2];

        ReputationScoreEntry entry = plugin.getReputationManager().getOrCreateReputationScoreEntry(observer.getUUID(),
                subject.getUUID(), key);
        if (entry == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.reputation.no-record"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        if (plugin.getReputationManager().removeReputationEntry(entry)) {
            Messenger.sendMessage(sender, messageProvider.get("messages.success.reputation.remove"),
                    Map.of("observer-name", args[0], "subject-name", args[1], "key", args[2]),
                    messageProvider.get("messages.prefix"));
        } else {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.db-save-error"),
                    null, messageProvider.get("messages.prefix"));
        }
    }

}
