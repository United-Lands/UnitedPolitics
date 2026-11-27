package org.unitedlands.politics.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.classes.configs.RecordDefinitionConfig;
import org.unitedlands.politics.classes.configs.UnitedPoliticsConfig;
import org.unitedlands.politics.integrations.UnitedWar.utils.UnitedWarUtils;
import org.unitedlands.politics.models.ActorProfile;
import org.unitedlands.politics.models.ReputationScoreEntry;
import org.unitedlands.politics.utils.ColorFormatter;
import org.unitedlands.unitedlands.classes.Country;
import org.unitedlands.unitedlands.classes.GeopolObject;
import org.unitedlands.unitedlands.classes.Region;
import org.unitedlands.unitedlands.classes.Settlement;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

public class ReputationManager {

    private static ReputationManager instance;

    public static ReputationManager instance() {
        return instance;
    }

    private final DatabaseManager databaseManager;

    private Map<UUID, ReputationScoreEntry> reputationScoreEntries = new HashMap<>();
    private Map<UUID, Map<UUID, Map<UUID, ReputationScoreEntry>>> entriesByObserver = new HashMap<>();
    private Map<UUID, Map<UUID, Map<UUID, ReputationScoreEntry>>> entriesBySubject = new HashMap<>();

    public ReputationManager(DatabaseManager databaseManager) {
        instance = this;
        this.databaseManager = databaseManager;
    }

    public void loadReputationRecords() {

        var service = databaseManager.getReputationScoreEntryService();
        try {
            var entries = service.getAllAsync().get();
            reputationScoreEntries = new HashMap<>();
            entriesByObserver = new HashMap<>();
            entriesBySubject = new HashMap<>();
            for (var entry : entries) {
                putEntry(entry);
            }
            United.logger().info("Loaded " + entries.size() + " reputation entries to memory.", "UnitedPolitics");
        } catch (InterruptedException | ExecutionException e) {
            United.logger().error("Failed to load reputation entries: " + e.getMessage(), "UnitedPolitics");
        }

    }

    private void putEntry(ReputationScoreEntry entry) {
        var previous = reputationScoreEntries.put(entry.getUuid(), entry);
        if (previous != null)
            unindexEntry(previous);
        indexEntry(entry);
    }

    private ReputationScoreEntry removeEntry(UUID entryId) {
        var removed = reputationScoreEntries.remove(entryId);
        if (removed != null)
            unindexEntry(removed);
        return removed;
    }

    private void indexEntry(ReputationScoreEntry entry) {
        var observerId = entry.getObserver();
        var subjectId = entry.getSubject();
        if (observerId == null || subjectId == null)
            return;
        var pair = entriesByObserver.computeIfAbsent(observerId, k -> new HashMap<>())
                .computeIfAbsent(subjectId, k -> new HashMap<>());
        pair.put(entry.getUuid(), entry);
        entriesBySubject.computeIfAbsent(subjectId, k -> new HashMap<>()).putIfAbsent(observerId, pair);
    }

    private void unindexEntry(ReputationScoreEntry entry) {
        var observerId = entry.getObserver();
        var subjectId = entry.getSubject();
        if (observerId == null || subjectId == null)
            return;
        var subjects = entriesByObserver.get(observerId);
        if (subjects == null)
            return;
        var pair = subjects.get(subjectId);
        if (pair == null)
            return;
        pair.remove(entry.getUuid());
        if (!pair.isEmpty())
            return;
        subjects.remove(subjectId);
        if (subjects.isEmpty())
            entriesByObserver.remove(observerId);
        var observers = entriesBySubject.get(subjectId);
        if (observers != null) {
            observers.remove(observerId);
            if (observers.isEmpty())
                entriesBySubject.remove(subjectId);
        }
    }

    private Collection<ReputationScoreEntry> getPairEntries(UUID observerId, UUID subjectId) {
        var subjects = entriesByObserver.get(observerId);
        if (subjects == null)
            return List.of();
        var pair = subjects.get(subjectId);
        if (pair == null)
            return List.of();
        return pair.values();
    }

    private static List<ReputationScoreEntry> flatten(Map<UUID, Map<UUID, ReputationScoreEntry>> pairs) {
        List<ReputationScoreEntry> result = new ArrayList<>();
        if (pairs == null)
            return result;
        for (var pair : pairs.values()) {
            result.addAll(pair.values());
        }
        return result;
    }

