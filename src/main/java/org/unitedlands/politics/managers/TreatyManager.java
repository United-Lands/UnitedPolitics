package org.unitedlands.politics.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.classes.Treaty;
import org.unitedlands.politics.classes.TreatyType;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.utils.United;

public class TreatyManager {

    private static TreatyManager instance;

    public static TreatyManager instance() {
        return instance;
    }

    private final DatabaseManager databaseManager;

    private Map<UUID, Treaty> treaties;

    public TreatyManager(DatabaseManager databaseManager) {
        instance = this;
        this.databaseManager = databaseManager;
    }

    // Treaties

    public void loadTreaties() {

        treaties = new HashMap<>();

        var service = databaseManager.getTreatyService();
        service.getAllAsync().thenAccept(entries -> {
            for (var entry : entries) {
                treaties.put(entry.getId(), entry);
            }
            United.logger().info("Loaded " + entries.size() + " treaties to memory.", "UnitedPolitics");
        });
    }

    public void addOrUpdateTreaty(Treaty treaty) {
        treaties.put(treaty.getId(), treaty);
        databaseManager.getTreatyService().createOrUpdateAsync(treaty);
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

    public List<Treaty> getTreaties(INationWrapper nation) {
        return treaties.values().stream().filter(t -> t.getMembers().contains(nation)).toList();
    }

    public List<Treaty> getTreaties(INationWrapper nation, TreatyType type) {
        return treaties.values().stream().filter(t -> t.getType() == type && t.getMembers().contains(nation)).toList();
    }

    public void removeTreaty(Treaty treaty) {
        treaties.remove(treaty.getId());
        databaseManager.getTreatyService().deleteAsync(treaty.getId());
    }

    public void removeEverythingFor(INationWrapper nation) {
        var treaties = getTreaties(nation);

        List<Treaty> treatiesToRemove = new ArrayList<>();
        for (var treaty : treaties) {
            if (treaty.hasMember(nation)) {
                treaty.removeMember(nation);
                if (treaty.getMembers().size() <= 1) {
                    treatiesToRemove.add(treaty);
                } else {
                    addOrUpdateTreaty(treaty);
                }
            }
        }

        for (var treatyToRemove : treatiesToRemove) {
            removeTreaty(treatyToRemove);
        }
    }

    // Mechanics

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
