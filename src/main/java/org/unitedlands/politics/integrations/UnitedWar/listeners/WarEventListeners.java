package org.unitedlands.politics.integrations.UnitedWar.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.classes.TreatyType;
import org.unitedlands.politics.classes.configs.IntegrationsConfig;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.managers.TreatyManager;
import org.unitedlands.politics.utils.ColorFormatter;
import org.unitedlands.politics.wrappers.UnitedLands.UnitedLandsNationWrapper;
import org.unitedlands.unitedlands.classes.Country;
import org.unitedlands.utils.United;
import org.unitedlands.wars.classes.war.WarFactionRole;
import org.unitedlands.wars.classes.wargoal.WarGoal;
import org.unitedlands.wars.events.WarGoalValidationEvent;
import org.unitedlands.wars.events.WarPreJoinEvent;
import org.unitedlands.wars.events.WarPreRegisterEvent;
import org.unitedlands.wars.events.WarRegisteredEvent;

public class WarEventListeners implements Listener {

    @EventHandler
    public void onWarGoalValidation(WarGoalValidationEvent event) {

        // If the war is between countries, inspect treaties

        if (isCountryConflict(event.getWarGoal())) {

            United.logger().debug("isCountryConflict");

            var attacker = new UnitedLandsNationWrapper((Country) event.getAttacker());
            var defender = new UnitedLandsNationWrapper((Country) event.getAttacker());

            if (TreatyManager.instance().haveTreaty(attacker, defender, TreatyType.ALLIANCE)) {
                // TODO: Move strings to config
                event.setValidationMessage("<yellow>You are in an alliance with the target country.</yellow>");
                event.setValid(false);
                return;
            }
            if (TreatyManager.instance().haveTreaty(attacker, defender, TreatyType.NON_AGGRESSION)) {
                // TODO: Move strings to config
                event.setValidationMessage(
                        "<yellow>You have a binding non-aggression-pact with the target country.</yellow>");
                event.setValid(false);
                return;
            }
        }

        // In all cases, see if attacker's opinion of the target isn't too high

        United.logger().debug("defender: " + event.getDefender().getName());

        // For conquest, claim dispute and revolt, the target for the opinion check
        // needs to be the target region's owner (not the region itself)
        // GeopolObject reputationTarget = event.getDefender();
        // switch (event.getWarGoal().getId()) {
        //     case "conquest":
        //     case "claim-dispute":
        //     case "revolt":
        //         reputationTarget = region.getCountry();
        //         break;
        //     default:
        //         break;
        // }

        var opinion = ReputationManager.instance().getTotalReputationScore(event.getAttacker().getUuid(),
                event.getDefender().getUuid());
        var threshold = IntegrationsConfig.get().unitedWars().warOpinionThreshold();

        United.logger().debug("opinion " + opinion);
        United.logger().debug("threshold " + threshold);

        if (opinion >= threshold) {
            // TODO: Move strings to config
            event.setValidationMessage(
                    "<yellow>Your population's opinion of " + event.getDefender().getCleanName() + " is "
                            + ColorFormatter.getAmountColored(opinion) + ". You can't declare war above an opinion of "
                            + ColorFormatter.getAmountColored(threshold) + ". Find ways to lower it first.</yellow>");
            event.setValid(false);
            return;
        }

    }

    @EventHandler
    public void onWarPreRegister(WarPreRegisterEvent event) {

        // On war pre-registration, UnitedPolitics is responsible for
        // automatically adding allies to the defender faction

        var war = event.getWar();

        // Ignore everything that isn't a country-vs-country conflict
        if (!isCountryConflict(war.getWarGoal()))
            return;

        var defenderFaction = war.getWarFaction(WarFactionRole.DEFENDER);
        // If there is no clear defender faction, ignore (e.g. in claim disputes)
        if (defenderFaction == null) {
            return;
        }
        var defenderCountry = UnitedPolitics.instance().getGeopolWrapper()
                .getNation(defenderFaction.getFactionLeaderId());
        if (defenderCountry == null) {
            return;
        }

        var allianceTreaties = TreatyManager.instance().getTreaties(defenderCountry, TreatyType.ALLIANCE);
        for (var allianceTreaty : allianceTreaties) {
            for (var member : allianceTreaty.getMembers()) {
                if (member.equals(defenderCountry))
                    continue;

                United.logger()
                        .info("Auto-adding ally " + member.getName() + " to faction " + defenderFaction.getName());
                defenderFaction.addCountry(((UnitedLandsNationWrapper) member).getCountry());
            }
        }
    }

    private boolean isCountryConflict(WarGoal warGoal) {
        switch (warGoal.getId()) {
            case "conquest":
            case "claim-dispute":
                return true;
            default:
                return false;
        }
    }

