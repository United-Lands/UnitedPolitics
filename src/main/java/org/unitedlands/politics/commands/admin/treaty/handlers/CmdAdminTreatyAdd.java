package org.unitedlands.politics.commands.admin.treaty.handlers;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.commands.admin.treaty.CmdAdminTreaty;
import org.unitedlands.politics.managers.TreatyManager;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.utils.United;

@UnitedSubCommand(
    parent = CmdAdminTreaty.class, 
    name = "add",
    usage = "/upa treaty add <treaty_name> <country>", 
    catchAll = true)

public class CmdAdminTreatyAdd implements UnitedCommandExecutor {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return switch (args.length) {
            case 1 -> TreatyManager.instance().getTreatyNames();
            case 2 -> UnitedPolitics.instance().getGeopolWrapper().getNations().stream().map(INationWrapper::getName)
                    .toList();
            default -> null;
        };
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {

        if (args.length != 2) {
            sendUsage(sender);
            return;
        }

        var treaty = TreatyManager.instance().getTreaty(args[0]);
        if (treaty == null) {
            United.messenger().send(sender, "errors.treaty.unknown-treaty", args[0]);
            return;
        }

        var country = UnitedPolitics.instance().getGeopolWrapper().getNation(args[1]);
        if (country == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[1]);
            return;
        }

        if (treaty.getMembers().contains(country)) {
            United.messenger().send(sender, "errors.treaty.already-member", args[1]);
            return;
        }

        treaty.addMember(country);
        TreatyManager.instance().addOrUpdateTreaty(treaty);

        United.messenger().broadcast("success.treaty.broadcast-join", country.getCleanName(), treaty.getCleanName());

    }

}
