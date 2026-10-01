package org.unitedlands.politics.commands.admin.treaty;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.commands.CmdAdmin;
import org.unitedlands.registrars.command.UnitedCommandExecutor;

@UnitedSubCommand (
    parent = CmdAdmin.class,
    name = "treaty"
)
public class CmdAdminTreaty implements UnitedCommandExecutor {

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

    }

}
