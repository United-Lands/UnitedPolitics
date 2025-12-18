package org.unitedlands.politics.commands.admin.actorprofile.handlers;

import org.unitedlands.classes.BaseSubcommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.commands.admin.actorprofile.handlers.reactions.AdminCreateReactionCommand;
import org.unitedlands.politics.commands.admin.actorprofile.handlers.reactions.AdminRemoveReactionCommand;

public class AdminReactionSubcommand extends BaseSubcommandHandler<UnitedPolitics> {

    public AdminReactionSubcommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);

    }

    @Override
    protected void registerSubHandlers() {
        subHandlers.put("create", new AdminCreateReactionCommand(plugin, messageProvider));
        subHandlers.put("remove", new AdminRemoveReactionCommand(plugin, messageProvider));
    }

}
