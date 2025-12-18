package org.unitedlands.politics.commands.admin.actorprofile.handlers;

import org.unitedlands.classes.BaseSubcommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.commands.admin.actorprofile.handlers.partners.AdminAddPartnerCommand;
import org.unitedlands.politics.commands.admin.actorprofile.handlers.partners.AdminRemovePartnerCommand;

public class AdminPartnersSubcommand extends BaseSubcommandHandler<UnitedPolitics> {

    public AdminPartnersSubcommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);

    }

    @Override
    protected void registerSubHandlers() {
        subHandlers.put("add", new AdminAddPartnerCommand(plugin, messageProvider));
        subHandlers.put("remove", new AdminRemovePartnerCommand(plugin, messageProvider));
    }

}
