package org.unitedlands.politics.integrations.UnitedWar;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.events.ReputationEvent;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.war.events.WarDeclaredEvent;

public class WarEventListeners implements Listener {

    private final UnitedPolitics plugin;

    public WarEventListeners(UnitedPolitics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWarDeclared(WarDeclaredEvent event) {
        var config = plugin.getConfig();
        var geopolWrapper = plugin.getGeopolWrapper();

        ITownWrapper attacker = geopolWrapper.getTown(event.getDeclaringTownId());
        INationWrapper attackerNation = attacker.getNation();
        List<ITownWrapper> attackerNationTowns = new ArrayList<>();
        if (event.isNationWar()) {
            if (attackerNation != null) {
                attackerNationTowns.addAll(attacker.getNation().getTowns());
            }
        }

        var defender = geopolWrapper.getTown(event.getTargetTownId());
        var defenderNation = defender.getNation();

        // ***********************
        // Warred Us Handling
        // ***********************

        if (config.getBoolean("settings.uw-warred-us.enabled", false)) {

            var penalty = config.getDouble("settings.uw-warred-us.amount", -200);

            if (event.isNationWar()) {

                if (defenderNation != null) {
                    if (!attackerNationTowns.isEmpty()) {
                        for (var attackerTown : attackerNationTowns) {
                            plugin.getReputationManager().handleReputationChange(defenderNation, attackerTown, penalty,
                                    "uw-warred-us", null, true);
                        }
                        plugin.getReputationManager().handleReputationChange(defenderNation, attackerNation, penalty,
                                "uw-warred-us", null, true);
                    } else {
                        plugin.getReputationManager().handleReputationChange(defenderNation, attacker, penalty,
                                "uw-warred-us", null, true);
                    }
                } else {
                    if (!attackerNationTowns.isEmpty()) {
                        for (var attackerTown : attackerNationTowns) {
                            plugin.getReputationManager().handleReputationChange(defender, attackerTown, penalty,
                                    "uw-warred-us", null, true);
                        }
                        plugin.getReputationManager().handleReputationChange(defender, attackerNation, penalty,
                                "uw-warred-us", null, true);
                    } else {
                        plugin.getReputationManager().handleReputationChange(defender, attacker, penalty,
                                "uw-warred-us", null,
                                true);
                    }
                }
            } else {
                plugin.getReputationManager().handleReputationChange(defender, attacker, penalty, "uw-warred-us", null,
                        true);
            }

        }

        // ***********************
        // Warred Friend handling
        // ***********************

        if (config.getBoolean("settings.uw-warred-friend.enabled", false)) {

            var friendsThreshold = config.getDouble("settings.uw-warred-friend.threshold", 100);
            var penalty = config.getDouble("settings.uw-warred-friend.amount", -100);

            var towns = plugin.getGeopolWrapper().getTowns();
            for (var town : towns) {
                var score = plugin.getReputationManager().getTotalReputationScore(town.getUUID(), defender.getUUID());
                if (score >= friendsThreshold) {
                    if (attackerNationTowns.isEmpty()) {
                        plugin.getReputationManager().handleReputationChange(town, attacker, penalty,
                                "uw-warred-friend",
                                null, false);
                    } else {
                        for (var attackerTown : attackerNationTowns) {
                            plugin.getReputationManager().handleReputationChange(town, attackerTown, penalty,
                                    "uw-warred-friend", null, false);

                        }
                    }
                }
            }

            var nations = plugin.getGeopolWrapper().getNations();
            for (var nation : nations) {
                var score = plugin.getReputationManager().getTotalReputationScore(nation.getUUID(), defender.getUUID());
                if (score >= friendsThreshold) {
                    if (attackerNationTowns.isEmpty()) {
                        plugin.getReputationManager().handleReputationChange(nation, attacker, penalty,
                                "uw-warred-friend",
                                null, false);
                    } else {
                        for (var attackerTown : attackerNationTowns) {
                            plugin.getReputationManager().handleReputationChange(nation, attackerTown, penalty,
                                    "uw-warred-friend", null, false);

                        }
                    }
                }
            }
        }

        // ***********************
        // Warred Enemy handling
        // ***********************

        if (config.getBoolean("settings.uw-warred-enemy.enabled", false)) {

            var enemyThreshold = config.getDouble("settings.uw-warred-enemy.threshold", -100);
            var bonus = config.getDouble("settings.uw-warred-enemy.amount", 100);

            var towns = plugin.getGeopolWrapper().getTowns();
            for (var town : towns) {
                var score = plugin.getReputationManager().getTotalReputationScore(town.getUUID(), defender.getUUID());
                if (score <= enemyThreshold) {
                    if (attackerNationTowns.isEmpty()) {
                        plugin.getReputationManager().handleReputationChange(town, attacker, bonus,
                                "uw-enemy-friend",
                                null, false);
                    } else {
                        for (var attackerTown : attackerNationTowns) {
                            plugin.getReputationManager().handleReputationChange(town, attackerTown, bonus,
                                    "uw-warred-enemy", null, false);

                        }
                    }
                }
            }

            var nations = plugin.getGeopolWrapper().getNations();
            for (var nation : nations) {
                var score = plugin.getReputationManager().getTotalReputationScore(nation.getUUID(), defender.getUUID());
                if (score <= enemyThreshold) {
                    if (attackerNationTowns.isEmpty()) {
                        plugin.getReputationManager().handleReputationChange(nation, attacker, bonus,
                                "uw-warred-enemy",
                                null, false);
                    } else {
                        for (var attackerTown : attackerNationTowns) {
                            plugin.getReputationManager().handleReputationChange(nation, attackerTown, bonus,
                                    "uw-warred-enemy", null, false);
                        }
                    }
                }
            }
        }

        ReputationEvent reputationEvent = new ReputationEvent("WAR_DECLARED", attacker.getUUID());
        reputationEvent.callEvent();
    }
}
