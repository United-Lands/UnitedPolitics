package org.unitedlands.politics.classes.configs;

import org.unitedlands.annotations.UnitedConfig;
import org.unitedlands.annotations.UnitedSection;
import org.unitedlands.annotations.UnitedSetting;
import org.unitedlands.registrars.config.UnitedConfigHandler;
import org.unitedlands.registrars.config.UnitedConfigs;

@UnitedConfig(file = "integrations.yml")
public interface IntegrationsConfig extends UnitedConfigHandler {
    static IntegrationsConfig get() { return UnitedConfigs.get(IntegrationsConfig.class); }

    @UnitedSection(key = "UnitedTrade")
    UnitedTradeIntegration unitedTrade();

    @UnitedSection(key = "UnitedWars")
    UnitedWarsIntegration unitedWars();

    @UnitedSection(key = "UnitedDungeons")
    UnitedDungeonsIntegration unitedDungeons();

    @UnitedSection(key = "Quickshop")
    QuickshopIntegration quickshop();

    record UnitedTradeIntegration(
        @UnitedSetting(key = "use-minimum-town-reputation", def = "false") boolean useMinimumTownReputation,
        @UnitedSetting(key = "use-minimum-nation-reputation", def = "false") boolean useMinimumNationReputation,
        @UnitedSetting(key = "default-reputation", def = "0") double defaultReputation,
        @UnitedSection(key = "reputation-bonus") ReputationBonusSettings reputationBonus
    ) { }

    record ReputationBonusSettings(
        @UnitedSetting(key = "enabled", def = "false") boolean enabled,
        @UnitedSetting(key = "minimum", def = "50") double minimum,
        @UnitedSetting(key = "limit", def = "100") double limit,
        @UnitedSetting(key = "factor", def = "0.5") double factor,
        @UnitedSetting(key = "reason", def = "") String reason
    ) { }

    record UnitedWarsIntegration(
        @UnitedSetting(key = "war-opinion-threshold", def = "50") double warOpinionThreshold
    ) { }

    record UnitedDungeonsIntegration(
        @UnitedSection(key = "looted-hostile-dungeon") DungeonLootSettings lootedHostileDungeon,
        @UnitedSection(key = "looted-friendly-dungeon") DungeonLootSettings lootedFriendlyDungeon
    ) { }

    record DungeonLootSettings(
        @UnitedSetting(key = "amount", def = "0") double amount
    ) { }

    record QuickshopIntegration(
        @UnitedSetting(key = "enabled", def = "true") boolean enabled,
        @UnitedSetting(key = "reputation-per-money-unit", def = "0.001") double reputationPerMoneyUnit,
        @UnitedSetting(key = "passthrough", def = "true") boolean passthrough
    ) { }

}
