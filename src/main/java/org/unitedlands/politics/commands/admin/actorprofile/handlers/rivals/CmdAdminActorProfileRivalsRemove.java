package org.unitedlands.politics.commands.admin.actorprofile.handlers.rivals;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.unitedlands.classes.GeopolObject;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

@UnitedSubCommand(
    parent = CmdAdminActorProfileRivals.class,
    name = "remove",
    usage = "/upa actorprofile rivals remove <actor> <rival>",
    catchAll = true
)
public class CmdAdminActorProfileRivalsRemove implements UnitedCommandExecutor {

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 2) {
            var names1 = UnitedLandsDataManager.instance().getCountryNames();
            var names2 = UnitedLandsDataManager.instance().getSettlementNames();
            names1.addAll(names2);
            return names1;
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sendUsage(sender);
            return;
        }


        GeopolObject actor = UnitedLandsDataManager.instance().getGeopolObject(args[0]);
        if (actor == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[0]);
            return;
        }

        var profile = ActorProfileManager.instance().getActorProfile(actor.getUuid());
        if (profile == null) {
            United.messenger().send(sender, "errors.actorprofile.no-profile");
            return;
        }

        GeopolObject rival = UnitedLandsDataManager.instance().getGeopolObject(args[1]);
        if (rival == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[1]);
            return;
        }

        var actorRivals = profile.getRivals();
        if (actorRivals != null) {
            var rivalEntry = actorRivals.stream().filter(r -> r.equals(rival.getUuid())).findAny().orElse(null);
            if (rivalEntry != null) {

                actorRivals.remove(rival.getUuid());
                profile.setRivals(actorRivals);

                if (ActorProfileManager.instance().addOrUpdateActorProfile(profile)) {
                    var cmd = "upa actorprofile edit " + actor.getName();
                    Bukkit.dispatchCommand(sender, cmd);
                } else {
                    United.messenger().send(sender, "errors.general.db-save-error");
                }
            } else {
                United.messenger().send(sender, "errors.actorprofile.rival-missing");
            }
        } else {
            United.messenger().send(sender, "errors.actorprofile.rival-missing");
        }

    }

}
