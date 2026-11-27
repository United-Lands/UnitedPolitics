package org.unitedlands.politics.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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

        var service = databaseManager.getDiplomacyRecordService();
        try {
            var entries = service.getAllAsync().get();
            Map<UUID, DiplomacyRecord> loadedRecords = new HashMap<>();
            for (var entry : entries) {
                loadedRecords.put(entry.getUuid(), entry);
            }
            diplomacyRecords = loadedRecords;
            United.logger().info("Loaded " + entries.size() + " diplomacy records to memory.", "UnitedPolitics");
        } catch (InterruptedException | ExecutionException e) {
            United.logger().error("Failed to load diplomacy records: " + e.getMessage(), "UnitedPolitics");
        }

    }

    public DiplomacyRecord getDiplomacyRecord(Country country) {
        return diplomacyRecords.get(country.getUuid());
    }

    public DiplomacyRecord getOrCreateDiplomacyRecord(Country country) {
        var record = diplomacyRecords.get(country.getUuid());
        if (record == null)
            record = new DiplomacyRecord(country.getUuid());
        return record;
    }

    public void saveDiplomacyRecord(DiplomacyRecord record) {
        var service = databaseManager.getDiplomacyRecordService();
        if (diplomacyRecords.containsKey(record.getUuid())) {
            diplomacyRecords.put(record.getUuid(), record);
            service.updateAsync(record);
        } else {
            diplomacyRecords.put(record.getUuid(), record);
            service.createAsync(record);
        }
    }

    public void removeDiplomacyRecord(DiplomacyRecord record) {
        var removed = diplomacyRecords.remove(record.getUuid());
        if (removed != null)
            databaseManager.getDiplomacyRecordService().deleteAsync(removed);
    }

    public void removeEverythingFor(Country country) {
        var ownRecord = diplomacyRecords.get(country.getUuid());
        if (ownRecord != null)
            removeDiplomacyRecord(ownRecord);

        for (var record : new ArrayList<>(diplomacyRecords.values())) {
            boolean changed = false;
            if (record.isOnEmbargo(country)) {
                record.removeEmbargo(country);
                changed = true;
            }
            var ambassadorsInCountry = record.getAmbassadors().entrySet().stream()
                    .filter(e -> e.getValue().equals(country))
                    .map(Map.Entry::getKey)
                    .toList();
            for (var citizen : ambassadorsInCountry) {
                record.removeAmbassador(citizen);
                changed = true;
            }
            if (changed)
                saveDiplomacyRecord(record);
        }
    }

    public void validateAmbassador(Citizen citizen) {
        for (var record : new ArrayList<>(diplomacyRecords.values())) {
            if (!record.isAmbassador(citizen))
                continue;

            var citizenCountry = citizen.getCountry();
            if (citizenCountry != null && citizenCountry.getUuid().equals(record.getUuid()))
                continue;

            var targetCountry = record.getAmbassdorCountry(citizen);
            record.removeAmbassador(citizen);
            saveDiplomacyRecord(record);

            if (targetCountry != null) {
                var entry = ReputationManager.instance().getReputationScoreEntryWithKey(targetCountry.getUuid(),
                        record.getUuid(), "ambassador");
                ReputationManager.instance().removeReputationEntry(entry);
            }

            United.logger().info("Removed ambassador " + citizen.getName()
                    + " because they are no longer a citizen of the sending country.", "UnitedPolitics");
        }
    }

    public boolean embargoExists(Country country, Country targetCountry) {
        var record = diplomacyRecords.get(country.getUuid());
        return record != null && record.isOnEmbargo(targetCountry);
    }

    public boolean isAmbassador(Citizen citizen) {
        return diplomacyRecords.values().stream().anyMatch(r -> r.isAmbassador(citizen));
    }

    public boolean hasAmbassador(Country country, Country targetCountry) {
        var record = diplomacyRecords.get(country.getUuid());
        return record != null && record.hasAmbassadorIn(targetCountry);
    }

}
