package org.unitedlands.politics.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.unitedlands.politics.UnitedPolitics;

public class ServerEventListener implements Listener {

    private final UnitedPolitics plugin;

    public ServerEventListener(UnitedPolitics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        plugin.getReputationManager().loadReputationRecords();
        plugin.getActorProfileManager().loadActorProfiles();
    }
}
