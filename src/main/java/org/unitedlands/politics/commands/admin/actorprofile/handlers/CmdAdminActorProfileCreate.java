package org.unitedlands.politics.commands.admin.actorprofile.handlers;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.commands.admin.actorprofile.CmdAdminActorProfile;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.models.ActorProfile;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.unitedlands.classes.GeopolObject;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

@UnitedSubCommand(
    parent = CmdAdminActorProfile.class,
    name = "create",
    usage = "/upa actorprofile create <actor>"
)
public class CmdAdminActorProfileCreate implements UnitedCommandExecutor {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 1) {
            var names1 = UnitedLandsDataManager.instance().getCountryNames();
            var names2 = UnitedLandsDataManager.instance().getSettlementNames();
            names1.addAll(names2);
            return names1;
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sendUsage(sender);
            return;
        }

        GeopolObject actor = UnitedLandsDataManager.instance().getGeopolObject(args[0]);
        if (actor == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[0]);
            return;
        }

        var existingProfile = ActorProfileManager.instance().getActorProfile(actor.getUuid());
        if (existingProfile != null) {
            United.messenger().send(sender, "errors.actorprofile.existing-record");
            return;
        }

        var profile = new ActorProfile(actor.getUuid());
        profile.setTimestamp(System.currentTimeMillis());

        if (ActorProfileManager.instance().addOrUpdateActorProfile(profile)) {
            United.messenger().send(sender, "success.actorprofile.create", args[0]);
        } else {
            United.messenger().send(sender, "errors.general.db-save-error");
        }
    }

}
