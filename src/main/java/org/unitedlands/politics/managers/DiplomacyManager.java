package org.unitedlands.politics.managers;

import org.bukkit.entity.Player;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.utils.Logger;

public class DiplomacyManager {

    private final UnitedPolitics plugin;

    public DiplomacyManager(UnitedPolitics plugin) {
        this.plugin = plugin;
    }

    public boolean payTribute(IGeopolObjectWrapper payer, IGeopolObjectWrapper receiver, double amount, Player player) {

        var sourceAccount = payer.getBankAccount();
        if (sourceAccount == null) {
            Logger.log("Couldn't retrieve bank account of " + payer.getName(), "UnitedPolitics");
            return false;
        }
        if (sourceAccount.getBalance() < amount) {
            Logger.log("Bank account of " + payer.getName() + " too low.", "UnitedPolitics");
            return false;
        }

        var targetAccount = receiver.getBankAccount();
        if (targetAccount == null) {
            Logger.log("Couldn't retrieve bank account of " + receiver.getName(), "UnitedPolitics");
            return false;
        }

        var config = plugin.getConfig();
        String moneyLogReasonSource = config.getString("settings.tribute.moneyLogReasonSource", "Paid tribute to");
        String moneyLogReasonTarget = config.getString("settings.tribute.moneyLogReasonTarget", "Tribute paid by");

        try {
            targetAccount.addMoney(amount, moneyLogReasonTarget + payer.getName());
        } catch (Exception ex) {
            Logger.logError("Failed to deposit tribute to account of " + receiver.getName(), "UnitedPolitics");
            return false;
        }

        try {
            sourceAccount.removeMoney(amount, moneyLogReasonSource + receiver.getName());
        } catch (Exception ex) {
            Logger.logError("Failed to withdraw tribute from account of " + payer.getName(), "UnitedPolitics");
            return false;
        }

        double repPerUnit = config.getDouble("settings.tribute.reputationPerMoneyUnit", 0.001d);
        double modifier = repPerUnit * amount;

        plugin.getReputationManager().handleReputationChange(receiver, payer, modifier, "paid-tribute", player);

        return true;
    }

}
