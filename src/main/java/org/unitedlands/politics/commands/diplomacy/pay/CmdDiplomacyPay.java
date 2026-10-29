package org.unitedlands.politics.commands.diplomacy.pay;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.commands.CmdDiplomacy;
import org.unitedlands.registrars.command.UnitedCommandExecutor;

@UnitedSubCommand (
    parent = CmdDiplomacy.class,
    name = "pay",
    description = "Command for payable diplomacy actions",
    usage = "/diplomacy pay <command> <args>"
)
public class CmdDiplomacyPay implements UnitedCommandExecutor {

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
    }

}
