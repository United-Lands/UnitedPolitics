package org.unitedlands.politics.commands.diplomacy.embargo;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.classes.commandhandlers.DiplomacyCommandHandler;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.unitedlands.classes.Country;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

@UnitedSubCommand (
    parent = CmdDipomacyEmbargo.class,
    name = "remove",
    description = "Command for removing embargoes",
    usage = "/diplomacy embargo remove <country>"
)

public class CmdDipomacyEmbargoRemove extends DiplomacyCommandHandler {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        switch (args.length) 
        {
            case 1: 
                var context = validate(sender, "politics.embargo");
                if (context != null) {
                    var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(context.country());
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

        if (args.length != 1) {
            sendUsage(sender);
            return;
        }

        var context = validate(sender, "politics.embargo");
        if (context == null)
            return;

        var targetCountry = UnitedLandsDataManager.instance().getCountry(args[0]);
        if (targetCountry == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[0]);
            return;
        }

        if (context.country().equals(targetCountry)) {
            United.messenger().send(sender, "errors.embargo.same-country");
            return;
        }
        
        if (!DiplomacyManager.instance().embargoExists(context.country(), targetCountry)) {
            United.messenger().send(sender, "errors.embargo.not-embargoed");
            return;
        }

        var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(context.country());
        record.removeEmbargo(targetCountry);
        record.save();

        United.messenger().broadcast("embargo.broadcast-lifted", context.country().getDisplayName(), targetCountry.getDisplayName());
    }

}
