package org.unitedlands.politics.commands;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedCommand;
import org.unitedlands.registrars.command.UnitedCommandExecutor;

@UnitedCommand (
    name = "unitedpoliticsadmin",
    aliases = { "upa" },
    usage = "/upa <command> <arguments>",
    permission = "united.politics.admin"
)
public class CmdAdmin implements UnitedCommandExecutor {

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

    }



    // @Override
    // protected void registerHandlers() {
    //     handlers.put("reputation", new AdminReputationSubcommand(plugin, messageProvider));
    //     handlers.put("actorprofile", new AdminActorProfileSubcommand(plugin, messageProvider));
    //     handlers.put("reload", new AdminReloadCommand(plugin, messageProvider));
    //     handlers.put("forcenewday", new AdminForceNewDayCommand(plugin, messageProvider));
    //     handlers.put("timetonewday", new AdminTimeToNewDayCommand(plugin, messageProvider));
    // }

}
