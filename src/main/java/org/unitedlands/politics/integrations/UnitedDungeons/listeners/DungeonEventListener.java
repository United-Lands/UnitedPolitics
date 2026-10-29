package org.unitedlands.politics.integrations.UnitedDungeons.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.dungeons.events.DungeonCompleteEvent;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.unitedlands.classes.GeopolObject;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;

public class DungeonEventListener implements Listener {

    private final UnitedPolitics plugin;

    public DungeonEventListener(UnitedPolitics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDungeonComplete(DungeonCompleteEvent event) {

        var dungeonId = event.getDungeon().getUuid();

        var actorProfiles = ActorProfileManager.instance().getActorProfiles();
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

            GeopolObject observer = UnitedLandsDataManager.instance().getGeopolObject(profile.getId());

            for (Player player : event.getPlayers()) {

                GeopolObject playerTown = UnitedLandsDataManager.instance().getSettlement(player);
                if (player == null)
                    continue;

                ReputationManager.instance().handleReputationChange(observer, playerTown, amount, sectionName, player, false);
            }

        }

    }

}
