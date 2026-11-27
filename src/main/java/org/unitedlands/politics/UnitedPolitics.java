package org.unitedlands.politics;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.libs.ormlite.logger.LoggerFactory;
import org.unitedlands.libs.ormlite.logger.NullLogBackend;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.managers.DatabaseManager;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.managers.TreatyManager;
import org.unitedlands.utils.United;

public class UnitedPolitics extends JavaPlugin {

    private static UnitedPolitics instance;

    public static UnitedPolitics instance() {
        return instance;
    }

    private DatabaseManager databaseManager;

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

        if (!databaseManager.initialize()) {
            United.logger().error("Database unavailable, disabling UnitedPolitics.", "UnitedPolitics");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        loadIntegrations();

        // registerCommands();

        United.logger().info("UnitedPolitics initialized.", "UnitedPolitics");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null)
            databaseManager.close();
    }

    private void loadManagers() {
        databaseManager = new DatabaseManager();

        new ReputationManager(databaseManager);
        new TreatyManager(databaseManager);
        new ActorProfileManager(databaseManager);
        new DiplomacyManager(databaseManager);
    }

    private void loadIntegrations() {

        Plugin unitedlands = Bukkit.getPluginManager().getPlugin("UnitedLands");
        if (unitedlands != null && unitedlands.isEnabled()) {
            United.logger().info("Enabling UnitedLands integrations.", "UnitedPolitics");
            this.ulEnabled = true;
        } 
        Plugin unitedTrade = Bukkit.getPluginManager().getPlugin("UnitedTrade");
        if (unitedTrade != null && unitedTrade.isEnabled()) {
            United.logger().info("Enabling UnitedTrade integrations.", "UnitedPolitics");
            this.unitedTradeEnabled = true;
        }

        Plugin unitedWars = Bukkit.getPluginManager().getPlugin("UnitedWars");
        if (unitedWars != null && unitedWars.isEnabled()) {
            United.logger().info("Enabling UnitedWars integrations.", "UnitedPolitics");
            this.unitedWarsEnabled = true;
        }

        Plugin unitedDungeons = Bukkit.getPluginManager().getPlugin("UnitedDungeons");
        if (unitedDungeons != null && unitedDungeons.isEnabled()) {
            United.logger().info("Enabling UnitedDungeons integrations.", "UnitedPolitics");
            this.unitedDungeonsEnabled = true;
        }

    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
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
