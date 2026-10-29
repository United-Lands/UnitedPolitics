package org.unitedlands.politics.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.unitedlands.classes.Settlement;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        var killer = event.getDamageSource().getCausingEntity();
        if (killer == null)
            return;

        if (killer instanceof Player killingPlayer) {
            if (UnitedPolitics.instance().getConfig().getBoolean("settings.killed.enabled", false))
                return;

            var blacklist = UnitedPolitics.instance().getConfig().getStringList("settings.killed.blacklisted-worlds");
            if (blacklist != null && !blacklist.isEmpty()) {
                var deathWorld = killer.getLocation().getWorld().getName();
                if (blacklist.contains(deathWorld))
                    return;
            }

            var victim = (Player) event.getPlayer();

            Settlement victimTown = UnitedLandsDataManager.instance().getSettlement(victim);
            if (victimTown == null)
                return;

            Settlement killerTown =  UnitedLandsDataManager.instance().getSettlement(killingPlayer);
            if (killerTown == null)
                return;

            if (victimTown.getUuid().equals(killerTown.getUuid()))
                return;

            if (victimTown.getCountry() != null && killerTown.getCountry() != null) {
                if (victimTown.getCountry().getUuid().equals(killerTown.getCountry().getUuid()))
                    return;
            }

            var amount = UnitedPolitics.instance().getConfig().getDouble("settings.killed.amount");
            ReputationManager.instance().handleReputationChange(victimTown, killerTown, amount, "killed-member",
                    killingPlayer, true);
        }
    }

}
