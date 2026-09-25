package org.unitedlands.politics;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.politics.integrations.Towny.commands.TownyTownReputationCommand;
import org.unitedlands.politics.integrations.Towny.listeners.TownScreenListener;
import org.unitedlands.politics.integrations.UnitedDungeons.listeners.DungeonEventListener;
import org.unitedlands.politics.integrations.UnitedLands.listeners.InfoScreenListener;
import org.unitedlands.politics.integrations.UnitedTrade.listeners.TradeEventListeners;
import org.unitedlands.politics.integrations.UnitedWar.listeners.WarEventListeners;
import org.unitedlands.politics.listeners.DeathListener;
import org.unitedlands.politics.listeners.ReputationEventListener;
import org.unitedlands.politics.listeners.ServerEventListener;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.managers.DatabaseManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.politics.managers.TimeManager;
import org.unitedlands.politics.wrappers.Towny.TownyGeopolWrapper;
import org.unitedlands.politics.wrappers.UnitedLands.UnitedLandsGeopolWrapper;
import org.unitedlands.politics.wrappers.interfaces.IGeopolWrapper;
import org.unitedlands.utils.United;

import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.logger.NullLogBackend;

public class UnitedPolitics extends JavaPlugin {

    private static UnitedPolitics instance;

    public static UnitedPolitics instance() {
        return instance;
    }

    private IGeopolWrapper geopolWrapper;

    private DatabaseManager databaseManager;
    private TimeManager timeManager;

    private boolean townyEnabled;
    private boolean unitedTradeEnabled;
    private boolean unitedWarEnabled;
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

    // private void registerCommands() {
    // var adminCommands = new AdminCommands(this, messageProvider);
    // getCommand("unitedpoliticsadmin").setExecutor(adminCommands);
    // getCommand("unitedpoliticsadmin").setTabCompleter(adminCommands);

    // var opinionCommand = new OpinionCommand(instance, messageProvider);
    // getCommand("opinion").setExecutor(opinionCommand);
    // getCommand("opinion").setTabCompleter(opinionCommand);
    // }

    private void loadManagers() {
        databaseManager = new DatabaseManager();
        timeManager = new TimeManager(this);

        new ReputationManager(databaseManager);
        new DiplomacyManager(databaseManager);
        new ActorProfileManager(databaseManager);
    }

    private void loadWrappers() {
        Plugin unitedlands = Bukkit.getPluginManager().getPlugin("UnitedLands");
        if (unitedlands != null && unitedlands.isEnabled()) {
            United.logger().info("UnitedLands found, enabling wrapper.", "UnitedPolitics");
            geopolWrapper = new UnitedLandsGeopolWrapper();
        } else {
            Plugin towny = Bukkit.getPluginManager().getPlugin("Towny");
            if (towny != null && towny.isEnabled()) {
                United.logger().info("Towny found, enabling wrapper.", "UnitedPolitics");
                geopolWrapper = new TownyGeopolWrapper();
            }
        }
    }

    private void loadIntegrations() {

        Plugin unitedlands = Bukkit.getPluginManager().getPlugin("UnitedLands");
        if (unitedlands != null && unitedlands.isEnabled()) {
            United.logger().info("Enabling UnitedLands integrations.", "UnitedPolitics");

            getServer().getPluginManager().registerEvents(new InfoScreenListener(), this);

            this.townyEnabled = true;
        } else {
            Plugin towny = Bukkit.getPluginManager().getPlugin("Towny");
            if (towny != null && towny.isEnabled()) {
                United.logger().info("Enabling Towny integrations.", "UnitedPolitics");
                new TownyTownReputationCommand();
                getServer().getPluginManager().registerEvents(new TownScreenListener(), this);
                this.townyEnabled = true;
            }
        }

        Plugin unitedTrade = Bukkit.getPluginManager().getPlugin("UnitedTrade");
        if (unitedTrade != null && unitedTrade.isEnabled()) {
            United.logger().info("Enabling UnitedTrade integrations.", "UnitedPolitics");
            getServer().getPluginManager().registerEvents(new TradeEventListeners(), this);
            this.unitedTradeEnabled = true;
        }

        Plugin unitedWar = Bukkit.getPluginManager().getPlugin("UnitedWar");
        if (unitedWar != null && unitedWar.isEnabled()) {
            United.logger().info("Enabling UnitedWar integrations.", "UnitedPolitics");
            getServer().getPluginManager().registerEvents(new WarEventListeners(this), this);
            this.unitedWarEnabled = true;
        }

        Plugin unitedDungeons = Bukkit.getPluginManager().getPlugin("UnitedDungeons");
        if (unitedDungeons != null && unitedDungeons.isEnabled()) {
            United.logger().info("Enabling UnitedDungeons integrations.", "UnitedPolitics");
            getServer().getPluginManager().registerEvents(new DungeonEventListener(this), this);
            this.unitedDungeonsEnabled = true;
        }
    }

    public IGeopolWrapper getGeopolWrapper() {
        return geopolWrapper;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    // Integrations

    public boolean isTownyEnabled() {
        return townyEnabled;
    }

    public boolean isUnitedTradeEnabled() {
        return unitedTradeEnabled;
    }

    public boolean isUnitedWarEnabled() {
        return unitedWarEnabled;
    }

    public boolean isUnitedDungeonsEnabled() {
        return unitedDungeonsEnabled;
    }

}
