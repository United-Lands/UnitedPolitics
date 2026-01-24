package org.unitedlands.politics.integrations.UnitedDungeons.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.dungeons.events.DungeonCompleteEvent;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;

public class DungeonEventListener implements Listener {

    private final UnitedPolitics plugin;

    public DungeonEventListener(UnitedPolitics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDungeonComplete(DungeonCompleteEvent event) {

        var dungeonId = event.getDungeon().getUuid();

        var actorProfiles = plugin.getActorProfileManager().getActorProfiles();
        for (var profile : actorProfiles) {

            String type = null;
            if (profile.getHostileDungeons() != null && profile.getHostileDungeons().contains(dungeonId)) {
                type = "hostile";
            } else if (profile.getFriendlyDungeons() != null && profile.getFriendlyDungeons().contains(dungeonId)) {
                type = "friendly";
            }

            if (type == null)
                continue;

            String sectionName = "ud-looted-" + type + "-dungeon";
            var amount = plugin.getConfig().getDouble("settings." + sectionName + ".amount", 0d);

            if (amount == 0d)
                continue;

            IGeopolObjectWrapper observer = GeopolUtils.findGeopolObject(profile.getId());

            for (Player player : event.getPlayers()) {

                IGeopolObjectWrapper playerTown = plugin.getGeopolWrapper().getTownByPlayer(player);
                if (player == null)
                    continue;

                plugin.getReputationManager().handleReputationChange(observer, playerTown, amount, sectionName, player, false);
            }

        }

    }

}
