package org.unitedlands.politics.commands.admin.embargo;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

@UnitedSubCommand (
    parent = CmdAdminEmbargo.class,
    name = "add",
    usage = "/upa embargo add <country> <target_country>",
    catchAll = true
)
public class CmdAdminEmbargoAdd implements UnitedCommandExecutor {

    

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return switch (args.length) 
        {
            case 1 -> UnitedLandsDataManager.instance().getCountryNames();
            case 2 -> UnitedLandsDataManager.instance().getCountryNames();
            default -> null;
        };
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

        if (DiplomacyManager.instance().embargoExists(country, targetCountry)) {
            United.messenger().send(sender, "errors.embargo.exists");
            return;
        }

        var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(country);
        record.addEmbargo(targetCountry);
        record.save();

        United.messenger().send(sender, "success.embargo.added", targetCountry.getDisplayName(), country.getDisplayName());
    }

}
