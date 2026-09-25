package org.unitedlands.politics.integrations.UnitedLands.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.politics.wrappers.UnitedLands.UnitedLandsNationWrapper;
import org.unitedlands.unitedlands.classes.events.infoscreen.CountryInfoScreenEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class InfoScreenListener implements Listener {

    @EventHandler 
    public void onCountryInfoScreen(CountryInfoScreenEvent event) {

        var wrapper = new UnitedLandsNationWrapper(event.getCountry());

        var diplomacyComponent = MiniMessage.miniMessage().deserialize(
            "<gray>Diplomacy: " + wrapper.getDiplomacyScore() + "</gray>"
        );

        event.getInfoScreen().addComponent("diplomacy", diplomacyComponent);
    }

}
