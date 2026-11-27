package org.unitedlands.politics.commands.admin.ambassador;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.unitedlands.classes.Citizen;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

@UnitedSubCommand (
    parent = CmdAdminAmbassador.class,
    name = "remove",
    usage = "/upa ambassador remove <country> <citizen>",
    catchAll = true
)
public class CmdAdminAmbassadorRemove implements UnitedCommandExecutor {

    
    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        switch (args.length) 
        {
            case 1:
                return UnitedLandsDataManager.instance().getCountryNames();
            case 2: 
                var country = UnitedLandsDataManager.instance().getCountry(args[0]);
                if (country != null) {
                    var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(country);
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
        
        if (args.length != 2) {
            sendUsage(sender);
            return;
        }

        var country = UnitedLandsDataManager.instance().getCountry(args[0]);
        if (country == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[0]);
            return;
        }

        var citizen = UnitedLandsDataManager.instance().getCitizen(args[1]);
        if (citizen == null) {
            United.messenger().send(sender, "errors.general.citizen-not-found", args[1]);
            return;
        }

        var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(country);

        if (!record.isAmbassador(citizen)) {
            United.messenger().send(sender, "errors.ambassador.citizen-is-not-ambassador", citizen.getName());
            return;
        }

        record.removeAmbassador(citizen);
        record.save();

        var entry = ReputationManager.instance().getOrCreateReputationScoreEntry(country.getUuid(), citizen.getCountry().getUuid(), "ambassador");
        ReputationManager.instance().removeReputationEntry(entry);

        United.messenger().send(sender, "success.ambassador.removed", citizen.getName(), citizen.getCountry().getName(), country.getName());

    }

}
