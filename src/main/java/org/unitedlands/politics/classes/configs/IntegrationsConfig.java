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

    record UnitedTradeIntegration(
        @UnitedSetting(key = "use-minimum-town-reputation", def = "false") boolean useMinimumTownReputation,
        @UnitedSetting(key = "use-minimum-nation-reputation", def = "false") boolean useMinimumNationReputation,
        @UnitedSetting(key = "default-reputation", def = "0") double defaultReputation,
        @UnitedSetting(key = "minimum-fail-message", def = "") String minimumFailMessage
    ) { }

    record UnitedWarsIntegration(
        @UnitedSetting(key = "war-opinion-threshold", def = "50") double warOpinionThreshold
    ) { }
}

