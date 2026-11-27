package org.unitedlands.politics.commands.diplomacy.embargo;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.commands.CmdDiplomacy;
import org.unitedlands.registrars.command.UnitedCommandExecutor;

@UnitedSubCommand (
    parent = CmdDiplomacy.class,
    name = "embargo",
    description = "Command for embargo diplomacy actions",
    usage = "/diplomacy embargo <command> <args>"
)

public class CmdDipomacyEmbargo implements UnitedCommandExecutor {

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
    }

}
