package org.unitedlands.politics.classes.commandhandlers;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.unitedlands.classes.Country;
import org.unitedlands.unitedlands.managers.PermissionManager;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

public class DiplomacyCommandHandler implements UnitedCommandExecutor {

    public record DiplomacyCommandHandlerContext(
            Player player,
            Country country) {
    }

    @Override
    public void handleCommand(CommandSender arg0, String[] arg1) {

    }

    @Override
    public List<String> handleTab(CommandSender arg0, String[] arg1) {
        return null;
    }

    protected DiplomacyCommandHandlerContext validate(CommandSender sender, String permission) {

        var player = (Player) sender;
        var nation = UnitedLandsDataManager.instance().getCountry(player);
        if (nation == null) {
            United.messenger().send(sender, "null");
            return null;
        }

        if (permission != null) {
            if (!PermissionManager.instance().hasRankPermission(permission, player)) {
                United.messenger().send(player, "general-errors.no-permission");
                return null;
            }
        }

        return new DiplomacyCommandHandlerContext(player, nation);
    }



}
