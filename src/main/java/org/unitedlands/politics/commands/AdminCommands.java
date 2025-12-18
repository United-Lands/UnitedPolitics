package org.unitedlands.politics.commands;


import org.unitedlands.classes.BaseCommandExecutor;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.commands.admin.AdminForceNewDayCommand;
import org.unitedlands.politics.commands.admin.AdminReloadCommand;
import org.unitedlands.politics.commands.admin.actorprofile.AdminActorProfileSubcommand;
import org.unitedlands.politics.commands.admin.reputation.AdminReputationSubcommand;

public class AdminCommands extends BaseCommandExecutor<UnitedPolitics> {

    public AdminCommands(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    protected void registerHandlers() {
        handlers.put("reputation", new AdminReputationSubcommand(plugin, messageProvider));
        handlers.put("actorprofile", new AdminActorProfileSubcommand(plugin, messageProvider));
        handlers.put("reload", new AdminReloadCommand(plugin, messageProvider));
        handlers.put("forcenewday", new AdminForceNewDayCommand(plugin, messageProvider));
    }

}
