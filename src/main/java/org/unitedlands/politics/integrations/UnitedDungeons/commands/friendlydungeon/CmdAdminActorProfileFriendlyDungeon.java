package org.unitedlands.politics.integrations.UnitedDungeons.commands.friendlydungeon;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.commands.admin.actorprofile.CmdAdminActorProfile;
import org.unitedlands.registrars.command.UnitedCommandExecutor;

@UnitedSubCommand(
    parent = CmdAdminActorProfile.class,
    name = "friendlydungeon",
    usage = "/upa actorprofile friendlydungeon <command>",
    requirePlugins = { "UnitedDungeons" }
)
public class CmdAdminActorProfileFriendlyDungeon implements UnitedCommandExecutor {

    @Override
    public void handleCommand(CommandSender sender, String[] args) { }

}
