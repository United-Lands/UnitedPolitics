package org.unitedlands.politics.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.classes.configs.RecordDefinitionConfig;
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
    private static final Object LOCK = new Object();

    private Collection<ReputationScoreEntry> reputationScoreEntries;

    public ReputationManager(DatabaseManager databaseManager) {
        instance = this;
        this.databaseManager = databaseManager;
    }

    public void loadReputationRecords() {

        reputationScoreEntries = new ArrayList<>();

        var service = databaseManager.getReputationScoreEntryService();
        service.getAllAsync().thenAccept(entries -> {
            reputationScoreEntries = entries;
            United.logger().info("Loaded " + entries.size() + " reputation entries to memory.", "UnitedPolitics");
        });

    }

    public List<String> getReputationKeys(UUID observerId, UUID subjectId) {
        return reputationScoreEntries.stream()
                .filter(r -> r.getObserver().equals(observerId) && r.getSubject().equals(subjectId))
                .map(ReputationScoreEntry::getKey)
                .collect(Collectors.toList());
    }

    public double getTotalReputationScore(UUID observerId, UUID subjectId) {
        var dynamicScore = reputationScoreEntries.stream()
                .filter(r -> r.getObserver().equals(observerId) && r.getSubject().equals(subjectId))
                .collect(Collectors.summingDouble(ReputationScoreEntry::getModifier));
        var staticScore = calculateStaticReputationScoreEntries(observerId, subjectId).stream()
                .collect(Collectors.summingDouble(ReputationScoreEntry::getModifier));
        return Math.max(-200, Math.min(200, staticScore + dynamicScore));
    }

    public Map<GeopolObject, Double> getTotalReputationScores(UUID observerId) {

        var dynamicRecords = reputationScoreEntries.stream()
                .filter(r -> r.getObserver().equals(observerId)).collect(Collectors.groupingBy(g -> g.getSubject()));

        var result = new HashMap<GeopolObject, Double>();
        for (var dynamicEntry : dynamicRecords.entrySet()) {
            var subjectObj = UnitedLandsDataManager.instance().getGeopolObject(dynamicEntry.getKey());
            if (subjectObj == null)
                continue;

            var dynamicScore = dynamicEntry.getValue().stream().collect(Collectors.summingDouble(r -> r.getModifier()));
            var staticScore = calculateStaticReputationScoreEntries(observerId, dynamicEntry.getKey()).stream()
                    .collect(Collectors.summingDouble(ReputationScoreEntry::getModifier));

            var totalScore = Math.max(-200, Math.min(200, staticScore + dynamicScore));
            result.put(subjectObj, totalScore);
        }

        return result;
    }

    public Collection<ReputationScoreEntry> getReputationScoreEntries(UUID observerId, UUID subjectId) {
        return reputationScoreEntries.stream()
                .filter(r -> r.getObserver().equals(observerId) && r.getSubject().equals(subjectId))
                .collect(Collectors.toList());
    }

    public ReputationScoreEntry getReputationScoreEntryWithKey(UUID observerId, UUID subjectId, String key) {
        return reputationScoreEntries.stream().filter(r -> r.getObserver().equals(observerId)
                && r.getSubject().equals(subjectId) && r.getKey().equalsIgnoreCase(key)).findFirst().orElse(null);
    }

    public Collection<ReputationScoreEntry> getReputationScoreEntriesForObject(UUID objectId) {
        return reputationScoreEntries.stream()
                .filter(r -> r.getObserver().equals(objectId) || r.getSubject().equals(objectId))
                .collect(Collectors.toList());
    }

    public Collection<ReputationScoreEntry> getReputationScoreEntriesForObserver(UUID observerId) {
        return reputationScoreEntries.stream().filter(r -> r.getObserver().equals(observerId))
                .collect(Collectors.toList());
    }

    public Collection<ReputationScoreEntry> getReputationScoreEntriesForSubject(UUID subjectId) {
        return reputationScoreEntries.stream().filter(r -> r.getSubject().equals(subjectId))
                .collect(Collectors.toList());
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

                } else if (subjectObj instanceof @SuppressWarnings("unused") Country subjectNation) {
                    // Nation observing nation
                    // TODO: Rivalry, allies etc.
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
        var reputationScoreEntries = getReputationScoreEntriesForObject(objectId);
        for (var entry : reputationScoreEntries) {
            removeReputationEntry(entry);
        }
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

        var recordDefinition = RecordDefinitionConfig.get().recordDefinitions().get(configSectionKey);
        String description = recordDefinition.description();
        double lowCap = recordDefinition.lowCap();
        double highCap = recordDefinition.highCap();
        double decay = recordDefinition.decay();
        double defaultamount = recordDefinition.defaultValue();

        entry = new ReputationScoreEntry();
        entry.setTimestamp(System.currentTimeMillis());
        entry.setObserver(observerId);
        entry.setSubject(subjectId);
        entry.setKey(configSectionKey);
        entry.setDescription(description);
        entry.setLowCap(lowCap);
        entry.setHighCap(highCap);
        entry.setDecayRate(decay);
        entry.setModifier(defaultamount);

        return entry;
    }

    public boolean addOrUpdateReputationScoreEntry(ReputationScoreEntry entry) {

        if (!reputationScoreEntries.contains(entry))
            reputationScoreEntries.add(entry);

        var service = databaseManager.getReputationScoreEntryService();
        return service.createOrUpdate(entry);
    }

    public boolean removeReputationEntry(ReputationScoreEntry entry) {
        if (entry == null)
            return true;

        reputationScoreEntries.remove(entry);

        var service = databaseManager.getReputationScoreEntryService();
        return service.delete(entry.getId());
    }

    public void handleReputationChange(GeopolObject observer, GeopolObject subject, double modifier,
            String configKey, Player player, boolean doPassthrough) {

        if (observer == null || subject == null)
            return;

        // Main entry
        var entry = getOrCreateReputationScoreEntry(observer.getUuid(), subject.getUuid(), configKey);

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

        double upFactor = UnitedPolitics.instance().getConfig().getDouble("rep-passthrough-up.factor");
        double downFactor = UnitedPolitics.instance().getConfig().getDouble("rep-passthrough-down.factor");

        Map<GeopolObject, Double> observerPassthroughUp = getPasshroughMapUp(observer, upFactor);
        Map<GeopolObject, Double> observerPassthroughDown = getPasshroughMapDown(observer, downFactor);
        Map<GeopolObject, Double> subjectPassthroughUp = getPasshroughMapUp(subject, upFactor);
        Map<GeopolObject, Double> subjectPassthroughDown = getPasshroughMapDown(subject, downFactor);

        // Pass reputation up
        for (var observerUpEntry : observerPassthroughUp.entrySet()) {
            // Change the main subject reputation
            addOrUpdatePassthroughEntry(observerUpEntry.getKey(), subject, modifier, configKey, observerUpEntry.getValue());
            // Also let the reputation change pass through up or down the subject's 
            // geopolitical layers, with increasingly smaller impact.
            for (var subjectUpEntry : subjectPassthroughUp.entrySet()) {
                addOrUpdatePassthroughEntry(observerUpEntry.getKey(), subjectUpEntry.getKey(), modifier, configKey, subjectUpEntry.getValue() * observerUpEntry.getValue());
            }
            for (var subjectDownEntry : subjectPassthroughDown.entrySet()) {
                addOrUpdatePassthroughEntry(observerUpEntry.getKey(), subjectDownEntry.getKey(), modifier, configKey, subjectDownEntry.getValue() * observerUpEntry.getValue());
            }
        }
        // Pass reputation down
        for (var observerDownEntry : observerPassthroughDown.entrySet()) {
            // Change the main subject reputation
            addOrUpdatePassthroughEntry(observerDownEntry.getKey(), subject, modifier, configKey, observerDownEntry.getValue());
            for (var subjectUpEntry : subjectPassthroughUp.entrySet()) {
                addOrUpdatePassthroughEntry(observerDownEntry.getKey(), subjectUpEntry.getKey(), modifier, configKey, subjectUpEntry.getValue() * observerDownEntry.getValue());
            }
            // Also let the reputation change pass through up or down the subject's 
            // geopolitical layers, with increasingly smaller impact.
            for (var subjectDownEntry : subjectPassthroughDown.entrySet()) {
                addOrUpdatePassthroughEntry(observerDownEntry.getKey(), subjectDownEntry.getKey(), modifier, configKey, subjectDownEntry.getValue() * observerDownEntry.getValue());
            }
        }

    }

    private void addOrUpdatePassthroughEntry(GeopolObject observer, GeopolObject subject, double modifier, String configKey, double factor) {
        var repEntry = getOrCreateReputationScoreEntry(observer.getUuid(), subject.getUuid(), configKey);
        repEntry.setModifier(repEntry.getModifier() + (modifier * factor));
        repEntry.setTimestamp(System.currentTimeMillis());
        if (addOrUpdateReputationScoreEntry(repEntry)) {
            logChange(observer, subject, configKey, repEntry.getModifier());
        }
    }

    private Map<GeopolObject, Double> getPasshroughMapUp(GeopolObject obj, Double factor) {
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

    private Map<GeopolObject, Double> getPasshroughMapDown(GeopolObject obj, Double factor) {
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
        return reputationScoreEntries;
    }

    public void setReputationScoreEntries(Collection<ReputationScoreEntry> reputationScoreEntries) {
        this.reputationScoreEntries = reputationScoreEntries;
    }

    public void doReputationDecay() {

        synchronized (LOCK) {

            var service = databaseManager.getReputationScoreEntryService();

            List<ReputationScoreEntry> entriesToRemove = new ArrayList<>();

            var sortedEntries = reputationScoreEntries.stream()
                    .sorted(Comparator.comparing(ReputationScoreEntry::getId)).collect(Collectors.toList());
            for (ReputationScoreEntry entry : sortedEntries) {

                GeopolObject subject = UnitedLandsDataManager.instance().getGeopolObject(entry.getObserver());
                GeopolObject target = UnitedLandsDataManager.instance().getGeopolObject(entry.getSubject());

                String subjectStr = subject != null ? subject.getName() : entry.getObserver().toString();
                String targetStr = target != null ? target.getName() : entry.getSubject().toString();

                var timeStamp = entry.getTimestamp();
                if (timeStamp == null)
                    timeStamp = 0L;

                var millisecondsSinceTimestamp = (System.currentTimeMillis() - timeStamp);
                var decayThreshold = UnitedPolitics.instance().getConfig().getLong("rep-decay-grace-period") * 1000;

                if (millisecondsSinceTimestamp < decayThreshold) {
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
                    if (service.delete(entry.getId())) {
                        United.logger()
                                .info("Reputation entry {" + entry.getKey() + "} of " + targetStr + " with "
                                        + subjectStr
                                        + " reached 0, removed", "UnitedPolitics");
                    }

                } else {
                    entry.setModifier(newModifier);

                    if (service.createOrUpdate(entry)) {
                        United.logger()
                                .info("Reputation entry {" + entry.getKey() + "} of " + targetStr + " with "
                                        + subjectStr
                                        + ": " + currentModifier + " → " + newModifier, "UnitedPolitics");
                    } else {
                        United.logger().error("Error saving entry " + entry.getId(), "UnitedPolitics");
                    }
                }
            }

            reputationScoreEntries.removeAll(entriesToRemove);
        }
    }

}
