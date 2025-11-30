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

public class AdminAddReputationCommand extends BaseCommandHandler<UnitedPolitics> {

    public AdminAddReputationCommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
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
        } else if (args.length == 4) {
            IGeopolObjectWrapper subject = GeopolUtils.findGeopolObject(args[0]);
            IGeopolObjectWrapper target = GeopolUtils.findGeopolObject(args[1]);
            if (subject != null && target != null)
                return plugin.getReputationManager().getReputationKeysForSubject(subject.getUUID(),
                        target.getUUID());

        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length < 4) {
            Messenger.sendMessage(sender, messageProvider.getList("messages.usages.reputation.add"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        IGeopolObjectWrapper subject = GeopolUtils.findGeopolObject(args[0]);
        if (subject == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.geopol-obj-not-found"),
                    Map.of("obj-name", args[0]), messageProvider.get("messages.prefix"));
            return;
        }

        IGeopolObjectWrapper target = GeopolUtils.findGeopolObject(args[1]);
        if (target == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.geopol-obj-not-found"),
                    Map.of("obj-name", args[1]), messageProvider.get("messages.prefix"));
            return;
        }

        double modifier = 0;
        try {
            modifier = Double.parseDouble(args[2]);
        } catch (Exception ex) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.number-format-error"),
                    Map.of("input", args[2]), messageProvider.get("messages.prefix"));
        }

        var key = args[3];

        ReputationScoreEntry entry = plugin.getReputationManager().getKeyedReputationScoreEntry(subject.getUUID(), target.getUUID(), key);
        if (entry == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.reputation.no-record"), null, messageProvider.get("messages.prefix"));
            return;
        }

        entry.setModifier(entry.getModifier() + modifier);

        if (plugin.getReputationManager().addOrUpdateReputationScoreEntry(entry)) {
            Messenger.sendMessage(sender, messageProvider.get("messages.success.reputation.add"),
                    Map.of("target-name", args[1], "subject-name", args[0], "modifier", args[2], "key", key),
                    messageProvider.get("messages.prefix"));
        } else {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.db-save-error"),
                    null, messageProvider.get("messages.prefix"));
        }
    }

}
