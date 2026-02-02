package org.unitedlands.politics.commands.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.utils.Formatter;
import org.unitedlands.utils.Messenger;

public class AdminTimeToNewDayCommand extends BaseCommandHandler<UnitedPolitics> {

    public AdminTimeToNewDayCommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        var timeToNewDay = plugin.getTimeManager().getTimeToNewDay();
        Messenger.sendMessage(sender, "Time to new day: " + Formatter.formatDuration(timeToNewDay), null, messageProvider.get("messages.prefix"));
    }

}
