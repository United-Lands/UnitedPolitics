package org.unitedlands.politics.commands.admin.actorprofile.handlers.reactions;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.commands.admin.actorprofile.CmdAdminActorProfile;
import org.unitedlands.registrars.command.UnitedCommandExecutor;

@UnitedSubCommand(
    parent = CmdAdminActorProfile.class,
    name = "reactions",
    usage = "/upa actorprofile reactions <command>"
)
public class CmdAdminActorProfileReactions implements UnitedCommandExecutor {

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

    }


}
