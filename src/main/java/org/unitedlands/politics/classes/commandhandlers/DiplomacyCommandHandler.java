package org.unitedlands.politics.classes.commandhandlers;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.utils.United;

public class DiplomacyCommandHandler implements UnitedCommandExecutor {

    public record DiplomacyCommandHandlerContext(
            Player player,
            INationWrapper country) {
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
        var nation = UnitedPolitics.instance().getGeopolWrapper().getNationByPlayer(player);
        if (nation == null) {
            United.messenger().send(sender, "null");
            return null;
        }

        if (permission != null) {
            if (!UnitedPolitics.instance().getGeopolWrapper().hasGeopolPermission(player, permission)) {
                United.messenger().send(player, "general-errors.no-permission");
                return null;
            }
        }

        return new DiplomacyCommandHandlerContext(player, nation);
    }



}
