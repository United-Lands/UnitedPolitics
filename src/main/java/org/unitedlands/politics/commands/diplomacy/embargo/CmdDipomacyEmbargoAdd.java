package org.unitedlands.politics.commands.diplomacy.embargo;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.classes.commandhandlers.DiplomacyCommandHandler;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.unitedlands.classes.Confirmation;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

@UnitedSubCommand (
    parent = CmdDipomacyEmbargo.class,
    name = "add",
    description = "Command for adding embargoes",
    usage = "/diplomacy embargo add <country>"
)

public class CmdDipomacyEmbargoAdd extends DiplomacyCommandHandler {

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
        
        if (DiplomacyManager.instance().embargoExists(context.country(), targetCountry)) {
            United.messenger().send(sender, "errors.embargo.exists");
            return;
        }

        var confirmation = new Confirmation("embargo");
        confirmation.setRunnable(() -> {
            var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(context.country());
            record.addEmbargo(targetCountry);
            record.save();

            United.messenger().broadcast("embargo.broadcast", context.country().getDisplayName(), targetCountry.getDisplayName());
        })
        .setTitle(United.messenger().get("embargo.confirm", targetCountry.getDisplayName()))
        .setSender(context.player())
        .setReceiver(context.player())
        .send();

    }

}
