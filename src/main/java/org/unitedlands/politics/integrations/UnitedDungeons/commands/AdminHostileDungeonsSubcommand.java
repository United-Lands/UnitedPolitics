package org.unitedlands.politics.integrations.UnitedDungeons.commands;

import org.unitedlands.classes.BaseSubcommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;

public class AdminHostileDungeonsSubcommand extends BaseSubcommandHandler<UnitedPolitics> {

    public AdminHostileDungeonsSubcommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);

    }

    @Override
    protected void registerSubHandlers() {
        subHandlers.put("add", new AdminAddHostileDungeonSubcommand(plugin, messageProvider));
        subHandlers.put("remove", new AdminRemoveHostileDungeonSubcommand(plugin, messageProvider));
    }

}