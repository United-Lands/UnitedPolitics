package org.unitedlands.politics.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.bukkit.entity.Player;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.classes.MessageProvider;
import org.unitedlands.politics.integrations.UnitedWar.utils.UnitedWarUtils;
import org.unitedlands.politics.models.ActorProfile;
import org.unitedlands.politics.models.ReputationScoreEntry;
import org.unitedlands.politics.utils.ColorFormatter;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.IRegionWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.utils.Logger;
import org.unitedlands.utils.Messenger;

public class ReputationManager {

    private final UnitedPolitics plugin;
    private final MessageProvider messageProvider;

    private static final Object LOCK = new Object();

    private Collection<ReputationScoreEntry> reputationScoreEntries;

    public ReputationManager(UnitedPolitics plugin, MessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    public void loadReputationRecords() {

        reputationScoreEntries = new ArrayList<>();

        var service = plugin.getDatabaseManager().getReputationScoreEntryService();
        service.getAllAsync().thenAccept(entries -> {
            reputationScoreEntries = entries;
            Logger.log("Loaded " + entries.size() + " reputation entries to memory.", "UnitedPolitics");
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

    public Map<IGeopolObjectWrapper, Double> getTotalReputationScores(UUID observerId) {

        var dynamicRecords = reputationScoreEntries.stream()
                .filter(r -> r.getObserver().equals(observerId)).collect(Collectors.groupingBy(g -> g.getSubject()));

        var result = new HashMap<IGeopolObjectWrapper, Double>();
        for (var dynamicEntry : dynamicRecords.entrySet()) {
            var subjectObj = GeopolUtils.findGeopolObject(dynamicEntry.getKey());
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

    public Collection<ReputationScoreEntry> getReputationScoreEntriesForSubject(UUID subjectId) {
        return reputationScoreEntries.stream().filter(r -> r.getSubject().equals(subjectId))
                .collect(Collectors.toList());
    }

    public Collection<ReputationScoreEntry> calculateStaticReputationScoreEntries(UUID observerId, UUID subjectId) {
        List<ReputationScoreEntry> result = new ArrayList<>();

        // General geopol relations

        IGeopolObjectWrapper observerObj = GeopolUtils.findGeopolObject(observerId);
        IGeopolObjectWrapper subjectObj = GeopolUtils.findGeopolObject(subjectId);

        if (observerObj != null && subjectObj != null) {
            if (observerObj instanceof ITownWrapper observerTown) {
                if (subjectObj instanceof ITownWrapper subjectTown) {
                    // Town observing town

                    // Same region score
                    if (observerTown.getRegion() != null && subjectTown.getRegion() != null
                            && observerTown.getRegion().equals(subjectTown.getRegion())) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "same-region"));
                    }
                    // Same nation score
                    if (observerTown.getNation() != null && subjectTown.getNation() != null
                            && observerTown.getNation().equals(subjectTown.getNation())) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "same-nation"));
                    }

                } else if (subjectObj instanceof IRegionWrapper subjectRegion) {
                    // Town observing region

                    // Our region score
                    if (observerTown.getRegion() != null && observerTown.getRegion().equals(subjectRegion)) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "our-region"));
                    }

                    // Same nation score
                    if (observerTown.getNation() != null && subjectRegion.getNation() != null
                            && observerTown.getNation().equals(subjectRegion.getNation())) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "same-nation"));
                    }

                } else if (subjectObj instanceof INationWrapper subjectNation) {
                    // Town observing nation

                    // Our nation score
                    if (observerTown.getNation() != null && observerTown.getNation().equals(subjectNation)) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "our-nation"));
                    }
                }
            } else if (observerObj instanceof IRegionWrapper observerRegion) {
                if (subjectObj instanceof ITownWrapper subjectTown) {
                    // Region observing town

                    // Our town score
                    if (subjectTown.getRegion() != null && subjectTown.getRegion().equals(observerRegion)) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "our-town"));
                    }

                } else if (subjectObj instanceof IRegionWrapper subjectRegion) {
                    // Region observing region

                    // Same nation score
                    if (observerRegion.getNation() != null
                            && observerRegion.getNation().equals(subjectRegion.getNation())) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "same-nation"));
                    }

                } else if (subjectObj instanceof INationWrapper subjectNation) {
                    // Region observing nation

                    // Our nation score
                    if (observerRegion.getNation() != null && observerRegion.getNation().equals(subjectNation)) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "our-nation"));
                    }
                }
            } else if (observerObj instanceof INationWrapper observerNation) {
                if (subjectObj instanceof ITownWrapper subjectTown) {
                    // Nation observing town

                    // Our town score
                    if (subjectTown.getNation() != null && subjectTown.getNation().equals(observerNation)) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "our-town"));
                    }

                } else if (subjectObj instanceof IRegionWrapper subjectRegion) {
                    // Nation observing region

                    // Our region score
                    if (subjectRegion.getNation() != null && subjectRegion.getNation().equals(observerNation)) {
                        result.add(createReputationScoreEntry(observerId, subjectId, "our-region"));
                    }

                } else if (subjectObj instanceof INationWrapper subjectNation) {
                    // Nation observing nation
                }
            }

        }

        // Rivals and partners

        ActorProfile observerProfile = plugin.getActorProfileManager().getActorProfile(observerId);

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
                        var partnerProfile = plugin.getActorProfileManager().getActorProfile(partnerId);
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
                        var rivalprofile = plugin.getActorProfileManager().getActorProfile(rivalId);
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

        if (plugin.isUnitedWarEnabled()) {
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

    public @Nonnull ReputationScoreEntry getOrCreateReputationScoreEntry(UUID observerId, UUID subjectId,
            String configSectionKey) {

        ReputationScoreEntry entry = getReputationScoreEntryWithKey(observerId, subjectId, configSectionKey);
        if (entry == null) {
            entry = createReputationScoreEntry(observerId, subjectId, configSectionKey);
        }

        return entry;
    }

    private ReputationScoreEntry createReputationScoreEntry(UUID observerId, UUID subjectId, String configSectionKey) {

        var config = plugin.getConfig();

        ReputationScoreEntry entry;
        String description = config.getString("record-definitions." + configSectionKey + ".description",
                "Unknown Reason");
        double lowCap = config.getDouble("record-definitions." + configSectionKey + ".low-cap", -200d);
        double highCap = config.getDouble("record-definitions." + configSectionKey + ".high-cap", 200d);
        double decay = config.getDouble("record-definitions." + configSectionKey + ".decay", 0d);
        double defaultamount = config.getDouble("record-definitions." + configSectionKey + ".default", 0d);
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

        var service = plugin.getDatabaseManager().getReputationScoreEntryService();
        return service.createOrUpdate(entry);
    }

    public boolean removeReputationEntry(ReputationScoreEntry entry) {
        if (entry == null)
            return true;

        reputationScoreEntries.remove(entry);

        var service = plugin.getDatabaseManager().getReputationScoreEntryService();
        return service.delete(entry.getId());
    }

    public void handleReputationChange(IGeopolObjectWrapper observer, IGeopolObjectWrapper subject, double modifier,
            String configKey, Player player, boolean doPassthrough) {

        // Main entry
        var entry = plugin.getReputationManager().getOrCreateReputationScoreEntry(observer.getUUID(), subject.getUUID(),
                configKey);
        entry.setModifier(entry.getModifier() + modifier);

        var msg = messageProvider.get("messages.reputation-changed");
        var prefix = messageProvider.get("messages.prefix");

        if (plugin.getReputationManager().addOrUpdateReputationScoreEntry(entry)) {
            logChange(observer, subject, configKey, entry.getModifier() + modifier);
            if (player != null) {
                Messenger.sendMessage(player, msg,
                        Map.of("name", observer.getName(), "prefix", ColorFormatter.getGeopolPrefixColored(observer),
                                "modifier",
                                ColorFormatter.getAmountColored(entry.getModifier())),
                        prefix);
            }
        }

        // Passthrough entries

        if (!doPassthrough)
            return;

        if (plugin.getConfig().getBoolean("rep-passthrough-up.enabled", false)) {
            double factor = plugin.getConfig().getDouble("rep-passthrough-up.factor");
            if (observer instanceof ITownWrapper town) {
                var region = town.getRegion();
                if (region != null) {
                    var regionEntry = plugin.getReputationManager().getOrCreateReputationScoreEntry(region.getUUID(),
                            subject.getUUID(), configKey);
                    regionEntry.setModifier(regionEntry.getModifier() + (modifier * factor));
                    if (plugin.getReputationManager().addOrUpdateReputationScoreEntry(regionEntry)) {
                        logChange(region, subject, configKey, regionEntry.getModifier() + (modifier * factor));
                        if (player != null) {
                            Messenger.sendMessage(player, msg,
                                    Map.of("name", region.getName(), "prefix",
                                            ColorFormatter.getGeopolPrefixColored(region),
                                            "modifier",
                                            ColorFormatter.getAmountColored(regionEntry.getModifier())),
                                    prefix);
                        }
                    }
                }
                var nation = town.getNation();
                if (nation != null) {
                    var nationEntry = plugin.getReputationManager().getOrCreateReputationScoreEntry(nation.getUUID(),
                            subject.getUUID(), configKey);
                    nationEntry.setModifier(nationEntry.getModifier() + (modifier * factor * factor));
                    if (plugin.getReputationManager().addOrUpdateReputationScoreEntry(nationEntry)) {
                        logChange(nation, subject, configKey, nationEntry.getModifier() + (modifier * factor * factor));
                        if (player != null) {
                            Messenger.sendMessage(player, msg,
                                    Map.of("name", nation.getName(), "prefix",
                                            ColorFormatter.getGeopolPrefixColored(nation),
                                            "modifier",
                                            ColorFormatter.getAmountColored(nationEntry.getModifier())),
                                    prefix);
                        }
                    }
                }
            } else if (observer instanceof IRegionWrapper region) {
                var nation = region.getNation();
                if (nation != null) {
                    var nationEntry = plugin.getReputationManager().getOrCreateReputationScoreEntry(nation.getUUID(),
                            subject.getUUID(), configKey);
                    nationEntry.setModifier(nationEntry.getModifier() + (modifier * factor));
                    if (plugin.getReputationManager().addOrUpdateReputationScoreEntry(nationEntry)) {
                        logChange(nation, subject, configKey, nationEntry.getModifier() + (modifier * factor));
                        if (player != null) {
                            Messenger.sendMessage(player, msg,
                                    Map.of("name", nation.getName(), "prefix",
                                            ColorFormatter.getGeopolPrefixColored(nation),
                                            "modifier",
                                            ColorFormatter.getAmountColored(nationEntry.getModifier())),
                                    prefix);
                        }
                    }
                }
            }
        }
        if (plugin.getConfig().getBoolean("rep-passthrough-down.enabled", false)) {
            double factor = plugin.getConfig().getDouble("rep-passthrough-down.factor");
            if (observer instanceof INationWrapper nation) {
                var regions = nation.getRegions();
                if (regions != null && !regions.isEmpty()) {
                    for (IRegionWrapper region : regions) {
                        var regionEntry = plugin.getReputationManager().getOrCreateReputationScoreEntry(
                                region.getUUID(),
                                subject.getUUID(), configKey);
                        regionEntry.setModifier(regionEntry.getModifier() + (modifier * factor));
                        if (plugin.getReputationManager().addOrUpdateReputationScoreEntry(regionEntry)) {
                            logChange(region, subject, configKey, regionEntry.getModifier() + (modifier * factor));
                            if (player != null) {
                                Messenger.sendMessage(player, msg,
                                        Map.of("name", region.getName(), "prefix",
                                                ColorFormatter.getGeopolPrefixColored(region), "modifier",
                                                ColorFormatter.getAmountColored(regionEntry.getModifier())),
                                        prefix);
                            }
                        }
                    }
                }
                var towns = nation.getTowns();
                if (towns != null && !towns.isEmpty()) {
                    for (ITownWrapper town : towns) {
                        var townEntry = plugin.getReputationManager().getOrCreateReputationScoreEntry(town.getUUID(),
                                subject.getUUID(), configKey);
                        townEntry.setModifier(townEntry.getModifier() + (modifier * factor * factor));
                        if (plugin.getReputationManager().addOrUpdateReputationScoreEntry(townEntry)) {
                            logChange(town, subject, configKey,
                                    townEntry.getModifier() + (modifier * factor * factor));
                            if (player != null) {
                                Messenger.sendMessage(player, msg,
                                        Map.of("name", town.getName(), "prefix",
                                                ColorFormatter.getGeopolPrefixColored(town),
                                                "modifier", ColorFormatter.getAmountColored(townEntry.getModifier())),
                                        prefix);
                            }
                        }
                    }
                }
            } else if (observer instanceof IRegionWrapper region) {
                var towns = region.getTowns();
                if (towns != null && !towns.isEmpty()) {
                    for (ITownWrapper town : towns) {
                        var townEntry = plugin.getReputationManager().getOrCreateReputationScoreEntry(town.getUUID(),
                                subject.getUUID(), configKey);
                        townEntry.setModifier(townEntry.getModifier() + (modifier * factor));
                        if (plugin.getReputationManager().addOrUpdateReputationScoreEntry(townEntry)) {
                            logChange(town, subject, configKey, townEntry.getModifier() + (modifier * factor));
                            if (player != null) {
                                Messenger.sendMessage(player, msg,
                                        Map.of("name", town.getName(), "prefix",
                                                ColorFormatter.getGeopolPrefixColored(town),
                                                "modifier", ColorFormatter.getAmountColored(townEntry.getModifier())),
                                        prefix);
                            }
                        }
                    }
                }
            }
        }
    }

    private void logChange(IGeopolObjectWrapper observer, IGeopolObjectWrapper subject, String key, double amount) {
        Logger.log(
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

            var service = plugin.getDatabaseManager().getReputationScoreEntryService();

            List<ReputationScoreEntry> entriesToRemove = new ArrayList<>();

            var sortedEntries = reputationScoreEntries.stream()
                    .sorted(Comparator.comparing(ReputationScoreEntry::getId)).collect(Collectors.toList());
            for (ReputationScoreEntry entry : sortedEntries) {

                IGeopolObjectWrapper subject = GeopolUtils.findGeopolObject(entry.getObserver());
                IGeopolObjectWrapper target = GeopolUtils.findGeopolObject(entry.getSubject());

                String subjectStr = subject != null ? subject.getName() : entry.getObserver().toString();
                String targetStr = target != null ? target.getName() : entry.getSubject().toString();

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
                        Logger.log("Reputation entry {" + entry.getKey() + "} of " + targetStr + " with " + subjectStr
                                + " reached 0, removed", "UnitedPolitics");
                    }

                } else {
                    entry.setTimestamp(System.currentTimeMillis());
                    entry.setModifier(newModifier);

                    if (service.createOrUpdate(entry)) {
                        Logger.log("Reputation entry {" + entry.getKey() + "} of " + targetStr + " with " + subjectStr
                                + ": " + currentModifier + " → " + newModifier, "UnitedPolitics");
                    } else {
                        Logger.logError("Error saving entry " + entry.getId(), "UnitedPolitics");
                    }
                }
            }

            reputationScoreEntries.removeAll(entriesToRemove);
        }
    }

}
