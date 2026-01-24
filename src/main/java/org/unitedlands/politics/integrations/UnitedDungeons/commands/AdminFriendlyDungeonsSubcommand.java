package org.unitedlands.politics.integrations.UnitedDungeons.commands;

import org.unitedlands.classes.BaseSubcommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;

public class AdminFriendlyDungeonsSubcommand extends BaseSubcommandHandler<UnitedPolitics> {

    public AdminFriendlyDungeonsSubcommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);

    }

    @Override
    protected void registerSubHandlers() {
        subHandlers.put("add", new AdminAddFriendlyDungeonSubcommand(plugin, messageProvider));
        subHandlers.put("remove", new AdminRemoveFriendlyDungeonSubcommand(plugin, messageProvider));
    }

}