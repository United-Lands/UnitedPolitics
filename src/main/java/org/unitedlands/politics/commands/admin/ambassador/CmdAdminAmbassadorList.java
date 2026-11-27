package org.unitedlands.politics.commands.admin.ambassador;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

@UnitedSubCommand (
    parent = CmdAdminAmbassador.class,
    name = "list",
    usage = "/upa ambassador list <country>"
)
public class CmdAdminAmbassadorList implements UnitedCommandExecutor {

    
    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        switch (args.length) 
        {
            case 1:
                return UnitedLandsDataManager.instance().getCountryNames();
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

        var country = UnitedLandsDataManager.instance().getCountry(args[0]);
        if (country == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[0]);
            return;
        }

       

        var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(country);

        United.messenger().send(sender, "ambassador.list-header", country.getDisplayName());

        if (record.getAmbassadors().size() == 0) {
                United.messenger().send(sender, "ambassador.empty");
        } else {
            for (var c : record.getAmbassadors().entrySet()) {
                United.messenger().send(sender, "ambassador.entry", c.getKey().getName(), c.getValue().getDisplayName());
            }
        }

    }

}
