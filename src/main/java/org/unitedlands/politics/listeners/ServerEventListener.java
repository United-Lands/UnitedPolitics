package org.unitedlands.politics.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.integrations.Quickshop.listeners.QuickshopListener;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.managers.TimeManager;
import org.unitedlands.politics.managers.TreatyManager;
import org.unitedlands.utils.United;

public class ServerEventListener implements Listener {

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        ReputationManager.instance().loadReputationRecords();
        ActorProfileManager.instance().loadActorProfiles();
        TreatyManager.instance().loadTreaties();
        TimeManager.instance().scheduleNewDay();

        Plugin quickshop = Bukkit.getPluginManager().getPlugin("QuickShop-Hikari");
        if (quickshop != null && quickshop.isEnabled()) {
            United.logger().info("Enabling QuickShop-Hikari integrations.", "UnitedPolitics");
            UnitedPolitics.instance().getServer().getPluginManager().registerEvents(new QuickshopListener(), UnitedPolitics.instance());
        }
    }
}
