package org.unitedlands.politics.managers;

import org.bukkit.entity.Player;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.utils.United;

public class DiplomacyManager {

    private static DiplomacyManager instance;

    public static DiplomacyManager instance() {
        return instance;
    }

    private final DatabaseManager databaseManager;

    public DiplomacyManager(DatabaseManager databaseManager) {
        instance = this;
        this.databaseManager = databaseManager;
    }

    public boolean payTribute(IGeopolObjectWrapper payer, IGeopolObjectWrapper receiver, double amount, Player player) {

        var sourceAccount = payer.getBankAccount();
        if (sourceAccount == null) {
            United.logger().info("Couldn't retrieve bank account of " + payer.getName(), "UnitedPolitics");
            return false;
        }
        if (sourceAccount.getBalance() < amount) {
            United.logger().info("Bank account of " + payer.getName() + " too low.", "UnitedPolitics");
            return false;
        }

        var targetAccount = receiver.getBankAccount();
        if (targetAccount == null) {
            United.logger().info("Couldn't retrieve bank account of " + receiver.getName(), "UnitedPolitics");
            return false;
        }

        var config = UnitedPolitics.instance().getConfig();
        String moneyLogReasonSource = config.getString("settings.tribute.moneyLogReasonSource", "Paid tribute to");
        String moneyLogReasonTarget = config.getString("settings.tribute.moneyLogReasonTarget", "Tribute paid by");

        try {
            targetAccount.addMoney(amount, moneyLogReasonTarget + payer.getName());
        } catch (Exception ex) {
            United.logger().info("Failed to deposit tribute to account of " + receiver.getName(), "UnitedPolitics");
            return false;
        }

        try {
            sourceAccount.removeMoney(amount, moneyLogReasonSource + receiver.getName());
        } catch (Exception ex) {
            United.logger().info("Failed to withdraw tribute from account of " + payer.getName(), "UnitedPolitics");
            return false;
        }

        double repPerUnit = config.getDouble("settings.tribute.reputationPerMoneyUnit", 0.001d);
        double modifier = repPerUnit * amount;

        ReputationManager.instance().handleReputationChange(receiver, payer, modifier, "paid-tribute", player, true);

        return true;
    }

}
