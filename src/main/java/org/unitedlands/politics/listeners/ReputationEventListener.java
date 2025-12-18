package org.unitedlands.politics.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.events.ReputationEvent;
import org.unitedlands.utils.Logger;

public class ReputationEventListener implements Listener {

    private final UnitedPolitics plugin;
    
    public ReputationEventListener(UnitedPolitics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onReputationEvent(ReputationEvent event)
    {
        Logger.log("Reputation event!");
    }
}
