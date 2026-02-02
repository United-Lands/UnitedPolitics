package org.unitedlands.politics.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.classes.MessageProvider;
import org.unitedlands.politics.models.ReputationScoreEntry;
import org.unitedlands.politics.utils.ColorFormatter;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.utils.Formatter;
import org.unitedlands.utils.Messenger;

import net.kyori.adventure.text.minimessage.MiniMessage;

public class OpinionCommand implements CommandExecutor, TabCompleter {

    private final UnitedPolitics plugin;
    private final MessageProvider messageProvider;

    public OpinionCommand(UnitedPolitics plugin, MessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command label,
            @NotNull String alias, @NotNull String @NotNull [] args) {

        List<String> options = new ArrayList<String>();
        String input = args[args.length - 1];

        if (args.length == 1 || args.length == 2) {
            var names = plugin.getGeopolWrapper().getNations().stream().map(INationWrapper::getName)
                    .collect(Collectors.toList());
            var names2 = plugin.getGeopolWrapper().getTowns().stream().map(ITownWrapper::getName)
                    .collect(Collectors.toList());
            names.addAll(names2);
            options = names;
        }

        return Formatter.getSortedCompletions(input, options);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command label,
            @NotNull String alias, @NotNull String @NotNull [] args) {

        var player = (Player) sender;

        IGeopolObjectWrapper observerObj = GeopolUtils.findGeopolObject(args[0]);
        if (observerObj == null) {
            return false;
        }

        IGeopolObjectWrapper subjectObj = null;
        if (args.length == 1) {
            subjectObj = plugin.getGeopolWrapper().getTownByPlayer(player);
        } else if (args.length == 2) {
            subjectObj = GeopolUtils.findGeopolObject(args[1]);
        }

        if (subjectObj == null) {
            if (args.length == 1) {
                Messenger.sendMessage(sender, messageProvider.get("messages.errors.opinion.no-town"), null,
                        messageProvider.get("messages.prefix"));
            } else if (args.length == 2) {
                Messenger.sendMessage(sender, messageProvider.get("messages.errors.opinion.target-not-found"), null,
                        messageProvider.get("messages.prefix"));
            }
            return false;
        }

        Messenger.sendMessage(sender, messageProvider.getList("messages.opinion-details-header"),
                Map.of("observer-name", observerObj.getName(), "subject-name", subjectObj.getName()));

        List<ReputationScoreEntry> allEntries = new ArrayList<>();
        var dynamicEntries = plugin.getReputationManager().getReputationScoreEntries(observerObj.getUUID(),
                subjectObj.getUUID());
        allEntries.addAll(dynamicEntries);

        var staticEntries = plugin.getReputationManager().calculateStaticReputationScoreEntries(observerObj.getUUID(),
                subjectObj.getUUID());
        allEntries.addAll(staticEntries);

        if (allEntries == null || allEntries.isEmpty()) {
            Messenger.sendMessage(sender, messageProvider.get("messages.opinion-details-empty"),
                    Map.of("observer-name", observerObj.getName(), "subject-name", subjectObj.getName()));
        }

        allEntries = allEntries.stream().sorted(Comparator.comparing(ReputationScoreEntry::getModifier))
                .collect(Collectors.toList());

        for (var item : allEntries) {

            String scoreStr = ColorFormatter.getAmountColored(item.getModifier());
            String decayStr = ColorFormatter.getAmountColored(item.getDecayRate());

            Messenger.sendMessage(sender, messageProvider.get("messages.opinion-details-entry"),
                    Map.of("description", item.getDescription(), "score", scoreStr, "decay", decayStr));

        }

        var total = Math.max(-200, Math.min(200, allEntries.stream().collect(Collectors.summingDouble(ReputationScoreEntry::getModifier))));

        Messenger.sendMessage(sender, "<white><bold>Total: " + ColorFormatter.getAmountColored(total) + "</bold></white>");

        Messenger.sendMessage(sender, messageProvider.getList("messages.opinion-details-footer"));

        // if (args.length == 1) {
        // var scores =
        // plugin.getReputationManager().getTotalReputationScores(observerObj.getUUID());
        // List<String> scoreList = new ArrayList<>();
        // for (var entry : scores.entrySet()) {
        // String prefString = ColorFormatter.getGeopolPrefixColored(entry.getKey());
        // String nameStr = entry.getKey().getName();
        // String score = ColorFormatter.getAmountColored(entry.getValue());
        // scoreList.add(prefString + " " + nameStr + ": " + score);
        // }

        // String msg = "<gold><bold>" + observerObj.getName() + ":</bold></gold> " +
        // String.join(", ", scoreList);
        // Messenger.sendMessage(sender, msg, null,
        // messageProvider.get("messages.prefix"));
        // }

        // if (args.length == 2) {

        // IGeopolObjectWrapper subjectObj = GeopolUtils.findGeopolObject(args[1]);
        // if (subjectObj == null) {
        // return false;
        // }

        // var score =
        // plugin.getReputationManager().getTotalReputationScore(observerObj.getUUID(),
        // subjectObj.getUUID());
        // String scoreStr = ColorFormatter.getAmountColored(score);

        // String msg = "<gold><bold>" + observerObj.getName() + " → " +
        // subjectObj.getName() + ":</bold></gold> "
        // + scoreStr;
        // Messenger.sendMessage(sender, msg, null,
        // messageProvider.get("messages.prefix"));
        // }

        return false;
    }

}
