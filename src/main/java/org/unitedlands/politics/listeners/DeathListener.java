package org.unitedlands.politics.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;

public class DeathListener implements Listener {

    private final UnitedPolitics plugin;

    public DeathListener(UnitedPolitics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        var killer = event.getDamageSource().getCausingEntity();
        if (killer == null)
            return;

        if (killer instanceof Player killingPlayer) {
            if (!plugin.getConfig().getBoolean("settings.killed.enabled", false))
                return;

            var blacklist = plugin.getConfig().getStringList("settings.killed.blacklisted-worlds");
            if (blacklist != null && !blacklist.isEmpty())
            {
                var deathWorld = killer.getLocation().getWorld().getName();
                if (blacklist.contains(deathWorld))
                    return;
            }

            var victim = (Player) event.getPlayer();

            IGeopolObjectWrapper victimTown = plugin.getGeopolWrapper().getTownByPlayer(victim);
            if (victimTown == null)
                return;

            IGeopolObjectWrapper killerTown = plugin.getGeopolWrapper().getTownByPlayer(killingPlayer);
            if (killerTown == null)
                return;

            if (victimTown.getUUID().equals(killerTown.getUUID()))
                return;

            var amount = plugin.getConfig().getDouble("settings.killed.amount");
            plugin.getReputationManager().handleReputationChange(victimTown, killerTown, amount, "killed-member",
                killingPlayer);
        }
    }

}
