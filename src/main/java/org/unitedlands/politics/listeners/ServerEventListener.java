package org.unitedlands.politics.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.managers.TimeManager;

public class ServerEventListener implements Listener {

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        ReputationManager.instance().loadReputationRecords();
        ActorProfileManager.instance().loadActorProfiles();
        TimeManager.instance().scheduleNewDay();
    }
}
