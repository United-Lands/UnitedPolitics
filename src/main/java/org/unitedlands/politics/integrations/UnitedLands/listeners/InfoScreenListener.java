package org.unitedlands.politics.integrations.UnitedLands.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.politics.classes.Treaty;
import org.unitedlands.politics.managers.TreatyManager;
import org.unitedlands.politics.wrappers.UnitedLands.UnitedLandsNationWrapper;
import org.unitedlands.unitedlands.classes.events.infoscreen.CountryInfoScreenEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class InfoScreenListener implements Listener {

    @EventHandler
    public void onCountryInfoScreen(CountryInfoScreenEvent event) {

        var nation = new UnitedLandsNationWrapper(event.getCountry());

        var diplomacyComponent = MiniMessage.miniMessage().deserialize(
                "<gray>Diplomacy: " + nation.getDiplomacyScore() + "</gray>");
        event.getInfoScreen().addComponent("diplomacy", diplomacyComponent);

        var treaties = TreatyManager.instance().getTreaties(nation);
        if (treaties.size() > 0) {
            var treatyString = "<white><b>Treaty memberships: </b></white>";
            treatyString += String.join(", ", treaties.stream().map(Treaty::getCleanName).toList());
            var treatyComponent = MiniMessage.miniMessage().deserialize(treatyString);
            event.getInfoScreen().addComponent("treaties", treatyComponent);
        }

    }

}
