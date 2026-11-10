package org.unitedlands.politics.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.unitedlands.politics.models.DiplomacyRecord;
import org.unitedlands.unitedlands.classes.Citizen;
import org.unitedlands.unitedlands.classes.Country;
import org.unitedlands.utils.United;

public class DiplomacyManager {
    private static DiplomacyManager instance;

    public static DiplomacyManager instance() {
        return instance;
    }

    private final DatabaseManager databaseManager;
    private Map<UUID, DiplomacyRecord> diplomacyRecords = new HashMap<>();

    public DiplomacyManager(DatabaseManager databaseManager) {
        instance = this;
        this.databaseManager = databaseManager;
    }

    public void loadDiplomacyRecords() {
        diplomacyRecords = new HashMap<>();

        var service = databaseManager.getDiplomacyRecordService();
        service.getAllAsync().thenAccept(entries -> {
            for (var entry : entries) {
                diplomacyRecords.put(entry.getUuid(), entry);
            }
            United.logger().info("Loaded " + entries.size() + " diplomacy records to memory.", "UnitedPolitics");
        });

    }

    public DiplomacyRecord getOrCreateDiplomacyRecord(Country country) {
        return diplomacyRecords.computeIfAbsent(country.getUuid(), k -> new DiplomacyRecord(country.getUuid()));
    }

    public void addOrUpdateDiplomacyRecord(Country country, DiplomacyRecord record) {
        diplomacyRecords.put(country.getUuid(), record);
    }

    public boolean embargoExists(Country country, Country targetCountry) {
        if (diplomacyRecords.containsKey(country.getUuid())) {
            return diplomacyRecords.get(country.getUuid()).isOnEmbargo(targetCountry);
        }
        return false;
    }

    public boolean isAmbassador(Citizen citizen) {
            return diplomacyRecords.values().stream().anyMatch(r -> r.isAmbassador(citizen));
    }

    public boolean hasAmbassador(Country country, Country targetCountry) {
        if (diplomacyRecords.containsKey(country.getUuid())) {
            return diplomacyRecords.get(country.getUuid()).hasAmbassadorIn(targetCountry);
        }
        return false;
    }

}
