package org.unitedlands.politics.commands.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.utils.Logger;
import org.unitedlands.utils.Messenger;

public class AdminReloadCommand extends BaseCommandHandler<UnitedPolitics>{

    public AdminReloadCommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        Logger.log("Stopping schedulers...", "UnitedPolitics");
        Logger.log("Reloading configs...", "UnitedPolitics");

        plugin.reloadConfig();
        plugin.getMessageProvider().reload(UnitedPolitics.getInstance().getConfig());

        plugin.getReputationManager().loadReputationRecords();
        
        Logger.log("Starting schedulers...", "UnitedPolitics");
        Logger.log("Done.", "UnitedPolitics");

        Messenger.sendMessage(sender, messageProvider.get("messages.reload"), null, messageProvider.get("messages.prefix"));

    }



}
