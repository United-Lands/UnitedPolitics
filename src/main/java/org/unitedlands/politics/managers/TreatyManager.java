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
import org.unitedlands.politics.classes.configs.UnitedPoliticsConfig;
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

    public List<Treaty> getTreaties(Country country) {
        return treaties.values().stream().filter(t -> t.getMembers().contains(country)).toList();
    }

    public List<Treaty> getTreaties(Country country, TreatyType type) {
        return treaties.values().stream().filter(t -> t.getType() == type && t.getMembers().contains(country)).toList();
    }

    public void removeTreaty(Treaty treaty) {
        treaties.remove(treaty.getId());
        databaseManager.getTreatyService().deleteAsync(treaty.getId());
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
                    addOrUpdateTreaty(treaty);
                }
            }
        }

        for (var treatyToRemove : treatiesToRemove) {
            removeTreaty(treatyToRemove);
        }
    }

    public boolean haveTreaty(Country first, Country second, TreatyType type) {
        return treaties.values().stream().anyMatch(t -> t.hasMember(first) && t.hasMember(second) && t.getType() == type);
    }


    // Mechanics

    public boolean payTribute(GeopolObject payer, GeopolObject receiver, double amount, Player player) {

        var sourceBalance = payer.getBalance();
        if (sourceBalance < amount) {
            United.logger().info("Bank account of " + payer.getName() + " too low.", "UnitedPolitics");
            return false;
        }

        var config = UnitedPolitics.instance().getConfig();
        String moneyLogReasonSource = config.getString("settings.tribute.moneyLogReasonSource", "Paid tribute to");
        String moneyLogReasonTarget = config.getString("settings.tribute.moneyLogReasonTarget", "Tribute paid by");

        try {
            UnitedLandsEconomyManager.instance().deposit(receiver.getUuid(), amount, moneyLogReasonTarget + payer.getName());
        } catch (Exception ex) {
            United.logger().info("Failed to deposit tribute to account of " + receiver.getName(), "UnitedPolitics");
            return false;
        }

        try {
            UnitedLandsEconomyManager.instance().withdraw(payer.getUuid(), amount, moneyLogReasonSource + receiver.getName());
        } catch (Exception ex) {
            United.logger().info("Failed to withdraw tribute from account of " + payer.getName(), "UnitedPolitics");
            return false;
        }

        double repPerUnit = config.getDouble("settings.tribute.reputationPerMoneyUnit", 0.001d);
        double modifier = repPerUnit * amount;

        ReputationManager.instance().handleReputationChange(receiver, payer, modifier, "paid-tribute", player, false);

        return true;
    }

    public boolean payPropaganda(GeopolObject payer, GeopolObject target, double amount, Player player) {

        var sourceBalance = payer.getBalance();
        if (sourceBalance < amount) {
            United.logger().info("Bank account of " + payer.getName() + " too low.", "UnitedPolitics");
            return false;
        }

        String moneyLogReasonSource = UnitedPoliticsConfig.get().actions().propaganda().moneyLogReasonSource();

        try {
            UnitedLandsEconomyManager.instance().withdraw(payer.getUuid(), amount, moneyLogReasonSource + target.getName());
        } catch (Exception ex) {
            United.logger().info("Failed to withdraw propaganda investment from account of " + payer.getName(), "UnitedPolitics");
            return false;
        }

        double repPerUnit = UnitedPoliticsConfig.get().actions().propaganda().reputationPerMoneyUnit();
        double modifier = repPerUnit * amount;

        ReputationManager.instance().handleReputationChange(payer, target, modifier, "paid-propaganda", player, false);

        return true;
    }
}
