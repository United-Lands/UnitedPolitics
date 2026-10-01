package org.unitedlands.politics.commands.admin.treaty.handlers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.classes.Treaty;
import org.unitedlands.politics.classes.TreatyType;
import org.unitedlands.politics.commands.admin.treaty.CmdAdminTreaty;
import org.unitedlands.politics.managers.TreatyManager;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.utils.United;

@UnitedSubCommand(parent = CmdAdminTreaty.class, name = "create", usage = "/upa treaty create <type> <founding_country> <treaty_name>", catchAll = true)
public class CmdAdminTreatyCreate implements UnitedCommandExecutor {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return switch (args.length) {
            case 1 -> Arrays.stream(TreatyType.values()).map(Enum::toString).toList();
            case 2 -> UnitedPolitics.instance().getGeopolWrapper().getNations().stream().map(INationWrapper::getName).toList();
            default -> null;
        };
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sendUsage(sender);
            return;
        }

        var type = TreatyType.ALLIANCE;
        try {
            type = TreatyType.valueOf(args[0]);
        } catch (Exception ex) {
            return;
        }

        var country = UnitedPolitics.instance().getGeopolWrapper().getNation(args[1]);
        if (country == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[1]);
            return;
        }

        var name = args[2];

        var treaty = new Treaty();
        treaty.setId(UUID.randomUUID());
        treaty.setTimestamp(System.currentTimeMillis());
        treaty.setType(type);
        treaty.setName(name);
        treaty.addMember(country);

        TreatyManager.instance().addOrUpdateTreaty(treaty);

        United.messenger().broadcast("success.treaty.broadcast-create", country.getCleanName(), type.toString().toLowerCase(), treaty.getCleanName());

    }

}
