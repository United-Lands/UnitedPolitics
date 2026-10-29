package org.unitedlands.politics.commands.admin.reputation.handlers;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.commands.admin.reputation.CmdAdminReputation;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.models.ReputationScoreEntry;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.unitedlands.classes.GeopolObject;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

@UnitedSubCommand(
    parent = CmdAdminReputation.class,
    name = "remove", 
    usage = "/upa reputation remove <observer> <subject> <key>",
    catchAll = true
)
public class CmdAdminReputationRemove implements UnitedCommandExecutor {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 1 || args.length == 2) {
            var names1 = UnitedLandsDataManager.instance().getCountryNames();
            var names2 = UnitedLandsDataManager.instance().getSettlementNames();
            names1.addAll(names2);
            return names1;
        } else if (args.length == 3) {
            GeopolObject subject = UnitedLandsDataManager.instance().getGeopolObject(args[0]);
            GeopolObject target = UnitedLandsDataManager.instance().getGeopolObject(args[1]);
            if (subject != null && target != null)
                return ReputationManager.instance().getReputationKeys(subject.getUuid(),
                        target.getUuid());

        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender);
            return;
        }

        GeopolObject observer = UnitedLandsDataManager.instance().getGeopolObject(args[0]);
        if (observer == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[0]);
            return;
        }

        GeopolObject subject = UnitedLandsDataManager.instance().getGeopolObject(args[1]);
        if (subject == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[1]);
            return;
        }

        var key = args[2];

        ReputationScoreEntry entry = ReputationManager.instance().getOrCreateReputationScoreEntry(observer.getUuid(),
                subject.getUuid(), key);
        if (entry == null) {
            United.messenger().send(sender, "errors.reputation.no-record");
            return;
        }

        if (ReputationManager.instance().removeReputationEntry(entry)) {
            United.messenger().send(sender, "success.reputation.remove",
                    args[0], args[1], args[2]);
        } else {
            United.messenger().send(sender, "errors.general.db-save-error");
        }
    }

}
