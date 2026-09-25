package org.unitedlands.politics.classes.configs;

import org.unitedlands.annotations.UnitedConfig;
import org.unitedlands.annotations.UnitedSection;
import org.unitedlands.annotations.UnitedSetting;
import org.unitedlands.registrars.config.UnitedConfigHandler;
import org.unitedlands.registrars.config.UnitedConfigs;
import org.unitedlands.registrars.config.UnitedDynamicSection;

@UnitedConfig(file = "record-definitions.yml") 
public interface RecordDefinitionConfig extends UnitedConfigHandler {
    static RecordDefinitionConfig get() { return UnitedConfigs.get(RecordDefinitionConfig.class); } 

    @UnitedSection(key = "record-definitions")
    UnitedDynamicSection<ReputationRecordDefinition> recordDefinitions();

    record ReputationRecordDefinition(
        @UnitedSetting(key = "description") String description,
        @UnitedSetting(key = "low-cap", def = "-200") double lowCap,
        @UnitedSetting(key = "high-cap", def = "200") double highCap,
        @UnitedSetting(key = "decay", def = "0") double decay,
        @UnitedSetting(key = "default", def = "0") double defaultValue

    ) { }

}