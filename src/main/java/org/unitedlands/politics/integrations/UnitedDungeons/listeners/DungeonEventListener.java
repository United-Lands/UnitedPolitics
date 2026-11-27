package org.unitedlands.politics.integrations.UnitedDungeons.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.dungeons.events.DungeonCompleteEvent;
import org.unitedlands.politics.classes.configs.IntegrationsConfig;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.unitedlands.classes.GeopolObject;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;

public class DungeonEventListener implements Listener {

    @EventHandler
    public void onDungeonComplete(DungeonCompleteEvent event) {

        var dungeonId = event.getDungeon().getUuid();

        var actorProfiles = ActorProfileManager.instance().getActorProfiles();
        for (var profile : actorProfiles) {

            var dungeonConfig = IntegrationsConfig.get().unitedDungeons();

            String type = null;
            double amount = 0d;
            if (profile.getHostileDungeons() != null && profile.getHostileDungeons().contains(dungeonId)) {
                type = "hostile";
                amount = dungeonConfig.lootedHostileDungeon().amount();
            } else if (profile.getFriendlyDungeons() != null && profile.getFriendlyDungeons().contains(dungeonId)) {
                type = "friendly";
                amount = dungeonConfig.lootedFriendlyDungeon().amount();
            }

            if (type == null)
                continue;

            String sectionName = "ud-looted-" + type + "-dungeon";

            if (amount == 0d)
                continue;

            GeopolObject observer = UnitedLandsDataManager.instance().getGeopolObject(profile.getUuid());

            for (Player player : event.getPlayers()) {

                GeopolObject playerTown = UnitedLandsDataManager.instance().getSettlement(player);
                if (playerTown == null)
                    continue;

                ReputationManager.instance().handleReputationChange(observer, playerTown, amount, sectionName, player, false);
            }

        }

    }

}
