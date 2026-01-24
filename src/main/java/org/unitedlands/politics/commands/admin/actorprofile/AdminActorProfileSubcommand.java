package org.unitedlands.politics.commands.admin.actorprofile;

import org.unitedlands.classes.BaseSubcommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.commands.admin.actorprofile.handlers.AdminCreateActorProfileCommand;
import org.unitedlands.politics.commands.admin.actorprofile.handlers.AdminEditActorProfileCommand;
import org.unitedlands.politics.commands.admin.actorprofile.handlers.AdminPartnersSubcommand;
import org.unitedlands.politics.commands.admin.actorprofile.handlers.AdminReactionSubcommand;
import org.unitedlands.politics.commands.admin.actorprofile.handlers.AdminRivalsSubcommand;
import org.unitedlands.politics.integrations.UnitedDungeons.commands.AdminFriendlyDungeonsSubcommand;
import org.unitedlands.politics.integrations.UnitedDungeons.commands.AdminHostileDungeonsSubcommand;

public class AdminActorProfileSubcommand extends BaseSubcommandHandler<UnitedPolitics> {

    public AdminActorProfileSubcommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);

    }

    @Override
    protected void registerSubHandlers() {
        subHandlers.put("reaction", new AdminReactionSubcommand(plugin, messageProvider));
        subHandlers.put("rivals", new AdminRivalsSubcommand(plugin, messageProvider));
        subHandlers.put("partners", new AdminPartnersSubcommand(plugin, messageProvider));
        subHandlers.put("create", new AdminCreateActorProfileCommand(plugin, messageProvider));
        subHandlers.put("edit", new AdminEditActorProfileCommand(plugin, messageProvider));

        if (plugin.isUnitedDungeonsEnabled())
        {
            subHandlers.put("hostiledungeon", new AdminHostileDungeonsSubcommand(plugin, messageProvider));
            subHandlers.put("friendlydungeon", new AdminFriendlyDungeonsSubcommand(plugin, messageProvider));
        }
    }

}
