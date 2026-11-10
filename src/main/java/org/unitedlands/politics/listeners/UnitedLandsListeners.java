package org.unitedlands.politics.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.annotations.UnitedListener;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.unitedlands.classes.events.country.CountryUpkeepCalculatedEvent;
import org.unitedlands.utils.United;

@UnitedListener (
    requirePlugins = { "UnitedLands" }
)
public class UnitedLandsListeners implements Listener {
    @EventHandler 
    private void onCountryUpkeepCalculated(CountryUpkeepCalculatedEvent event) {

        var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(event.getCountry());

        United.logger().debug("event.getFinalCosts() " + event.getFinalCosts());
        United.logger().debug("record.getAmbassadors().keySet().size() " + record.getAmbassadors().keySet().size());

        var cost = event.getFinalCosts() + 2500.0 * record.getAmbassadors().keySet().size();
        United.logger().debug("cost " + cost);
        event.setFinalCosts(cost);
    }
}
