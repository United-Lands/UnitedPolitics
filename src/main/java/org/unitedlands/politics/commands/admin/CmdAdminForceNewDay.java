package org.unitedlands.politics.commands.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.commands.CmdAdmin;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.utils.United;

@UnitedSubCommand (
    parent = CmdAdmin.class,
    name = "forcenewday"
)
public class CmdAdminForceNewDay implements UnitedCommandExecutor {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        ReputationManager.instance().doReputationDecay();

        United.messenger().send(sender, "forced-new-day");
    }

}
