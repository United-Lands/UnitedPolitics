package org.unitedlands.politics.integrations.Towny.commands;

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
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.models.ReputationScoreEntry;
import org.unitedlands.politics.utils.ColorFormatter;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.utils.Messenger;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.object.AddonCommand;

public class TownyTownReputationCommand implements CommandExecutor, TabCompleter {
    
    private final UnitedPolitics plugin;
    private final IMessageProvider messageProvider;

    public TownyTownReputationCommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
        TownyCommandAddonAPI.addSubCommand(new AddonCommand(CommandType.TOWN, "reputation", this));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
            @NotNull String alias, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            var names1 = plugin.getGeopolWrapper().getNations().stream().map(INationWrapper::getName)
                    .collect(Collectors.toList());
            var names2 = plugin.getGeopolWrapper().getTowns().stream().map(ITownWrapper::getName)
                    .collect(Collectors.toList());
            names1.addAll(names2);
            return names1;
        }
        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias,
            @NotNull String @NotNull [] args) {
        var player = (Player) sender;
        var resident = TownyAPI.getInstance().getResident(player);

        var residentTown = resident.getTownOrNull();
        if (residentTown == null)
            return false;

        if (args.length == 0) {

            Messenger.sendMessage(sender, messageProvider.getList("messages.reputation-info-header"));

            var records = plugin.getReputationManager().getReputationEntriesForTarget(residentTown.getUUID());
            if (records == null || records.isEmpty()) {
                Messenger.sendMessage(sender, messageProvider.get("messages.reputation-info-empty"));
                Messenger.sendMessage(sender, messageProvider.getList("messages.reputation-entry-footer"));
                return false;
            }

            var grouped = records.stream().collect(Collectors.groupingBy(ReputationScoreEntry::getSubject));

            for (var item : grouped.entrySet()) {
                var geopolObject = GeopolUtils.findGeopolObject(item.getKey());
                if (geopolObject == null)
                    continue;

                Double score = item.getValue().stream()
                        .collect(Collectors.summingDouble(ReputationScoreEntry::getModifier));

                String scoreStr = ColorFormatter.getAmountColored(score);
                String prefixStr = ColorFormatter.getGeopolPrefixColored(geopolObject);

                Messenger.sendMessage(sender, messageProvider.get("messages.reputation-info-entry"),
                        Map.of("prefix", prefixStr, "subject-name", geopolObject.getName(), "score", scoreStr));
            }

            Messenger.sendMessage(sender, messageProvider.getList("messages.reputation-entry-footer"));
        }

        if (args.length == 1) {

            Messenger.sendMessage(sender, messageProvider.getList("messages.reputation-details-header"),
                    Map.of("subject-name", args[0]));

            IGeopolObjectWrapper geopolObj = GeopolUtils.findGeopolObject(args[0]);
            if (geopolObj == null) {
                Messenger.sendMessage(sender, messageProvider.get("messages.reputation-details-empty"),
                        Map.of("subject-name", args[0]));
            }

            var entries = plugin.getReputationManager().getReputationEntriesForSubject(geopolObj.getUUID(),
                    residentTown.getUUID());
            if (entries == null || entries.isEmpty()) {
                Messenger.sendMessage(sender, messageProvider.get("messages.reputation-details-empty"),
                        Map.of("subject-name", args[0]));
            }

            entries = entries.stream().sorted(Comparator.comparing(ReputationScoreEntry::getModifier))
                    .collect(Collectors.toList());

            for (var item : entries) {

                String scoreStr = ColorFormatter.getAmountColored(item.getModifier());
                String decayStr = ColorFormatter.getAmountColored(item.getDecayRate());
                
                Messenger.sendMessage(sender, messageProvider.get("messages.reputation-details-entry"),
                        Map.of("description", item.getDescription(), "score", scoreStr, "decay", decayStr));

            }

            Messenger.sendMessage(sender, messageProvider.getList("messages.reputation-details-footer"));

        }

        return true;

    }

}