    @EventHandler
    public void onWarRegistered(WarRegisteredEvent event) {
        // var config = plugin.getConfig();
        // var geopolWrapper = UnitedPolitics.instance().getGeopolWrapper();

        // ITownWrapper attacker = geopolWrapper.getTown(event.getDeclaringTownId());
        // INationWrapper attackerNation = attacker.getNation();
        // List<ITownWrapper> attackerNationTowns = new ArrayList<>();
        // if (event.isNationWar()) {
        // if (attackerNation != null) {
        // attackerNationTowns.addAll(attacker.getNation().getTowns());
        // }
        // }

        // var defender = geopolWrapper.getTown(event.getTargetTownId());
        // var defenderNation = defender.getNation();

        // // ***********************
        // // Warred Us Handling
        // // ***********************

        // if (config.getBoolean("settings.uw-warred-us.enabled", false)) {

        // var penalty = config.getDouble("settings.uw-warred-us.amount", -200);

        // if (event.isNationWar()) {

        // if (defenderNation != null) {
        // if (!attackerNationTowns.isEmpty()) {
        // for (var attackerTown : attackerNationTowns) {
        // ReputationManager.instance().handleReputationChange(defenderNation,
        // attackerTown, penalty,
        // "uw-warred-us", null, true);
        // }
        // ReputationManager.instance().handleReputationChange(defenderNation,
        // attackerNation, penalty,
        // "uw-warred-us", null, true);
        // } else {
        // ReputationManager.instance().handleReputationChange(defenderNation, attacker,
        // penalty,
        // "uw-warred-us", null, true);
        // }
        // } else {
        // if (!attackerNationTowns.isEmpty()) {
        // for (var attackerTown : attackerNationTowns) {
        // ReputationManager.instance().handleReputationChange(defender, attackerTown,
        // penalty,
        // "uw-warred-us", null, true);
        // }
        // ReputationManager.instance().handleReputationChange(defender, attackerNation,
        // penalty,
        // "uw-warred-us", null, true);
        // } else {
        // ReputationManager.instance().handleReputationChange(defender, attacker,
        // penalty,
        // "uw-warred-us", null,
        // true);
        // }
        // }
        // } else {
        // ReputationManager.instance().handleReputationChange(defender, attacker,
        // penalty, "uw-warred-us", null,
        // true);
        // }

        // }

        // // ***********************
        // // Warred Friend handling
        // // ***********************

        // if (config.getBoolean("settings.uw-warred-friend.enabled", false)) {

        // var friendsThreshold =
        // config.getDouble("settings.uw-warred-friend.threshold", 100);
        // var penalty = config.getDouble("settings.uw-warred-friend.amount", -100);

        // var towns = UnitedPolitics.instance().getGeopolWrapper().getTowns();
        // for (var town : towns) {
        // var score =
        // ReputationManager.instance().getTotalReputationScore(town.getUUID(),
        // defender.getUUID());
        // if (score >= friendsThreshold) {
        // if (attackerNationTowns.isEmpty()) {
        // ReputationManager.instance().handleReputationChange(town, attacker, penalty,
        // "uw-warred-friend",
        // null, false);
        // } else {
        // for (var attackerTown : attackerNationTowns) {
        // ReputationManager.instance().handleReputationChange(town, attackerTown,
        // penalty,
        // "uw-warred-friend", null, false);

        // }
        // }
        // }
        // }

        // var nations = UnitedPolitics.instance().getGeopolWrapper().getNations();
        // for (var nation : nations) {
        // var score =
        // ReputationManager.instance().getTotalReputationScore(nation.getUUID(),
        // defender.getUUID());
        // if (score >= friendsThreshold) {
        // if (attackerNationTowns.isEmpty()) {
        // ReputationManager.instance().handleReputationChange(nation, attacker,
        // penalty,
        // "uw-warred-friend",
        // null, false);
        // } else {
        // for (var attackerTown : attackerNationTowns) {
        // ReputationManager.instance().handleReputationChange(nation, attackerTown,
        // penalty,
        // "uw-warred-friend", null, false);

        // }
        // }
        // }
        // }
        // }

        // // ***********************
        // // Warred Enemy handling
        // // ***********************

        // if (config.getBoolean("settings.uw-warred-enemy.enabled", false)) {

        // var enemyThreshold = config.getDouble("settings.uw-warred-enemy.threshold",
        // -100);
        // var bonus = config.getDouble("settings.uw-warred-enemy.amount", 100);

        // var towns = UnitedPolitics.instance().getGeopolWrapper().getTowns();
        // for (var town : towns) {
        // var score =
        // ReputationManager.instance().getTotalReputationScore(town.getUUID(),
        // defender.getUUID());
        // if (score <= enemyThreshold) {
        // if (attackerNationTowns.isEmpty()) {
        // ReputationManager.instance().handleReputationChange(town, attacker, bonus,
        // "uw-enemy-friend",
        // null, false);
        // } else {
        // for (var attackerTown : attackerNationTowns) {
        // ReputationManager.instance().handleReputationChange(town, attackerTown,
        // bonus,
        // "uw-warred-enemy", null, false);

        // }
        // }
        // }
        // }

        // var nations = UnitedPolitics.instance().getGeopolWrapper().getNations();
        // for (var nation : nations) {
        // var score =
        // ReputationManager.instance().getTotalReputationScore(nation.getUUID(),
        // defender.getUUID());
        // if (score <= enemyThreshold) {
        // if (attackerNationTowns.isEmpty()) {
        // ReputationManager.instance().handleReputationChange(nation, attacker, bonus,
        // "uw-warred-enemy",
        // null, false);
        // } else {
        // for (var attackerTown : attackerNationTowns) {
        // ReputationManager.instance().handleReputationChange(nation, attackerTown,
        // bonus,
        // "uw-warred-enemy", null, false);
        // }
        // }
        // }
        // }
        // }

        // ReputationEvent reputationEvent = new ReputationEvent("WAR_DECLARED",
        // attacker.getUUID());
        // reputationEvent.callEvent();
    }

    @EventHandler
    private void onWarPreJoin(WarPreJoinEvent event) {

    }
}
