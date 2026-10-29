package org.unitedlands.politics.commands;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedCommand;
import org.unitedlands.registrars.command.UnitedCommandExecutor;

@UnitedCommand (
    name        = "diplomacy",
    aliases     = { "d", "dip"},
    description = "Main diplomacy command",
    usage       = "/diplomacy <cmd> [args]"
)
public class CmdDiplomacy implements UnitedCommandExecutor {

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
    }

}
