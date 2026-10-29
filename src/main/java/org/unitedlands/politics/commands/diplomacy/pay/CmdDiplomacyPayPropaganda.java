package org.unitedlands.politics.commands.diplomacy.pay;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.classes.commandhandlers.DiplomacyCommandHandler;
import org.unitedlands.politics.managers.TreatyManager;
import org.unitedlands.unitedlands.classes.GeopolObject;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

@UnitedSubCommand(
    parent = CmdDiplomacyPay.class, 
    name = "propaganda", 
    description = "Pay money to lower your opinion of a target", 
    usage = "/diplomacy pay propaganda <target> <amount>", 
    playerOnly = true, 
    catchAll = true
)
public class CmdDiplomacyPayPropaganda extends DiplomacyCommandHandler {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return UnitedLandsDataManager.instance().getCountryNames();
        }
        return null;
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        if (args.length != 2) {
            sendUsage(sender);
            return;
        }

        var context = validate(sender, "politics.pay.propaganda");
        if (context == null)
            return;

        GeopolObject target = UnitedLandsDataManager.instance().getGeopolObject(args[0]);
        if (target == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found");
            return;
        }

        if (target.getUuid().equals(context.country().getUuid())) {
            United.messenger().send(sender, "errors.pay.same-entity");
            return;
        }

        double amount = 0;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (Exception ex) {
            United.messenger().send(sender, "errors.general.number-format-error", args[1]);
            return;
        }

        if (context.country().getBalance() < amount) {
            United.messenger().send(sender, "errors.pay.insufficient-funds");
            return;
        }

        if (TreatyManager.instance().payPropaganda(context.country(), target, amount, context.player())) {
            United.messenger().send(context.player(), "success.pay.propaganda", target.getCleanName());
        } else {
            United.messenger().send(context.player(), "errors.general.generic");
        }

    }

}
