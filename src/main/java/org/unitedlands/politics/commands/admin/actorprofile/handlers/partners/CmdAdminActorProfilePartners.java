package org.unitedlands.politics.commands.admin.actorprofile.handlers.partners;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.commands.admin.actorprofile.CmdAdminActorProfile;
import org.unitedlands.registrars.command.UnitedCommandExecutor;


@UnitedSubCommand(
    parent = CmdAdminActorProfile.class,
    name = "partners",
    usage = "/upa actorprofile partners <command>"
)
public class CmdAdminActorProfilePartners implements UnitedCommandExecutor {

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

    }

}
