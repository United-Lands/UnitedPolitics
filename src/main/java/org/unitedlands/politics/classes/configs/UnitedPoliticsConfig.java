package org.unitedlands.politics.classes.configs;

import java.util.List;

import org.unitedlands.annotations.UnitedConfig;
import org.unitedlands.annotations.UnitedSection;
import org.unitedlands.annotations.UnitedSetting;
import org.unitedlands.registrars.config.UnitedConfigHandler;
import org.unitedlands.registrars.config.UnitedConfigs;

@UnitedConfig(file = "config.yml") 
public interface UnitedPoliticsConfig extends UnitedConfigHandler {
    static UnitedPoliticsConfig get() { return UnitedConfigs.get(UnitedPoliticsConfig.class); } 

    @UnitedSetting(key = "developer-mode", def = "false")
    boolean developerMode();

    @UnitedSection(key = "mysql")
    MysqlSettings mysql();


    @UnitedSection (key = "actions") 
    ActionSettings actions();

    record MysqlSettings(
            @UnitedSetting(key = "host",     def = "localhost")     String host,
            @UnitedSetting(key = "port",     def = "3306")          int    port,
            @UnitedSetting(key = "username", def = "unitedlands")   String username,
            @UnitedSetting(key = "password", def = "unitedlands")   String password,
            @UnitedSetting(key = "database", def = "unitedlands")   String database
    ) {}

    record ActionSettings(
        @UnitedSection (key = "tribute") TributeSettings tribute,        
        @UnitedSection (key = "propaganda") PropagandaSettings propaganda,
        @UnitedSection (key = "killed") KillSettings killed
    ) { }

    record TributeSettings(
        @UnitedSetting(key = "reputation-per-money-unit", def = "0.0001")   double reputationPerMoneyUnit,
        @UnitedSetting(key = "money-log-reason-source", def = "Paid tribute to ")   String moneyLogReasonSource,
        @UnitedSetting(key = "money-log-reason-target", def = "Tribute paid by ")   String moneyLogReasonTarget
    ) { }

    record PropagandaSettings(
        @UnitedSetting(key = "reputation-per-money-unit", def = "0.0001")   double reputationPerMoneyUnit,
        @UnitedSetting(key = "money-log-reason-source", def = "Paid tribute to ")   String moneyLogReasonSource
    ) { }

    record KillSettings(
        @UnitedSetting(key = "enabled", def = "true")   boolean enabled,
        @UnitedSetting(key = "blacklisted-worlds")   List<String> blacklistedWorlds,
        @UnitedSetting(key = "amount", def = "5 ")   double amount
    ) { }




}