    public List<String> getReputationKeys(UUID observerId, UUID subjectId) {
        return getPairEntries(observerId, subjectId).stream()
                .map(ReputationScoreEntry::getKey)
                .collect(Collectors.toList());
    }

    public double getTotalReputationScore(UUID observerId, UUID subjectId) {
        var dynamicScore = getPairEntries(observerId, subjectId).stream()
                .collect(Collectors.summingDouble(ReputationScoreEntry::getModifier));
        var staticScore = calculateStaticReputationScoreEntries(observerId, subjectId).stream()
                .collect(Collectors.summingDouble(ReputationScoreEntry::getModifier));
        return clampTotalReputation(staticScore + dynamicScore);
    }

    public double clampTotalReputation(double score) {
        var caps = UnitedPoliticsConfig.get().totalReputation();
        return Math.max(caps.lowCap(), Math.min(caps.highCap(), score));
    }

    public Map<GeopolObject, Double> getTotalReputationScores(UUID observerId) {

        var result = new HashMap<GeopolObject, Double>();
        var dynamicRecords = entriesByObserver.get(observerId);
        if (dynamicRecords == null)
            return result;

        for (var dynamicEntry : dynamicRecords.entrySet()) {
            var subjectObj = UnitedLandsDataManager.instance().getGeopolObject(dynamicEntry.getKey());
            if (subjectObj == null)
                continue;

            var dynamicScore = dynamicEntry.getValue().values().stream()
                    .collect(Collectors.summingDouble(r -> r.getModifier()));
            var staticScore = calculateStaticReputationScoreEntries(observerId, dynamicEntry.getKey()).stream()
                    .collect(Collectors.summingDouble(ReputationScoreEntry::getModifier));

            var totalScore = clampTotalReputation(staticScore + dynamicScore);
            result.put(subjectObj, totalScore);
        }

        return result;
    }

    public Collection<ReputationScoreEntry> getReputationScoreEntries(UUID observerId, UUID subjectId) {
        return new ArrayList<>(getPairEntries(observerId, subjectId));
    }

    public ReputationScoreEntry getReputationScoreEntryWithKey(UUID observerId, UUID subjectId, String key) {
        return getPairEntries(observerId, subjectId).stream()
                .filter(r -> r.getKey().equalsIgnoreCase(key)).findFirst().orElse(null);
    }

    public Collection<ReputationScoreEntry> getReputationScoreEntriesForObject(UUID objectId) {
        var result = flatten(entriesByObserver.get(objectId));
        for (var entry : flatten(entriesBySubject.get(objectId))) {
            if (!objectId.equals(entry.getObserver()))
                result.add(entry);
        }
        return result;
    }

    public Collection<ReputationScoreEntry> getReputationScoreEntriesForObserver(UUID observerId) {
        return flatten(entriesByObserver.get(observerId));
    }

    public Collection<ReputationScoreEntry> getReputationScoreEntriesForSubject(UUID subjectId) {
        return flatten(entriesBySubject.get(subjectId));
    }

