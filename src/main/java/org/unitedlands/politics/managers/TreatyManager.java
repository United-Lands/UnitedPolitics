package org.unitedlands.politics.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.bukkit.entity.Player;
import org.unitedlands.politics.classes.configs.UnitedPoliticsConfig;
import org.unitedlands.politics.models.Treaty;
import org.unitedlands.politics.models.TreatyType;
import org.unitedlands.unitedlands.classes.BankRecordTransactionType;
import org.unitedlands.unitedlands.classes.Country;
import org.unitedlands.unitedlands.classes.GeopolObject;
import org.unitedlands.unitedlands.managers.UnitedLandsEconomyManager;
import org.unitedlands.utils.United;

public class TreatyManager {

    private static TreatyManager instance;

    public static TreatyManager instance() {
        return instance;
    }

    private final DatabaseManager databaseManager;

    private Map<UUID, Treaty> treaties = new HashMap<>();

    public TreatyManager(DatabaseManager databaseManager) {
        instance = this;
        this.databaseManager = databaseManager;
    }

    // Treaties

    public void loadTreaties() {

        var service = databaseManager.getTreatyService();
        try {
            var entries = service.getAllAsync().get();
            Map<UUID, Treaty> loadedTreaties = new HashMap<>();
            for (var entry : entries) {
                loadedTreaties.put(entry.getUuid(), entry);
            }
            treaties = loadedTreaties;
            United.logger().info("Loaded " + entries.size() + " treaties to memory.", "UnitedPolitics");
        } catch (InterruptedException | ExecutionException e) {
            United.logger().error("Failed to load treaties: " + e.getMessage(), "UnitedPolitics");
        }
    }

    public void addTreaty(Treaty treaty) {
        treaties.put(treaty.getUuid(), treaty);
        databaseManager.getTreatyService().createAsync(treaty);
    }

    public void updateTreaty(Treaty treaty) {
        treaties.put(treaty.getUuid(), treaty);
        databaseManager.getTreatyService().updateAsync(treaty);
    }

    public Treaty getTreaty(UUID uuid) {
        return treaties.get(uuid);
    }

    public Treaty getTreaty(String name) {
        return treaties.values().stream().filter(t -> t.getName().equals(name)).findFirst().orElse(null);
    }

    public Collection<Treaty> getTreaties() {
        return treaties.values();
    }

    public List<String> getTreatyNames() {
        return treaties.values().stream().map(t -> t.getName()).toList();
    }

    public List<Treaty> getTreaties(Country country) {
        return treaties.values().stream().filter(t -> t.getMembers().contains(country)).toList();
    }

    public List<Treaty> getTreaties(Country country, TreatyType type) {
        return treaties.values().stream().filter(t -> t.getType() == type && t.getMembers().contains(country)).toList();
    }

    public void removeTreaty(Treaty treaty) {
        treaties.remove(treaty.getUuid());
        databaseManager.getTreatyService().deleteAsync(treaty);
    }

    public void removeEverythingFor(Country country) {
        var treaties = getTreaties(country);

        List<Treaty> treatiesToRemove = new ArrayList<>();
        for (var treaty : treaties) {
            if (treaty.hasMember(country)) {
                treaty.removeMember(country);
                if (treaty.getMembers().size() <= 1) {
                    treatiesToRemove.add(treaty);
                } else {
                    updateTreaty(treaty);
                }
            }
        }

        for (var treatyToRemove : treatiesToRemove) {
            removeTreaty(treatyToRemove);
        }
    }

    public boolean haveAnyTreaty(Country first, Country second) {
        return treaties.values().stream().anyMatch(t -> t.hasMember(first) && t.hasMember(second));
    }


    public boolean haveTreaty(Country first, Country second, TreatyType type) {
        return treaties.values().stream().anyMatch(t -> t.hasMember(first) && t.hasMember(second) && t.getType() == type);
    }


    // Mechanics

    public static boolean isValidPaymentAmount(double amount) {
        return Double.isFinite(amount) && amount > 0;
    }

