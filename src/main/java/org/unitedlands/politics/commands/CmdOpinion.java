package org.unitedlands.politics.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.annotations.UnitedCommand;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.models.ReputationScoreEntry;
import org.unitedlands.politics.utils.ColorFormatter;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.utils.United;

@UnitedCommand(name = "opinion", usage = "/opinion <observer> [subject]")

public class CmdOpinion implements UnitedCommandExecutor {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 1 || args.length == 2) {
            var names = UnitedPolitics.instance().getGeopolWrapper().getNations().stream().map(INationWrapper::getName)
                    .collect(Collectors.toList());
            var names2 = UnitedPolitics.instance().getGeopolWrapper().getTowns().stream().map(ITownWrapper::getName)
                    .collect(Collectors.toList());
            names.addAll(names2);
            return names;
        }
        return null;
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        var player = (Player) sender;

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        IGeopolObjectWrapper observerObj = GeopolUtils.findGeopolObject(args[0]);
        if (observerObj == null) {
            return;
        }

        IGeopolObjectWrapper subjectObj = null;
        if (args.length == 1) {
            subjectObj = UnitedPolitics.instance().getGeopolWrapper().getTownByPlayer(player);
        } else if (args.length == 2) {
            subjectObj = GeopolUtils.findGeopolObject(args[1]);
        }

        if (subjectObj == null) {
            if (args.length == 1) {
                United.messenger().send(sender, "errors.opinion.no-town");
            } else if (args.length == 2) {
                United.messenger().send(sender, "errors.opinion.target-not-found");
            }
            return;
        }

        United.messenger().send(sender, "opinion-details-header", false,
                new Object[] { observerObj.getCleanName(), subjectObj.getCleanName() });

        List<ReputationScoreEntry> allEntries = new ArrayList<>();
        var dynamicEntries = ReputationManager.instance().getReputationScoreEntries(observerObj.getUUID(),
                subjectObj.getUUID());
        allEntries.addAll(dynamicEntries);

        var staticEntries = ReputationManager.instance().calculateStaticReputationScoreEntries(observerObj.getUUID(),
                subjectObj.getUUID());
        allEntries.addAll(staticEntries);

        if (allEntries == null || allEntries.isEmpty()) {
            United.messenger().send(sender, "opinion-details-empty", false,
                    new Object[] { subjectObj.getCleanName() });
        }

        allEntries = allEntries.stream().sorted(Comparator.comparing(ReputationScoreEntry::getModifier))
                .collect(Collectors.toList());

        for (var item : allEntries) {

            String scoreStr = ColorFormatter.getAmountColored(item.getModifier());
            String decayStr = "(<gray>" + ColorFormatter.getAmountColored(item.getDecayRate()) + "/d</gray>)";

            var timeStamp = item.getTimestamp();
            if (timeStamp == null)
                timeStamp = 0L;

            var millisecondsSinceTimestamp = (System.currentTimeMillis() - timeStamp);
            var decayThreshold = UnitedPolitics.instance().getConfig().getLong("rep-decay-grace-period") * 1000;

            if (millisecondsSinceTimestamp < decayThreshold) {
                decayStr = "(<gray>Will start decaying in "
                        + United.formatter().formatDuration(decayThreshold - millisecondsSinceTimestamp) + "</gray>)";
            }

            United.messenger().send(sender, "opinion-details-entry", false,
                    new Object[] { item.getDescription(), scoreStr, item.getDecayRate() != 0 ? decayStr : ""});

        }

        var total = Math.max(-200, Math.min(200,
                allEntries.stream().collect(Collectors.summingDouble(ReputationScoreEntry::getModifier))));

        United.messenger().sendRaw(sender,
                "<white><bold>Total: " + ColorFormatter.getAmountColored(total) + "</bold></white>", false,
                new Object[0]);

        United.messenger().send(sender, "opinion-details-footer", false, new Object[0]);

    }

}
