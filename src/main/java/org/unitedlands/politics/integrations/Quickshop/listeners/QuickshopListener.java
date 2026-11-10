package org.unitedlands.politics.integrations.Quickshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.annotations.UnitedListener;
import org.unitedlands.politics.classes.configs.IntegrationsConfig;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.economy.ShopSuccessPurchaseEvent;

@UnitedListener (
    requirePlugins = "QuickShop-Hikari"
)
public class QuickshopListener implements Listener {

    /* Check shop interactions against embargo list */

    @EventHandler 
    private void onPurchaseComplete(ShopPurchaseEvent event) {
        if (!IntegrationsConfig.get().quickshop().enabled())
            return;

        var shopLocation = event.getShop().bukkitLocation();

        var shopTown = UnitedLandsDataManager.instance().getSettlement(shopLocation);
        if (shopTown == null)
            return;

        if (!shopTown.hasCountry())
            return;

        var player = event.getPurchaser().getBukkitPlayer();
        if (player.isEmpty())
            return;

        var citizen = UnitedLandsDataManager.instance().getCitizen(player.get());
        if (citizen == null)
            return;

        if (!citizen.hasCountry())
            return;

        var shopCountry = shopTown.getCountry();
        var playerCountry = citizen.getCountry();

        if (DiplomacyManager.instance().embargoExists(shopCountry, playerCountry)) {
            //United.messenger().send(player.get(), "embargo.active", shopCountry.getDisplayName());
            event.setCancelled(true, United.messenger().get("embargo.active", shopCountry.getDisplayName()));
        }

    }

    /* Give reputation for purchases / sales */

    @EventHandler 
    private void onPurchaseComplete(ShopSuccessPurchaseEvent event) {

        if (!IntegrationsConfig.get().quickshop().enabled())
            return;

        var shopLocation = event.getShop().bukkitLocation();

        var shopTown = UnitedLandsDataManager.instance().getSettlement(shopLocation);
        if (shopTown == null)
            return;

        var player = event.getPurchaser().getBukkitPlayer();
        if (player.isEmpty())
            return;

        var playerTown = UnitedLandsDataManager.instance().getSettlement(player.get());
        if (playerTown == null || playerTown.equals(shopTown))
            return;

        double repPerUnit = IntegrationsConfig.get().quickshop().reputationPerMoneyUnit();
        double modifier = repPerUnit * event.getBalance();

        ReputationManager.instance().handleReputationChange(shopTown, playerTown, modifier, "qs-used-shop", player.get(), IntegrationsConfig.get().quickshop().passthrough());
    }

}
