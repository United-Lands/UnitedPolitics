package org.unitedlands.politics.commands.admin.reputation;

import org.unitedlands.classes.BaseSubcommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.commands.admin.reputation.handlers.AdminAddReputationCommand;
import org.unitedlands.politics.commands.admin.reputation.handlers.AdminCreateReputationCommand;
import org.unitedlands.politics.commands.admin.reputation.handlers.AdminRemoveReputationCommand;
import org.unitedlands.politics.commands.admin.reputation.handlers.AdminSetReputationCommand;

public class AdminReputationSubcommand extends BaseSubcommandHandler<UnitedPolitics> {

    public AdminReputationSubcommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);

    }

    @Override
    protected void registerSubHandlers() {
        subHandlers.put("add", new AdminAddReputationCommand(plugin, messageProvider));
        subHandlers.put("set", new AdminSetReputationCommand(plugin, messageProvider));
        subHandlers.put("remove", new AdminRemoveReputationCommand(plugin, messageProvider));
        subHandlers.put("create", new AdminCreateReputationCommand(plugin, messageProvider));
    }

}
