package org.unitedlands.politics.commands.diplomacy.embargo;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.classes.commandhandlers.DiplomacyCommandHandler;
import org.unitedlands.politics.managers.DiplomacyManager;
import org.unitedlands.utils.United;

@UnitedSubCommand (
    parent = CmdDipomacyEmbargo.class,
    name = "list",
    description = "Command for listing embargoes",
    usage = "/diplomacy embargo remove <country>"
)

public class CmdDipomacyEmbargoList extends DiplomacyCommandHandler {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        
        var context = validate(sender, null);
        if (context == null)
            return;

        var record = DiplomacyManager.instance().getOrCreateDiplomacyRecord(context.country());
        
        United.messenger().send(sender, "embargo.list-header", context.country().getDisplayName());

        if (record.getEmbargoes().size() == 0) {
                United.messenger().send(sender, "embargo.empty");
        } else {
            for (var c : record.getEmbargoes()) {
                United.messenger().send(sender, "embargo.entry", c.getDisplayName());
            }
        }

    }

}
