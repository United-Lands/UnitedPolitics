package org.unitedlands.politics.commands.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.utils.Messenger;

public class AdminForceNewDayCommand extends BaseCommandHandler<UnitedPolitics> {

    public AdminForceNewDayCommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getReputationManager().doReputationDecay();
        });

        Messenger.sendMessage(sender, messageProvider.get("messages.forced-new-day"), null,
                messageProvider.get("messages.prefix"));
    }

}
