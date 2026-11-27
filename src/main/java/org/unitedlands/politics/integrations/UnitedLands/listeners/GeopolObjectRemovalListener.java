package org.unitedlands.politics.integrations.UnitedLands.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.annotations.UnitedListener;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.managers.TreatyManager;
import org.unitedlands.unitedlands.classes.events.country.CountryRemovedEvent;
import org.unitedlands.unitedlands.classes.events.region.RegionRemovedEvent;
import org.unitedlands.unitedlands.classes.events.settlement.SettlementRemovedEvent;
import org.unitedlands.utils.United;

@UnitedListener (
    requirePlugins = "UnitedLands"
)
public class GeopolObjectRemovalListener implements Listener {

    @EventHandler
    public void onCountryRemoved(CountryRemovedEvent event) {
        var country = event.getCountry();

        removeGeopolObjectData(country.getUuid());
        TreatyManager.instance().removeEverythingFor(country);
        DiplomacyManager.instance().removeEverythingFor(country);

        United.logger().info("Removed political data of country " + country.getName(), "UnitedPolitics");
    }

    @EventHandler
    public void onRegionRemoved(RegionRemovedEvent event) {
        var region = event.getRegion();

        removeGeopolObjectData(region.getUuid());

        United.logger().info("Removed political data of region " + region.getName(), "UnitedPolitics");
    }

    @EventHandler
    public void onSettlementRemoved(SettlementRemovedEvent event) {
        var settlement = event.getSettlement();

        removeGeopolObjectData(settlement.getUuid());

        United.logger().info("Removed political data of settlement " + settlement.getName(), "UnitedPolitics");
    }

    private void removeGeopolObjectData(UUID objectId) {
        ReputationManager.instance().removeEverythingFor(objectId);
        ActorProfileManager.instance().removeEverythingFor(objectId);
    }
}
