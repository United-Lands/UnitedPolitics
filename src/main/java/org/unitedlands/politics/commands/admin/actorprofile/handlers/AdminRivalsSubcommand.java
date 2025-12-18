package org.unitedlands.politics.commands.admin.actorprofile.handlers;

import org.unitedlands.classes.BaseSubcommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.commands.admin.actorprofile.handlers.rivals.AdminAddRivalCommand;
import org.unitedlands.politics.commands.admin.actorprofile.handlers.rivals.AdminRemoveRivalCommand;

public class AdminRivalsSubcommand extends BaseSubcommandHandler<UnitedPolitics> {

    public AdminRivalsSubcommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);

    }

    @Override
    protected void registerSubHandlers() {
        subHandlers.put("add", new AdminAddRivalCommand(plugin, messageProvider));
        subHandlers.put("remove", new AdminRemoveRivalCommand(plugin, messageProvider));
    }

}
