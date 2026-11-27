package org.unitedlands.politics.classes;

import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.managers.ReputationManager;

public class NewDayRunnable implements Runnable {

    private final UnitedPolitics plugin;

    public NewDayRunnable(UnitedPolitics plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ReputationManager.instance().doReputationDecay();
        plugin.getTimeManager().scheduleNewDay();
    }

}
