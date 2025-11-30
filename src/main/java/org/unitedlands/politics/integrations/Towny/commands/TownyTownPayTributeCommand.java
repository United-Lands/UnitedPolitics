package org.unitedlands.politics.integrations.Towny.commands;

import java.util.ArrayList;
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
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.Towny.TownyTownWrapper;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.utils.Messenger;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.object.AddonCommand;

public class TownyTownPayTributeCommand implements CommandExecutor, TabCompleter {

    private final UnitedPolitics plugin;
    private final IMessageProvider messageProvider;

    public TownyTownPayTributeCommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
        TownyCommandAddonAPI.addSubCommand(new AddonCommand(CommandType.TOWN, "paytribute", this));
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
        if (residentTown == null) {
            Messenger.sendMessage(sender, messageProvider.getList("messages.tribute.usage"));
            return false;
        }
        if (!resident.isMayor() && !resident.getTownRanks().contains("co-mayor")) {
            Messenger.sendMessage(sender, messageProvider.getList("messages.errors.general.no-rank"));
            return false;
        }

        IGeopolObjectWrapper target = GeopolUtils.findGeopolObject(args[0]);
        if (target == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.geopol-obj-not-found"),
                    Map.of("obj-name", args[0]), messageProvider.get("messages.prefix"));
            return false;
        }

        double amount = 0;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (Exception ex) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.number-format-error"),
                    Map.of("input", args[1]), messageProvider.get("messages.prefix"));
        }

        if (plugin.getDiplomacyManager().payTribute(new TownyTownWrapper(residentTown), target, amount, player)) {
            Messenger.sendMessage(sender, messageProvider.get("messages.tribute.success"),
                    Map.of("amount", args[1], "target-name", target.getName()), messageProvider.get("messages.prefix"));
            return true;
        } else {
            Messenger.sendMessage(sender, messageProvider.get("messages.tribute.error"),
                    Map.of("amount", args[1], "target-name", target.getName()), messageProvider.get("messages.prefix"));
            return false;
        }

    }
}
