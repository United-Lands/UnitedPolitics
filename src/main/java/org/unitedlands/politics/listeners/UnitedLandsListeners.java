package org.unitedlands.politics.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.annotations.UnitedListener;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.unitedlands.classes.events.country.CountryUpkeepCalculatedEvent;

@UnitedListener (
    requirePlugins = { "UnitedLands" }
)
public class UnitedLandsListeners implements Listener {
    @EventHandler 
    private void onCountryUpkeepCalculated(CountryUpkeepCalculatedEvent event) {

        var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(event.getCountry());
        var cost = event.getFinalCosts() + 2500.0 * record.getAmbassadors().keySet().size();
        event.setFinalCosts(cost);
    }
}
