package org.unitedlands.politics.classes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.models.ReputationScoreEntry;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.utils.Logger;

public class NewDayRunnable implements Runnable {

    private final UnitedPolitics plugin;

    private static final Object NEW_DAY_LOCK = new Object();

    public NewDayRunnable(UnitedPolitics plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        synchronized (NEW_DAY_LOCK) {
            doReputationDecay();
        }
        plugin.getTimeManager().scheduleNewDay();
    }

    public void doReputationDecay() {

        var service = plugin.getDatabaseManager().getReputationScoreEntryService();

        List<ReputationScoreEntry> entriesToRemove = new ArrayList<>();

        var reputationScoreEntries = new ArrayList<>(plugin.getReputationManager().getReputationScoreEntries());
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
        plugin.getReputationManager().setReputationScoreEntries(reputationScoreEntries);

    }

}