    public Collection<ReputationScoreEntry> calculateStaticReputationScoreEntries(UUID observerId, UUID subjectId) {
        List<ReputationScoreEntry> result = new ArrayList<>();

        // General geopol relations

        GeopolObject observerObj = UnitedLandsDataManager.instance().getGeopolObject(observerId);
        GeopolObject subjectObj = UnitedLandsDataManager.instance().getGeopolObject(subjectId);

        if (observerObj != null && subjectObj != null) {
            if (observerObj instanceof Settlement observerTown) {
                if (subjectObj instanceof Settlement subjectTown) {
                    // Town observing town

                    // Same region score
                    if (observerTown.getRegion() != null && subjectTown.getRegion() != null
                            && observerTown.getRegion().equals(subjectTown.getRegion())) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "same-region"));
                    }
                    // Same nation score
                    if (observerTown.getCountry() != null && subjectTown.getCountry() != null
                            && observerTown.getCountry().equals(subjectTown.getCountry())) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "same-nation"));
                    }

                } else if (subjectObj instanceof Region subjectRegion) {
                    // Town observing region

                    // Our region score
                    if (observerTown.getRegion() != null && observerTown.getRegion().equals(subjectRegion)) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "our-region"));
                    }

                    // Same nation score
                    if (observerTown.getCountry() != null && subjectRegion.getCountry() != null
                            && observerTown.getCountry().equals(subjectRegion.getCountry())) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "same-nation"));
                    }

                } else if (subjectObj instanceof Country subjectNation) {
                    // Town observing nation

                    // Our nation score
                    if (observerTown.getCountry() != null && observerTown.getCountry().equals(subjectNation)) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "our-nation"));
                    }
                }
            } else if (observerObj instanceof Region observerRegion) {
                if (subjectObj instanceof Settlement subjectTown) {
                    // Region observing town

                    // Our town score
                    if (subjectTown.getRegion() != null && subjectTown.getRegion().equals(observerRegion)) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "our-town"));
                    }

                } else if (subjectObj instanceof Region subjectRegion) {
                    // Region observing region

                    // Same nation score
                    if (observerRegion.getCountry() != null
                            && observerRegion.getCountry().equals(subjectRegion.getCountry())) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "same-nation"));
                    }

                } else if (subjectObj instanceof Country subjectNation) {
                    // Region observing nation

                    // Our nation score
                    if (observerRegion.getCountry() != null && observerRegion.getCountry().equals(subjectNation)) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "our-nation"));
                    }
                }
            } else if (observerObj instanceof Country observerNation) {
                if (subjectObj instanceof Settlement subjectTown) {
                    // Nation observing town

                    // Our town score
                    if (subjectTown.getCountry() != null && subjectTown.getCountry().equals(observerNation)) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "our-town"));
                    }

                } else if (subjectObj instanceof Region subjectRegion) {
                    // Nation observing region

                    // Our region score
                    if (subjectRegion.getCountry() != null && subjectRegion.getCountry().equals(observerNation)) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "our-region"));
                    }

                } else if (subjectObj instanceof Country subjectNation) {
                    // Nation observing nation
                    // TODO: Rivalry, allies etc.

                    // See if the subject has put the observer on a trade embargo
                    if (DiplomacyManager.instance().embargoExists(subjectNation, observerNation)) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "embargo"));
                    }
                }
            }

        }

        // Rivals and partners

        ActorProfile observerProfile = ActorProfileManager.instance().getActorProfile(observerId);

        if (observerProfile != null) {
            // Direct relations
            var observerPartners = observerProfile.getPartners();
            var observerRivals = observerProfile.getRivals();

            if (observerPartners != null && observerPartners.contains(subjectId)) {
                result.add(createReputationScoreEntry(observerId, subjectId, "our-partner"));
            } else if (observerRivals != null && observerRivals.contains(subjectId)) {
                result.add(createReputationScoreEntry(observerId, subjectId, "our-rival"));
            } else {
                // Indirect relations
                if (observerPartners != null) {
                    for (var partnerId : observerPartners) {
                        var partnerProfile = ActorProfileManager.instance().getActorProfile(partnerId);
                        if (partnerProfile == null)
                            continue;
                        if (partnerProfile.getPartners().contains(subjectId)) {
                            result.add(createReputationScoreEntry(observerId, subjectId, "our-partners-partner"));
                        } else if (partnerProfile.getRivals().contains(subjectId)) {
                            result.add(createReputationScoreEntry(observerId, subjectId, "our-partners-rival"));
                        }
                    }
                }

                if (observerRivals != null) {
                    for (var rivalId : observerRivals) {
                        var rivalprofile = ActorProfileManager.instance().getActorProfile(rivalId);
                        if (rivalprofile == null)
                            continue;
                        if (rivalprofile.getPartners() != null && rivalprofile.getPartners().contains(subjectId)) {
                            result.add(createReputationScoreEntry(observerId, subjectId, "our-rivals-partner"));
                        } else if (rivalprofile.getRivals() != null && rivalprofile.getRivals().contains(subjectId)) {
                            result.add(createReputationScoreEntry(observerId, subjectId, "our-rivals-rival"));
                        }
                    }
                }
            }
        }

        // Wars

        if (UnitedPolitics.instance().isUnitedWarsEnabled()) {
            UnitedWarUtils warUtils = new UnitedWarUtils();
            if (warUtils.isActorInWar(observerObj)) {
                var opponents = warUtils.getOpponents(observerObj);
                var allies = warUtils.getAllies(observerObj);

                if (opponents.contains(subjectId)) {
                    result.add(createReputationScoreEntry(observerId, subjectId, "uw-enemy-in-war"));
                } else if (allies.contains(subjectId)) {
                    result.add(createReputationScoreEntry(observerId, subjectId, "uw-ally-in-war"));
                }
            }
        }

        return result;
    }

    public void removeEverythingFor(UUID objectId) {
        var entriesToRemove = getReputationScoreEntriesForObject(objectId);
        if (entriesToRemove.isEmpty())
            return;
        for (var entry : entriesToRemove) {
            removeEntry(entry.getUuid());
        }
        databaseManager.getReputationScoreEntryService().deleteAllAsync(entriesToRemove);
    }

    public ReputationScoreEntry getOrCreateReputationScoreEntry(UUID observerId, UUID subjectId,
            String configSectionKey) {

        ReputationScoreEntry entry = getReputationScoreEntryWithKey(observerId, subjectId, configSectionKey);
        if (entry == null) {
            entry = createReputationScoreEntry(observerId, subjectId, configSectionKey);
        }

        return entry;
    }

    private ReputationScoreEntry createReputationScoreEntry(UUID observerId, UUID subjectId, String configSectionKey) {

        ReputationScoreEntry entry;

        var recordDefinitions = RecordDefinitionConfig.get().recordDefinitions();
        if (configSectionKey == null || !recordDefinitions.has(configSectionKey)) {
            United.logger().error("No record definition found for reputation key '" + configSectionKey
                    + "', entry not created.", "UnitedPolitics");
            return null;
        }

        var recordDefinition = recordDefinitions.get(configSectionKey);
        String description = recordDefinition.description();
        double lowCap = recordDefinition.lowCap();
        double highCap = recordDefinition.highCap();
        double decay = recordDefinition.decay();
        double defaultamount = recordDefinition.defaultValue();
        boolean ignoreDecayGracePeriod = recordDefinition.ignoreDecayGracePeriod();

        entry = new ReputationScoreEntry();
        entry.setUuid(UUID.randomUUID());
        entry.setTimestamp(System.currentTimeMillis());
        entry.setObserver(observerId);
        entry.setSubject(subjectId);
        entry.setKey(configSectionKey);
        entry.setDescription(description);
        entry.setLowCap(lowCap);
        entry.setHighCap(highCap);
        entry.setDecayRate(decay);
        entry.setModifier(defaultamount);
        entry.setIgnoreDecayGracePeriod(ignoreDecayGracePeriod);

        return entry;
    }

    public void resetToDefault(ReputationScoreEntry entry) {
        var recordDefinitions = RecordDefinitionConfig.get().recordDefinitions();
        if (entry == null || !recordDefinitions.has(entry.getKey()))
            return;
        entry.setModifier(recordDefinitions.get(entry.getKey()).defaultValue());
    }

    public boolean addOrUpdateReputationScoreEntry(ReputationScoreEntry entry) {

        if (entry == null || entry.getUuid() == null)
            return false;

        var service = databaseManager.getReputationScoreEntryService();

        if (reputationScoreEntries.containsKey(entry.getUuid())) {
            putEntry(entry);
            service.updateAsync(entry);
        } else {
            putEntry(entry);
            service.createAsync(entry);
        }
        return true;
    }

    public boolean removeReputationEntry(ReputationScoreEntry entry) {
        if (entry == null || entry.getUuid() == null)
            return false;

        var removed = removeEntry(entry.getUuid());
        if (removed == null)
            return false;

        databaseManager.getReputationScoreEntryService().deleteAsync(removed);
        return true;
    }

    public void handleReputationChange(GeopolObject observer, GeopolObject subject, double modifier,
            String configKey, Player player, boolean doPassthrough) {

        if (observer == null || subject == null)
            return;

        // Main entry
        var entry = getOrCreateReputationScoreEntry(observer.getUuid(), subject.getUuid(), configKey);
        if (entry == null)
            return;

        double oldModifier = entry.getModifier();
        entry.setModifier(oldModifier + modifier);
        entry.setTimestamp(System.currentTimeMillis());

        if (addOrUpdateReputationScoreEntry(entry)) {
            logChange(observer, subject, configKey, entry.getModifier());
            if (player != null) {
                sendMessage(observer, subject, configKey, player, entry, oldModifier);
            }
        }

        if (!doPassthrough)
            return;

        // Passthrough

        // Passthrough works up and down depending on the observer's and subject's
        // position in the geopolitical hierarchy.

        var upSettings = UnitedPoliticsConfig.get().repPassthroughUp();
        var downSettings = UnitedPoliticsConfig.get().repPassthroughDown();

        Map<GeopolObject, Double> observerPassthroughUp = upSettings.enabled()
                ? getPassthroughMapUp(observer, upSettings.factor())
                : new HashMap<>();
        Map<GeopolObject, Double> observerPassthroughDown = downSettings.enabled()
                ? getPassthroughMapDown(observer, downSettings.factor())
                : new HashMap<>();
        Map<GeopolObject, Double> subjectPassthroughUp = upSettings.enabled()
                ? getPassthroughMapUp(subject, upSettings.factor())
                : new HashMap<>();
        Map<GeopolObject, Double> subjectPassthroughDown = downSettings.enabled()
                ? getPassthroughMapDown(subject, downSettings.factor())
                : new HashMap<>();

        // Pass reputation up
        for (var observerUpEntry : observerPassthroughUp.entrySet()) {
            // Change the main subject reputation
            addOrUpdatePassthroughEntry(observerUpEntry.getKey(), subject, modifier, configKey,
                    observerUpEntry.getValue());
            // Also let the reputation change pass through up or down the subject's
            // geopolitical layers, with increasingly smaller impact.
            for (var subjectUpEntry : subjectPassthroughUp.entrySet()) {
                addOrUpdatePassthroughEntry(observerUpEntry.getKey(), subjectUpEntry.getKey(), modifier, configKey,
                        subjectUpEntry.getValue() * observerUpEntry.getValue());
            }
            for (var subjectDownEntry : subjectPassthroughDown.entrySet()) {
                addOrUpdatePassthroughEntry(observerUpEntry.getKey(), subjectDownEntry.getKey(), modifier, configKey,
                        subjectDownEntry.getValue() * observerUpEntry.getValue());
            }
        }
        // Pass reputation down
        for (var observerDownEntry : observerPassthroughDown.entrySet()) {
            // Change the main subject reputation
            addOrUpdatePassthroughEntry(observerDownEntry.getKey(), subject, modifier, configKey,
                    observerDownEntry.getValue());
            for (var subjectUpEntry : subjectPassthroughUp.entrySet()) {
                addOrUpdatePassthroughEntry(observerDownEntry.getKey(), subjectUpEntry.getKey(), modifier, configKey,
                        subjectUpEntry.getValue() * observerDownEntry.getValue());
            }
            // Also let the reputation change pass through up or down the subject's
            // geopolitical layers, with increasingly smaller impact.
            for (var subjectDownEntry : subjectPassthroughDown.entrySet()) {
                addOrUpdatePassthroughEntry(observerDownEntry.getKey(), subjectDownEntry.getKey(), modifier, configKey,
                        subjectDownEntry.getValue() * observerDownEntry.getValue());
            }
        }

    }

    private void addOrUpdatePassthroughEntry(GeopolObject observer, GeopolObject subject, double modifier,
            String configKey, double factor) {
        var repEntry = getOrCreateReputationScoreEntry(observer.getUuid(), subject.getUuid(), configKey);
        if (repEntry == null)
            return;
        repEntry.setModifier(repEntry.getModifier() + (modifier * factor));
        repEntry.setTimestamp(System.currentTimeMillis());
        if (addOrUpdateReputationScoreEntry(repEntry)) {
            logChange(observer, subject, configKey, repEntry.getModifier());
        }
    }

    private Map<GeopolObject, Double> getPassthroughMapUp(GeopolObject obj, Double factor) {
        Map<GeopolObject, Double> map = new HashMap<>();
        if (obj instanceof Settlement town) {
            if (town.getRegion() != null) {
                map.put(town.getRegion(), factor);
            }
            if (town.getCountry() != null) {
                map.put(town.getCountry(), factor * factor);
            }
        } else if (obj instanceof Region region) {
            if (region.getCountry() != null) {
                map.put(region.getCountry(), factor);
            }
        }
        return map;
    }

    private Map<GeopolObject, Double> getPassthroughMapDown(GeopolObject obj, Double factor) {
        Map<GeopolObject, Double> map = new HashMap<>();
        if (obj instanceof Country nation) {
            for (var region : nation.getRegions()) {
                map.put(region, factor);
            }
            for (var town : nation.getSettlements()) {
                map.put(town, factor * factor);
            }
        } else if (obj instanceof Region region) {
            for (var town : region.getSettlements()) {
                map.put(town, factor);
            }
        }
        return map;
    }

    private void sendMessage(GeopolObject observer, GeopolObject subject, String configKey,
            Player player, ReputationScoreEntry entry, double oldModifier) {

        double change = entry.getModifier() - oldModifier;
        String changeString = ColorFormatter.getAmountColored(change);
        if (change == 0)
            changeString += " <yellow>(maxed)</yellow>";

        String displayName = RecordDefinitionConfig.get().recordDefinitions().get(configKey).displayName();
        if (displayName == null)
            displayName = configKey;

        var total = getTotalReputationScore(observer.getUuid(), subject.getUuid());
        United.messenger().send(player, "reputation-changed",
                observer.getName(),
                subject.getName(),
                changeString,
                displayName,
                ColorFormatter.getAmountColored(total));
    }

    private void logChange(GeopolObject observer, GeopolObject subject, String key, double amount) {
        if (observer == null || subject == null)
            return;
        United.logger().info(
                "Added/updated reputation entry {" + key + "} of " + observer.getName() + " with " + subject.getName()
                        + ": " + amount,
                "UnitedPolitics");
    }

    public Collection<ReputationScoreEntry> getReputationScoreEntries() {
        return Collections.unmodifiableCollection(reputationScoreEntries.values());
    }

    public void doReputationDecay() {

        var service = databaseManager.getReputationScoreEntryService();

        List<ReputationScoreEntry> entriesToRemove = new ArrayList<>();
        List<ReputationScoreEntry> entriesToUpdate = new ArrayList<>();

        var decayThreshold = UnitedPoliticsConfig.get().repDecayGracePeriod() * 1000;

        var sortedEntries = reputationScoreEntries.values().stream()
                .sorted(Comparator.comparing(ReputationScoreEntry::getUuid)).collect(Collectors.toList());
        for (ReputationScoreEntry entry : sortedEntries) {

            GeopolObject subject = UnitedLandsDataManager.instance().getGeopolObject(entry.getObserver());
            GeopolObject target = UnitedLandsDataManager.instance().getGeopolObject(entry.getSubject());

            String subjectStr = subject != null ? subject.getName() : entry.getObserver().toString();
            String targetStr = target != null ? target.getName() : entry.getSubject().toString();

            var timeStamp = entry.getTimestamp();
            if (timeStamp == null)
                timeStamp = 0L;

            var millisecondsSinceTimestamp = (System.currentTimeMillis() - timeStamp);

            if (millisecondsSinceTimestamp < decayThreshold && !entry.ignoreDecayGracePeriod()) {
                continue;
            }

            var decay = entry.getDecayRate();
            var currentModifier = entry.getModifier();
            var newModifier = currentModifier + decay;

            if (currentModifier < 0 && decay > 0) {
                newModifier = Math.min(newModifier, 0);
            } else if (currentModifier > 0 && decay < 0) {
                newModifier = Math.max(newModifier, 0);
            }

            if (newModifier == 0) {
                entriesToRemove.add(entry);
                United.logger().info("Reputation entry {" + entry.getKey() + "} of " + targetStr + " with "
                        + subjectStr + " reached 0, removed", "UnitedPolitics");
            } else {
                entry.setModifier(newModifier);
                if (entry.getModifier() == currentModifier)
                    continue;
                entriesToUpdate.add(entry);
                United.logger().info("Reputation entry {" + entry.getKey() + "} of " + targetStr + " with "
                        + subjectStr + ": " + currentModifier + " → " + entry.getModifier(), "UnitedPolitics");
            }
        }

        for (var entry : entriesToRemove) {
            removeEntry(entry.getUuid());
        }

        if (!entriesToUpdate.isEmpty())
            service.updateAllAsync(entriesToUpdate);
        if (!entriesToRemove.isEmpty())
            service.deleteAllAsync(entriesToRemove);
    }

}
