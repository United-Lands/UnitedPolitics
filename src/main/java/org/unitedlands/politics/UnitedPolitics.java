package org.unitedlands.politics;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.libs.ormlite.logger.LoggerFactory;
import org.unitedlands.libs.ormlite.logger.NullLogBackend;
import org.unitedlands.politics.integrations.UnitedDungeons.listeners.DungeonEventListener;
import org.unitedlands.politics.integrations.UnitedLands.listeners.CountryListener;
import org.unitedlands.politics.integrations.UnitedTrade.listeners.TradeEventListeners;
import org.unitedlands.politics.integrations.UnitedWar.listeners.WarEventListeners;
import org.unitedlands.politics.listeners.DeathListener;
import org.unitedlands.politics.listeners.ReputationEventListener;
import org.unitedlands.politics.listeners.ServerEventListener;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.managers.DatabaseManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.managers.TreatyManager;
import org.unitedlands.politics.managers.TimeManager;
import org.unitedlands.utils.United;

public class UnitedPolitics extends JavaPlugin {

    private static UnitedPolitics instance;

    public static UnitedPolitics instance() {
        return instance;
    }

    private DatabaseManager databaseManager;
    private TimeManager timeManager;

    private boolean ulEnabled;
    private boolean unitedTradeEnabled;
    private boolean unitedWarsEnabled;
    private boolean unitedDungeonsEnabled;

    @Override
    public void onEnable() {

        LoggerFactory.setLogBackendFactory(new NullLogBackend.NullLogBackendFactory());

        instance = this;

        saveDefaultConfig();

        loadManagers();
        loadWrappers();

        loadIntegrations();

        registerEvents();
        // registerCommands();

        databaseManager.initialize();

        United.logger().info("UnitedPolitics initialized.", "UnitedPolitics");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new ServerEventListener(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new ReputationEventListener(), this);
    }

    private void loadManagers() {
        databaseManager = new DatabaseManager();
        timeManager = new TimeManager(this);

        new ReputationManager(databaseManager);
        new TreatyManager(databaseManager);
        new ActorProfileManager(databaseManager);
    }

    private void loadWrappers() {

    }

    private void loadIntegrations() {

        Plugin unitedlands = Bukkit.getPluginManager().getPlugin("UnitedLands");
        if (unitedlands != null && unitedlands.isEnabled()) {
            United.logger().info("Enabling UnitedLands integrations.", "UnitedPolitics");

            getServer().getPluginManager().registerEvents(new CountryListener(), this);

            this.ulEnabled = true;
        } 
        Plugin unitedTrade = Bukkit.getPluginManager().getPlugin("UnitedTrade");
        if (unitedTrade != null && unitedTrade.isEnabled()) {
            United.logger().info("Enabling UnitedTrade integrations.", "UnitedPolitics");
            getServer().getPluginManager().registerEvents(new TradeEventListeners(), this);
            this.unitedTradeEnabled = true;
        }

        Plugin unitedWars = Bukkit.getPluginManager().getPlugin("UnitedWars");
        if (unitedWars != null && unitedWars.isEnabled()) {
            United.logger().info("Enabling UnitedWars integrations.", "UnitedPolitics");
            getServer().getPluginManager().registerEvents(new WarEventListeners(), this);
            this.unitedWarsEnabled = true;
        }

        Plugin unitedDungeons = Bukkit.getPluginManager().getPlugin("UnitedDungeons");
        if (unitedDungeons != null && unitedDungeons.isEnabled()) {
            United.logger().info("Enabling UnitedDungeons integrations.", "UnitedPolitics");
            getServer().getPluginManager().registerEvents(new DungeonEventListener(this), this);
            this.unitedDungeonsEnabled = true;
        }

    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    // Integrations

    public boolean isULEnabled() {
        return ulEnabled;
    }

    public boolean isUnitedTradeEnabled() {
        return unitedTradeEnabled;
    }

    public boolean isUnitedWarsEnabled() {
        return unitedWarsEnabled;
    }

    public boolean isUnitedDungeonsEnabled() {
        return unitedDungeonsEnabled;
    }

}
