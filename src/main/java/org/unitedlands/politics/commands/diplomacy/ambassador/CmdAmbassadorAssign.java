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
    name = "assign",
    description = "Command for assigning ambassadors",
    usage = "/diplomacy ambassador assign <country> <citizen>"
)
public class CmdAmbassadorAssign extends DiplomacyCommandHandler {

    
    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        switch (args.length) {
            case 1:
                return UnitedLandsDataManager.instance().getCountryNames();
            case 2: 
                var context = validate(sender, "politics.ambassador");
                if (context != null) {
                    return context.country().getCitizens().stream().map(Citizen::getName).toList();
                }
                break;
            default:
                break;
        };
        return null;
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        
        if (args.length != 2) {
            sendUsage(sender);
            return;
        }

        var context = validate(sender, "politics.ambassador");
        if (context == null)
            return;
        
        var targetCountry = UnitedLandsDataManager.instance().getCountry(args[0]);
        if (targetCountry == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[0]);
            return;
        }

        var citizen = UnitedLandsDataManager.instance().getCitizen(args[1]);
        if (citizen == null) {
            United.messenger().send(sender, "errors.general.citizen-not-found", args[1]);
            return;
        }

        if (citizen.getCountry() == null || !citizen.getCountry().equals(context.country())) {
            United.messenger().send(sender, "errors.ambassador.citizen-not-in-country", targetCountry.getName());
            return;
        }

        if (DiplomacyManager.instance().isAmbassador(citizen)) {
            United.messenger().send(sender, "errors.ambassador.citizen-is-ambassador", citizen.getName());
            return;
        }

        if (DiplomacyManager.instance().hasAmbassador(context.country(), targetCountry)) {
            United.messenger().send(sender, "errors.ambassador.already-has-ambassador", citizen.getCountry().getName(), targetCountry.getName());
            return;
        }

        var targetLeader = context.country().getLeader();
        if (targetLeader == null) {
            United.messenger().send(sender, "errors.ambassador.target-npc", targetCountry.getName());
            return;
        }

        if (!targetLeader.getPlayer().isOnline()) {
            United.messenger().send(sender, "errors.ambassador.target-leader-offline", targetCountry.getName());
            return;
        }

        Confirmation confirmation = new Confirmation("ambassador-sender");
        confirmation.setRunnable(() -> {

            Confirmation confirmationTarget = new Confirmation("ambassador-target");
            confirmationTarget.setRunnable(() -> {

                var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(context.country());
                record.addAmbassador(citizen, targetCountry);
                record.save();

                var entry = ReputationManager.instance().getOrCreateReputationScoreEntry(targetCountry.getUuid(), context.country().getUuid(), "ambassador");
                entry.setModifier(0.25);
                ReputationManager.instance().addOrUpdateReputationScoreEntry(entry);

                United.messenger().broadcast("ambassador.broadcast", context.country().getDisplayName(), targetCountry.getDisplayName());
                        
            })
            .setTitle(United.messenger().get("ambassador.confirm-target", context.country().getDisplayName()))
            .setSender(context.player())
            .setReceiver(targetLeader.getPlayer())
            .send();

        })
        .setTitle(United.messenger().get("ambassador.confirm-sender", targetCountry.getDisplayName()))
        .setSender(context.player())
        .setReceiver(context.player())
        .send();

    }

}
