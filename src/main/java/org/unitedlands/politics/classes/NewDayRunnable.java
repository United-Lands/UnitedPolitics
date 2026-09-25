package org.unitedlands.politics.classes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.models.ReputationScoreEntry;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.utils.United;

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

        var reputationScoreEntries = new ArrayList<>(ReputationManager.instance().getReputationScoreEntries());
        var sortedEntries = reputationScoreEntries.stream()
                .sorted(Comparator.comparing(ReputationScoreEntry::getId)).collect(Collectors.toList());

        for (ReputationScoreEntry entry : sortedEntries) {

            IGeopolObjectWrapper subject = GeopolUtils.findGeopolObject(entry.getObserver());
            IGeopolObjectWrapper target = GeopolUtils.findGeopolObject(entry.getSubject());

            String subjectStr = subject != null ? subject.getName() : entry.getObserver().toString();
            String targetStr = target != null ? target.getName() : entry.getSubject().toString();

            var timeStamp = entry.getTimestamp();
            if (timeStamp == null)
                timeStamp = 0L;

            var millisecondsSinceTimestamp = (System.currentTimeMillis() - timeStamp);
            var decayThreshold = plugin.getConfig().getLong("rep-decay-grace-period") * 1000;

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
                    United.logger().info("Reputation entry {" + entry.getKey() + "} of " + targetStr + " with " + subjectStr
                            + " reached 0, removed", "UnitedPolitics");
                }

            } else {
                entry.setModifier(newModifier);
                if (service.createOrUpdate(entry)) {
                    United.logger().info("Reputation entry {" + entry.getKey() + "} of " + targetStr + " with " + subjectStr
                            + ": " + currentModifier + " → " + newModifier, "UnitedPolitics");
                } else {
                    United.logger().error("Error saving entry " + entry.getId(), "UnitedPolitics");
                }
            }
        }

        reputationScoreEntries.removeAll(entriesToRemove);
        ReputationManager.instance().setReputationScoreEntries(reputationScoreEntries);

    }

}
