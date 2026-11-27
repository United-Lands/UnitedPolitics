package org.unitedlands.politics.integrations.UnitedLands.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.unitedlands.classes.events.NewDayEvent;

public class NewDayListener implements Listener {

    @EventHandler
    public void onNewDay(NewDayEvent event) {
        ReputationManager.instance().doReputationDecay();
    }

}
