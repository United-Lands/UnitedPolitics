package org.unitedlands.politics.classes.configs;

import org.unitedlands.annotations.UnitedConfig;
import org.unitedlands.annotations.UnitedSection;
import org.unitedlands.annotations.UnitedSetting;
import org.unitedlands.registrars.config.UnitedConfigHandler;
import org.unitedlands.registrars.config.UnitedConfigs;

@UnitedConfig(file = "config.yml") 
public interface GeneralConfig extends UnitedConfigHandler {
    static GeneralConfig get() { return UnitedConfigs.get(GeneralConfig.class); } 

    @UnitedSetting(key = "developer-mode", def = "false")
    boolean developerMode();

    @UnitedSection(key = "mysql")
    MysqlSettings mysql();

    record MysqlSettings(
            @UnitedSetting(key = "host",     def = "localhost")     String host,
            @UnitedSetting(key = "port",     def = "3306")          int    port,
            @UnitedSetting(key = "username", def = "unitedlands")   String username,
            @UnitedSetting(key = "password", def = "unitedlands")   String password,
            @UnitedSetting(key = "database", def = "unitedlands")   String database
    ) {}



}