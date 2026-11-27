package org.unitedlands.politics.integrations.UnitedDungeons.commands.hostiledungeon;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.commands.admin.actorprofile.CmdAdminActorProfile;
import org.unitedlands.registrars.command.UnitedCommandExecutor;

@UnitedSubCommand(
    parent = CmdAdminActorProfile.class,
    name = "hostiledungeon",
    usage = "/upa actorprofile hostiledungeon <command>",
    requirePlugins = { "UnitedDungeons" }
)
public class CmdAdminActorProfileHostileDungeon implements UnitedCommandExecutor {

    @Override
    public void handleCommand(CommandSender sender, String[] args) { }

}
