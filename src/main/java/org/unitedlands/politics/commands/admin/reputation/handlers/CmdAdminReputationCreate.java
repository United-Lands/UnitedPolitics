package org.unitedlands.politics.commands.admin.reputation.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.classes.configs.RecordDefinitionConfig;
import org.unitedlands.politics.commands.admin.reputation.CmdAdminReputation;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.models.ReputationScoreEntry;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.utils.United;

@UnitedSubCommand(
    parent = CmdAdminReputation.class, 
    name = "add", 
    usage = "/upa reputation create <observer> <subject> <key> <amount>",
    catchAll = true
)
public class CmdAdminReputationCreate implements UnitedCommandExecutor {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 1 || args.length == 2) {
            var names1 = UnitedPolitics.instance().getGeopolWrapper().getNations().stream().map(INationWrapper::getName)
                    .collect(Collectors.toList());
            var names2 = UnitedPolitics.instance().getGeopolWrapper().getTowns().stream().map(ITownWrapper::getName)
                    .collect(Collectors.toList());
            names1.addAll(names2);
            return names1;
        } else if (args.length == 3) {
            return RecordDefinitionConfig.get().recordDefinitions().keys().stream().toList();
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sendUsage(sender);
            return;
        }

        IGeopolObjectWrapper observer = GeopolUtils.findGeopolObject(args[0]);
        if (observer == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[0]);
            return;
        }

        IGeopolObjectWrapper subject = GeopolUtils.findGeopolObject(args[1]);
        if (subject == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[1]);
            return;
        }

        var key = args[2];

        double modifier = 0;
        try {
            modifier = Double.parseDouble(args[3]);
        } catch (Exception ex) {
            United.messenger().send(sender, "errors.general.number-format-error", args[3]);
            return;
        }

        ReputationScoreEntry existingEntry = ReputationManager.instance()
                .getReputationScoreEntryWithKey(observer.getUUID(), subject.getUUID(), key);
        if (existingEntry != null) {
            United.messenger().send(sender, "errors.reputation.existing-record");
            return;
        }

        var entry = ReputationManager.instance().getOrCreateReputationScoreEntry(observer.getUUID(), subject.getUUID(),
                key);
        entry.setModifier(modifier);

        if (ReputationManager.instance().addOrUpdateReputationScoreEntry(entry)) {
            United.messenger().send(sender, "success.reputation.create",
                    args[0], args[1], args[2], args[3]);
        } else {
            United.messenger().send(sender, "errors.general.db-save-error");
        }
    }

}
