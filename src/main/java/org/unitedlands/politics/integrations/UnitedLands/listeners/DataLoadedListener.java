package org.unitedlands.politics.integrations.UnitedLands.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.annotations.UnitedListener;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.politics.managers.TreatyManager;
import org.unitedlands.unitedlands.classes.events.DataLoadedEvent;

@UnitedListener (
    requirePlugins = "UnitedLands"
)
public class DataLoadedListener implements Listener {

    @EventHandler
    public void onDataLoaded(DataLoadedEvent event) {
        TreatyManager.instance().clearCachedObjects();
        DiplomacyManager.instance().clearCachedObjects();
    }

}
