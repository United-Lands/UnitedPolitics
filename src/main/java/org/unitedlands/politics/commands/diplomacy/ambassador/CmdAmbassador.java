package org.unitedlands.politics.commands.diplomacy.ambassador;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.commands.CmdDiplomacy;
import org.unitedlands.registrars.command.UnitedCommandExecutor;

@UnitedSubCommand (
    parent = CmdDiplomacy.class,
    name = "ambassador",
    description = "Command for ambassador diplomacy actions",
    usage = "/diplomacy ambassador <command> <args>"
)
public class CmdAmbassador implements UnitedCommandExecutor {

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
    }

}
