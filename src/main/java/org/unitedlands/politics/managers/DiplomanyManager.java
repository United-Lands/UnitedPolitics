package org.unitedlands.politics.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.unitedlands.politics.models.DiplomacyRecord;
import org.unitedlands.unitedlands.classes.Country;
import org.unitedlands.utils.United;

public class DiplomanyManager {
    private static DiplomanyManager instance;

    public static DiplomanyManager instance() {
        return instance;
    }

    private final DatabaseManager databaseManager;
    private Map<UUID, DiplomacyRecord> diplomacyRecords;

    public DiplomanyManager(DatabaseManager databaseManager) {
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

    public DiplomacyRecord getDiplomacyRecord(Country country) {
        return diplomacyRecords.get(country.getUuid());
    }

    public void addDiplomacyRecord(Country country, DiplomacyRecord record) {
        diplomacyRecords.put(country.getUuid(), record);
    }

}
