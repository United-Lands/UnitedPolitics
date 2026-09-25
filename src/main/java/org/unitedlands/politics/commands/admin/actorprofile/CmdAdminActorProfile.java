package org.unitedlands.politics.commands.admin.actorprofile;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.commands.CmdAdmin;
import org.unitedlands.registrars.command.UnitedCommandExecutor;

@UnitedSubCommand(
    parent = CmdAdmin.class,
    name = "actorprofile"
)
public class CmdAdminActorProfile implements UnitedCommandExecutor {

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

    }

}