    public boolean payTribute(GeopolObject payer, GeopolObject receiver, double amount, Player player) {

        if (!isValidPaymentAmount(amount)) {
            United.logger().warning("Rejected invalid tribute amount " + amount + " from " + payer.getName(), "UnitedPolitics");
            return false;
        }

        if (payer.getUuid().equals(receiver.getUuid())) {
            United.logger().warning("Rejected tribute from " + payer.getName() + " to itself.", "UnitedPolitics");
            return false;
        }

        var sourceBalance = payer.getBalance();
        if (sourceBalance < amount) {
            United.logger().info("Bank account of " + payer.getName() + " too low.", "UnitedPolitics");
            return false;
        }

        var tributeConfig = UnitedPoliticsConfig.get().actions().tribute();
        String moneyLogReasonSource = tributeConfig.moneyLogReasonSource();
        String moneyLogReasonTarget = tributeConfig.moneyLogReasonTarget();

        var economy = UnitedLandsEconomyManager.instance();

        if (!withdraw(payer, amount, moneyLogReasonSource + receiver.getName())) {
            United.logger().warning("Failed to withdraw tribute from account of " + payer.getName(), "UnitedPolitics");
            return false;
        }

        boolean deposited;
        try {
            deposited = economy.deposit(receiver.getUuid(), amount, BankRecordTransactionType.DIPLOMACY,
                    moneyLogReasonTarget + payer.getName());
        } catch (Exception ex) {
            deposited = false;
        }

        if (!deposited) {
            United.logger().error("Failed to deposit tribute to account of " + receiver.getName()
                    + ", refunding " + payer.getName(), "UnitedPolitics");
            refund(payer, amount, moneyLogReasonSource + receiver.getName());
            return false;
        }

        double repPerUnit = tributeConfig.reputationPerMoneyUnit();
        double modifier = repPerUnit * amount;

        ReputationManager.instance().handleReputationChange(receiver, payer, modifier, "paid-tribute", player, false);

        return true;
    }

    public boolean payPropaganda(GeopolObject payer, GeopolObject target, double amount, Player player) {

        if (!isValidPaymentAmount(amount)) {
            United.logger().warning("Rejected invalid propaganda amount " + amount + " from " + payer.getName(), "UnitedPolitics");
            return false;
        }

        if (payer.getUuid().equals(target.getUuid())) {
            United.logger().warning("Rejected propaganda from " + payer.getName() + " against itself.", "UnitedPolitics");
            return false;
        }

        var sourceBalance = payer.getBalance();
        if (sourceBalance < amount) {
            United.logger().info("Bank account of " + payer.getName() + " too low.", "UnitedPolitics");
            return false;
        }

        String moneyLogReasonSource = UnitedPoliticsConfig.get().actions().propaganda().moneyLogReasonSource();

        if (!withdraw(payer, amount, moneyLogReasonSource + target.getName())) {
            United.logger().warning("Failed to withdraw propaganda investment from account of " + payer.getName(), "UnitedPolitics");
            return false;
        }

        double repPerUnit = UnitedPoliticsConfig.get().actions().propaganda().reputationPerMoneyUnit();
        double modifier = repPerUnit * amount;

        ReputationManager.instance().handleReputationChange(payer, target, modifier, "paid-propaganda", player, false);

        return true;
    }

    private boolean withdraw(GeopolObject payer, double amount, String reason) {
        try {
            return UnitedLandsEconomyManager.instance().withdraw(payer.getUuid(), amount,
                    BankRecordTransactionType.DIPLOMACY, reason);
        } catch (Exception ex) {
            return false;
        }
    }

    private void refund(GeopolObject payer, double amount, String reason) {
        boolean refunded;
        try {
            refunded = UnitedLandsEconomyManager.instance().deposit(payer.getUuid(), amount,
                    BankRecordTransactionType.DIPLOMACY, "Refund: " + reason);
        } catch (Exception ex) {
            refunded = false;
        }
        if (!refunded) {
            United.logger().error("Failed to refund " + amount + " to account of " + payer.getName()
                    + ", manual correction required.", "UnitedPolitics");
        }
    }
}
