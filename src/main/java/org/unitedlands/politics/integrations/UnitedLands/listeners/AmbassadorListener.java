package org.unitedlands.politics.integrations.UnitedLands.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.unitedlands.classes.events.citizen.CitizenUpdatedEvent;
import org.unitedlands.unitedlands.classes.events.settlement.SettlementUpdatedEvent;

public class AmbassadorListener implements Listener {

    @EventHandler
    public void onCitizenUpdated(CitizenUpdatedEvent event) {
        var citizen = event.getCitizen();
        if (citizen == null)
            return;
        DiplomacyManager.instance().validateAmbassador(citizen);
    }

    @EventHandler
    public void onSettlementUpdated(SettlementUpdatedEvent event) {
        var settlement = event.getSettlement();
        if (settlement == null)
            return;
        for (var citizen : settlement.getCitizens()) {
            DiplomacyManager.instance().validateAmbassador(citizen);
        }
    }
}
