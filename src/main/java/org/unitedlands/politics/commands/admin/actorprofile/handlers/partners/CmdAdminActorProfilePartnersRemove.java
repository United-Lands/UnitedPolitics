package org.unitedlands.politics.commands.admin.actorprofile.handlers.partners;

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
    parent = CmdAdminActorProfilePartners.class, 
    name = "add", 
    usage = "/upa actorprofile partners remove <actor> <partner>", 
    catchAll = true
)
public class CmdAdminActorProfilePartnersRemove implements UnitedCommandExecutor {

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

        GeopolObject partner = UnitedLandsDataManager.instance().getGeopolObject(args[1]);
        if (partner == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found");
            return;
        }

        var actorPartners = profile.getPartners();
        if (actorPartners != null) {
            var partnerEntry = actorPartners.stream().filter(r -> r.equals(partner.getUuid())).findAny().orElse(null);
            if (partnerEntry != null) {

                actorPartners.remove(partner.getUuid());
                profile.setPartners(actorPartners);

                if (ActorProfileManager.instance().addOrUpdateActorProfile(profile)) {
                    var cmd = "upa actorprofile edit " + actor.getName();
                    Bukkit.dispatchCommand(sender, cmd);
                } else {
                    United.messenger().send(sender, "errors.general.db-save-error");
                }
            } else {
                United.messenger().send(sender, "errors.actorprofile.partner-missing");
            }
        } else {
            United.messenger().send(sender, "errors.actorprofile.partner-missing");
            return;
        }

    }

}
