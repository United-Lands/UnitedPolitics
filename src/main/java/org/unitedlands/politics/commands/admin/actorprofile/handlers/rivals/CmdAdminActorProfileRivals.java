package org.unitedlands.politics.commands.admin.actorprofile.handlers.rivals;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.commands.admin.actorprofile.CmdAdminActorProfile;
import org.unitedlands.registrars.command.UnitedCommandExecutor;

@UnitedSubCommand(
    parent = CmdAdminActorProfile.class,
    name = "rivals",
    usage = "/upa actorprofile rivals <command>"
)
public class CmdAdminActorProfileRivals implements UnitedCommandExecutor {

    @Override
    public void handleCommand(CommandSender sender, String[] args) { }

}
