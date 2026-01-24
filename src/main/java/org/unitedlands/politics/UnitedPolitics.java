package org.unitedlands.politics;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.politics.classes.MessageProvider;
import org.unitedlands.politics.commands.AdminCommands;
import org.unitedlands.politics.commands.OpinionCommand;
import org.unitedlands.politics.integrations.Towny.commands.TownyTownPayTributeCommand;
import org.unitedlands.politics.integrations.Towny.commands.TownyTownReputationCommand;
import org.unitedlands.politics.integrations.Towny.listeners.TownScreenListener;
import org.unitedlands.politics.integrations.UnitedDungeons.listeners.DungeonEventListener;
import org.unitedlands.politics.integrations.UnitedTrade.listeners.TradeEventListeners;
import org.unitedlands.politics.integrations.UnitedWar.WarEventListeners;
import org.unitedlands.politics.listeners.DeathListener;
import org.unitedlands.politics.listeners.ReputationEventListener;
import org.unitedlands.politics.listeners.ServerEventListener;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.managers.DatabaseManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.politics.wrappers.Towny.TownyGeopolWrapper;
import org.unitedlands.politics.wrappers.interfaces.IGeopolWrapper;
import org.unitedlands.utils.Logger;

public class UnitedPolitics extends JavaPlugin {

    private static UnitedPolitics instance;

    public static UnitedPolitics getInstance() {
        return instance;
    }

    private IGeopolWrapper geopolWrapper;

    private DatabaseManager databaseManager;
    private ReputationManager reputationManager;
    private DiplomacyManager diplomacyManager;
    private ActorProfileManager actorProfileManager;

    private boolean townyEnabled;
    private boolean unitedTradeEnabled;
    private boolean unitedWarEnabled;
    private boolean unitedDungeonsEnabled;



    private static MessageProvider messageProvider;

    @Override
    public void onEnable() {

        instance = this;

        saveDefaultConfig();

        messageProvider = new MessageProvider(getConfig());

        loadManagers();
        loadWrappers();

        loadIntegrations();

        registerEvents();
        registerCommands();

        databaseManager.initialize();

        Logger.log("UnitedPolitics initialized.", "UnitedPolitics");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new ServerEventListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new ReputationEventListener(this), this);
    }

    private void registerCommands() {
        var adminCommands = new AdminCommands(this, messageProvider);
        getCommand("unitedpoliticsadmin").setExecutor(adminCommands);
        getCommand("unitedpoliticsadmin").setTabCompleter(adminCommands);

        var opinionCommand = new OpinionCommand(instance, messageProvider);
        getCommand("opinion").setExecutor(opinionCommand);
        getCommand("opinion").setTabCompleter(opinionCommand);
    }

    private void loadManagers() {
        databaseManager = new DatabaseManager(this);
        reputationManager = new ReputationManager(this, messageProvider);
        diplomacyManager = new DiplomacyManager(this);
        actorProfileManager = new ActorProfileManager(this);
    }

    private void loadWrappers() {
        Plugin towny = Bukkit.getPluginManager().getPlugin("Towny");
        if (towny != null && towny.isEnabled()) {
            Logger.log("Towny found, enabling wrapper.", "UnitedPolitics");
            geopolWrapper = new TownyGeopolWrapper();
        }
    }

    private void loadIntegrations() {
        Plugin towny = Bukkit.getPluginManager().getPlugin("Towny");
        if (towny != null && towny.isEnabled()) {
            Logger.log("Enabling Towny integrations.", "UnitedPolitics");
            new TownyTownReputationCommand(this, messageProvider);
            new TownyTownPayTributeCommand(this, messageProvider);
            getServer().getPluginManager().registerEvents(new TownScreenListener(this), this);
            this.townyEnabled = true;
        }

        Plugin unitedTrade = Bukkit.getPluginManager().getPlugin("UnitedTrade");
        if (unitedTrade != null && unitedTrade.isEnabled()) {
            Logger.log("Enabling UnitedTrade integrations.", "UnitedPolitics");
            getServer().getPluginManager().registerEvents(new TradeEventListeners(this, messageProvider), this);
            this.unitedTradeEnabled = true;
        }

        Plugin unitedWar = Bukkit.getPluginManager().getPlugin("UnitedWar");
        if (unitedWar != null && unitedWar.isEnabled()) {
            Logger.log("Enabling UnitedWar integrations.", "UnitedPolitics");
            getServer().getPluginManager().registerEvents(new WarEventListeners(this), this);
            this.unitedWarEnabled = true;
        }

        Plugin unitedDungeons = Bukkit.getPluginManager().getPlugin("UnitedDungeons");
        if (unitedDungeons != null && unitedDungeons.isEnabled()) {
            Logger.log("Enabling UnitedDungeons integrations.", "UnitedPolitics");
            getServer().getPluginManager().registerEvents(new DungeonEventListener(this), this);
            this.unitedDungeonsEnabled = true;
        }
    }

    public IGeopolWrapper getGeopolWrapper() {
        return geopolWrapper;
    }

    public MessageProvider getMessageProvider() {
        return messageProvider;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ReputationManager getReputationManager() {
        return reputationManager;
    }

    public DiplomacyManager getDiplomacyManager() {
        return diplomacyManager;
    }

    public ActorProfileManager getActorProfileManager() {
        return actorProfileManager;
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
