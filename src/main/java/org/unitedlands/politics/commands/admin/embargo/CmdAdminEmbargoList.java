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
    name = "list",
    usage = "/upa embargo list <country>",
    catchAll = true
)
public class CmdAdminEmbargoList implements UnitedCommandExecutor {

    

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return switch (args.length) 
        {
            case 1 -> UnitedLandsDataManager.instance().getCountryNames();
            default -> null;
        };
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        
        if (args.length != 1) {
            sendUsage(sender);
            return;
        }

        var country = UnitedLandsDataManager.instance().getCountry(args[0]);
        if (country == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[0]);
            return;
        }

        var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(country);
        
        United.messenger().send(sender, "embargo.list-header", country.getDisplayName());

        if (record.getEmbargoes().size() == 0) {
                United.messenger().send(sender, "embargo.empty");
        } else {
            for (var c : record.getEmbargoes()) {
                United.messenger().send(sender, "embargo.entry", c.getDisplayName());
            }
        }
    }

}
