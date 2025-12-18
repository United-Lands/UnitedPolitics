package org.unitedlands.politics.commands.admin.actorprofile.handlers.partners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.utils.Messenger;

public class AdminRemovePartnerCommand extends BaseCommandHandler<UnitedPolitics> {

    public AdminRemovePartnerCommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 2) {
            var names1 = plugin.getGeopolWrapper().getNations().stream().map(INationWrapper::getName)
                    .collect(Collectors.toList());
            var names2 = plugin.getGeopolWrapper().getTowns().stream().map(ITownWrapper::getName)
                    .collect(Collectors.toList());
            names1.addAll(names2);
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            Messenger.sendMessage(sender, messageProvider.getList("messages.usages.partner.remove"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        IGeopolObjectWrapper actor = GeopolUtils.findGeopolObject(args[0]);
        if (actor == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.geopol-obj-not-found"),
                    Map.of("obj-name", args[0]), messageProvider.get("messages.prefix"));
            return;
        }

        var profile = plugin.getActorProfileManager().getActorProfile(actor.getUUID());
        if (profile == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.actorprofile.no-profile"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        IGeopolObjectWrapper partner = GeopolUtils.findGeopolObject(args[1]);
        if (partner == null) {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.geopol-obj-not-found"),
                    Map.of("obj-name", args[1]), messageProvider.get("messages.prefix"));
            return;
        }

        var actorPartners = profile.getPartners();
        if (actorPartners != null) {
            var partnerEntry = actorPartners.stream().filter(r -> r.equals(partner.getUUID())).findAny().orElse(null);
            if (partnerEntry != null) {

                actorPartners.remove(partner.getUUID());
                profile.setPartners(actorPartners);

                if (plugin.getActorProfileManager().addOrUpdateActorProfile(profile)) {
                    var cmd = "upa actorprofile edit " + actor.getName();
                    Bukkit.dispatchCommand(sender, cmd);
                } else {
                    Messenger.sendMessage(sender, messageProvider.get("messages.errors.general.db-save-error"),
                            null, messageProvider.get("messages.prefix"));
                }
            } else {
                Messenger.sendMessage(sender, messageProvider.get("messages.errors.actorprofile.partner-missing"), null,
                        messageProvider.get("messages.prefix"));
            }
        } else {
            Messenger.sendMessage(sender, messageProvider.get("messages.errors.actorprofile.partner-missing"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

    }

}
