package org.unitedlands.politics.commands.admin.embargo;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.unitedlands.classes.Country;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

@UnitedSubCommand (
    parent = CmdAdminEmbargo.class,
    name = "remove",
    usage = "/upa embargo remove <country> <target_country>",
    catchAll = true
)
public class CmdAdminEmbargoRemove implements UnitedCommandExecutor {

    

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
                    return record.getEmbargoes().stream().map(Country::getName).toList();
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

        var targetCountry = UnitedLandsDataManager.instance().getCountry(args[1]);
        if (targetCountry == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[1]);
            return;
        }

        if (country.equals(targetCountry)) {
            United.messenger().send(sender, "errors.embargo.same-country");
            return;
        }

        if (!DiplomacyManager.instance().embargoExists(country, targetCountry)) {
            United.messenger().send(sender, "errors.embargo.not-embargoed");
            return;
        }

        var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(country);
        record.removeEmbargo(targetCountry);
        record.save();

        United.messenger().send(sender, "success.embargo.removed", targetCountry.getDisplayName(), country.getDisplayName());
    }

}
