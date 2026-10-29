package org.unitedlands.politics.commands.diplomacy.pay;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.classes.commandhandlers.DiplomacyCommandHandler;
import org.unitedlands.politics.managers.TreatyManager;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.utils.United;

@UnitedSubCommand(
    parent = CmdDiplomacyPay.class, 
    name = "tribute", 
    description = "Pay money to raise opinion of the target", 
    usage = "/diplomacy pay tribute <target> <amount>", 
    playerOnly = true, 
    catchAll = true
)
public class CmdDiplomacyPayTribute extends DiplomacyCommandHandler {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return UnitedPolitics.instance().getGeopolWrapper().getNationNames();
        }
        return null;
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        if (args.length != 2) {
            sendUsage(sender);
            return;
        }

        var context = validate(sender, "politics.pay.tribute");
        if (context == null)
            return;

        IGeopolObjectWrapper target = GeopolUtils.findGeopolObject(args[0]);
        if (target == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found");
            return;
        }

        if (target.getUUID().equals(context.country().getUUID())) {
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

        var account = context.country().getBankAccount();
        if (account.getBalance() < amount) {
            United.messenger().send(sender, "errors.pay.insufficient-funds");
            return;
        }

        if (TreatyManager.instance().payTribute(context.country(), target, amount, context.player())) {
            United.messenger().send(context.player(), "success.pay.tribute", target.getCleanName());
        } else {
            United.messenger().send(context.player(), "errors.general.generic");
        }

    }

}
