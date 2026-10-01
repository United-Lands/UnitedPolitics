package org.unitedlands.politics.commands.admin.treaty.handlers;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.commands.admin.treaty.CmdAdminTreaty;
import org.unitedlands.politics.managers.TreatyManager;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.utils.United;

@UnitedSubCommand(
    parent = CmdAdminTreaty.class, 
    name = "delete",
    usage = "/upa treaty delete <treaty_name>", 
    catchAll = true)

public class CmdAdminTreatyDelete implements UnitedCommandExecutor {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return switch (args.length) {
            case 1 -> TreatyManager.instance().getTreatyNames();
            default -> null;
        };
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        if (args.length != 1) {
            sendUsage(sender);
            return;
        }

        var treaty = TreatyManager.instance().getTreaty(args[0]);
        if (treaty == null) {
            United.messenger().send(sender, "errors.treaty.unknown-treaty", args[0]);
            return;
        }

        TreatyManager.instance().removeTreaty(treaty);

        United.messenger().broadcast("success.treaty.broadcast-deleted", treaty.getCleanName());

    }

}
