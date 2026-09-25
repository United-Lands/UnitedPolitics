package org.unitedlands.politics.integrations.Towny.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.models.ReputationScoreEntry;
import org.unitedlands.politics.utils.ColorFormatter;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.utils.United;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.object.AddonCommand;

public class TownyTownReputationCommand implements CommandExecutor, TabCompleter {

    public TownyTownReputationCommand() {
        TownyCommandAddonAPI.addSubCommand(new AddonCommand(CommandType.TOWN, "reputation", this));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
            @NotNull String alias, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            var names1 = UnitedPolitics.instance().getGeopolWrapper().getNations().stream().map(INationWrapper::getName)
                    .collect(Collectors.toList());
            var names2 = UnitedPolitics.instance().getGeopolWrapper().getTowns().stream().map(ITownWrapper::getName)
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

        if (args.length == 1) {

            United.messenger().send(sender, "reputation-details-header", args[0]);

            IGeopolObjectWrapper geopolObj = GeopolUtils.findGeopolObject(args[0]);
            if (geopolObj == null) {
                United.messenger().send(sender, "reputation-details-empty", args[0]);
            }

            List<ReputationScoreEntry> allEntries = new ArrayList<>();
            var dynamicEntries = ReputationManager.instance().getReputationScoreEntries(geopolObj.getUUID(),
                    residentTown.getUUID());
            allEntries.addAll(dynamicEntries);

            var staticEntries = ReputationManager.instance().calculateStaticReputationScoreEntries(geopolObj.getUUID(),
                    residentTown.getUUID());
            allEntries.addAll(staticEntries);

            if (allEntries == null || allEntries.isEmpty()) {
                United.messenger().send(sender, "reputation-details-empty", args[0]);
            }

            allEntries = allEntries.stream().sorted(Comparator.comparing(ReputationScoreEntry::getModifier))
                    .collect(Collectors.toList());

            for (var item : allEntries) {

                String scoreStr = ColorFormatter.getAmountColored(item.getModifier());
                String decayStr = ColorFormatter.getAmountColored(item.getDecayRate());

                var timeStamp = item.getTimestamp();
                if (timeStamp == null)
                    timeStamp = 0L;

                var millisecondsSinceTimestamp = (System.currentTimeMillis() - timeStamp);
                var decayThreshold = UnitedPolitics.instance().getConfig().getLong("rep-decay-grace-period") * 1000;

                if (millisecondsSinceTimestamp < decayThreshold) {
                    decayStr = "<gray>Will start decaying in "
                            + United.formatter().formatDuration(decayThreshold - millisecondsSinceTimestamp) + "</gray>";
                }

                United.messenger().send(sender, "reputation-details-entry",
                        item.getDescription(), scoreStr, decayStr);

            }

            United.messenger().send(sender, "reputation-details-footer");

        }

        return true;

    }

}
