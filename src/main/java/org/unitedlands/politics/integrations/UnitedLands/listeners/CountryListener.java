package org.unitedlands.politics.integrations.UnitedLands.listeners;

import org.bukkit.event.Listener;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.managers.TreatyManager;
import org.unitedlands.politics.wrappers.UnitedLands.UnitedLandsNationWrapper;
import org.unitedlands.unitedlands.classes.events.country.CountryPreRemoveEvent;

public class CountryListener implements Listener {

    public void onCountryPreRemove(CountryPreRemoveEvent event) {

        var country = event.getCountry();
        var countryUuid = country.getUuid();

        ReputationManager.instance().removeEverythingFor(countryUuid);
        ActorProfileManager.instance().removeEverythingFor(countryUuid);

        var nation = new UnitedLandsNationWrapper(country);
        TreatyManager.instance().removeEverythingFor(nation);        

    }
}
