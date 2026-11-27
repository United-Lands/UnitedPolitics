package org.unitedlands.politics.commands.diplomacy.ambassador;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.classes.commandhandlers.DiplomacyCommandHandler;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.unitedlands.classes.Citizen;
import org.unitedlands.unitedlands.classes.Confirmation;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

@UnitedSubCommand (
    parent = CmdAmbassador.class,
    name = "recall",
    description = "Command for recalling ambassadors",
    usage = "/diplomacy ambassador recall <citizen>"
)
public class CmdAmbassadorRecall extends DiplomacyCommandHandler {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        switch (args.length) 
        {
            case 1: 
                var context = validate(sender, "politics.ambassador");
                if (context != null) {
                    var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(context.country());
                    return record.getAmbassadors().keySet().stream().map(Citizen::getName).toList();
                }
                break;
            default:
                break;
        };
        return null;
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        if (args.length != 1) {
            sendUsage(sender);
            return;
        }

        var context = validate(sender, "politics.ambassador");
        if (context == null)
            return;

        var citizen = UnitedLandsDataManager.instance().getCitizen(args[0]);
        if (citizen == null) {
            United.messenger().send(sender, "errors.general.citizen-not-found", args[0]);
            return;
        }

        var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(context.country());
        if (!record.isAmbassador(citizen)) {
            United.messenger().send(sender, "errors.ambassador.citizen-is-not-ambassador", citizen.getName());
            return;
        }
        
        var targetCountry = record.getAmbassdorCountry(citizen);

        Confirmation confirmation = new Confirmation("ambassador-recalled");
        confirmation.setRunnable(() -> { 

            record.removeAmbassador(citizen);
            record.save();

            var entry = ReputationManager.instance().getOrCreateReputationScoreEntry(targetCountry.getUuid(), citizen.getCountry().getUuid(), "ambassador-recalled");
            entry.setModifier(-25);
            ReputationManager.instance().addOrUpdateReputationScoreEntry(entry);
        
            United.messenger().broadcast("ambassador.broadcast-recalled", context.country().getDisplayName(), targetCountry.getDisplayName());
        })
        .setTitle(United.messenger().get("ambassador.confirm-recall", targetCountry.getDisplayName()))
        .setSender(context.player())
        .setReceiver(context.player())
        .send();

    }

}
