package org.unitedlands.politics.commands.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.commands.CmdAdmin;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.managers.TimeManager;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.utils.United;


@UnitedSubCommand (
    parent = CmdAdmin.class,
    name = "reload"
)
public class CmdAdminReload implements UnitedCommandExecutor {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        United.logger().info("Stopping schedulers...", "UnitedPolitics");
        TimeManager.instance().cancelScheduledNewDay();
        United.logger().info("Reloading configs...", "UnitedPolitics");

        UnitedPolitics.instance().reloadConfig();

        United.logger().info("Loading database entries...", "UnitedPolitics");

        ReputationManager.instance().loadReputationRecords();
        ActorProfileManager.instance().loadActorProfiles();
        
        United.logger().info("Starting schedulers...", "UnitedPolitics");
        TimeManager.instance().scheduleNewDay();
        United.logger().info("Done.", "UnitedPolitics");

        United.messenger().send(sender, "reload");

    }



}
