package org.unitedlands.politics.commands.admin.actorprofile.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.unitedlands.classes.BaseCommandHandler;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.integrations.UnitedDungeons.utils.DungeonsActorComponentUtils;
import org.unitedlands.politics.utils.ColorFormatter;
import org.unitedlands.politics.utils.GeopolUtils;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.politics.wrappers.interfaces.INationWrapper;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.utils.Messenger;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AdminEditActorProfileCommand extends BaseCommandHandler<UnitedPolitics> {

    public AdminEditActorProfileCommand(UnitedPolitics plugin, IMessageProvider messageProvider) {
        super(plugin, messageProvider);
    }

    @Override
    public List<String> handleTab(CommandSender sender, String[] args) {
        if (args.length == 1) {
            var names1 = plugin.getGeopolWrapper().getNations().stream().map(INationWrapper::getName)
                    .collect(Collectors.toList());
            var names2 = plugin.getGeopolWrapper().getTowns().stream().map(ITownWrapper::getName)
                    .collect(Collectors.toList());
            names1.addAll(names2);
            return names1;
        }
        return new ArrayList<>();
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            Messenger.sendMessage(sender, messageProvider.getList("messages.usages.actorprofile.edit"), null,
                    messageProvider.get("messages.prefix"));
            return;
        }

        // Argument can either be a UUID or a name, try UUID first and then fall back to
        // name.
        IGeopolObjectWrapper actor = null;
        try {
            actor = GeopolUtils.findGeopolObject(UUID.fromString(args[0]));
        } catch (Exception ignore) {
            actor = GeopolUtils.findGeopolObject(args[0]);
        }
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

        Messenger.sendMessage(sender, messageProvider.get("messages.actorprofile.header"),
                Map.of("name", actor.getName()));

        // --------------
        // Reactions
        // --------------

        Messenger.sendMessage(sender, messageProvider.get("messages.actorprofile.reactions-header"),
                Map.of("name", actor.getName()));

        if (profile.getEventReactions() != null) {
            for (var reaction : profile.getEventReactions()) {
                Component reactionComponent = Messenger.getMessage(
                        messageProvider.get("messages.actorprofile.reactions-line"),
                        Map.of("event-key", reaction.getEventKey(), "record-key", reaction.getReactionReputationKey(),
                                "amount", ColorFormatter.getAmountColored(reaction.getAmount())),
                        null);

                var reactionRemoveComponent = MiniMessage.miniMessage()
                        .deserialize(" <dark_gray>[<red>-</red>]</dark_gray>")
                        .clickEvent(ClickEvent.runCommand(
                                "/upa actorprofile reaction remove " + actor.getName() + " " + reaction.getEventKey()));

                Messenger.send(sender, reactionComponent.append(reactionRemoveComponent));
            }
        }

        Component reactionAddComponent = MiniMessage.miniMessage()
                .deserialize("<dark_gray>[<green>+</green>]</dark_gray>")
                .clickEvent(ClickEvent.suggestCommand("/upa actorprofile reaction create " + actor.getName() + " "));
        Messenger.send(sender, reactionAddComponent);

        // --------------
        // Rivals
        // --------------

        Messenger.sendMessage(sender, messageProvider.get("messages.actorprofile.rivals-header"));

        if (profile.getRivals() != null) {
            for (var rivalId : profile.getRivals()) {
                var rival = GeopolUtils.findGeopolObject(rivalId);
                if (rival == null)
                    continue;

                Component rivalComponent = Messenger.getMessage(
                        messageProvider.get("messages.actorprofile.rivals-line"),
                        Map.of("rival", rival.getName()), null);

                var rivalRemoveComponent = MiniMessage.miniMessage()
                        .deserialize(" <dark_gray>[<red>-</red>]</dark_gray>")
                        .clickEvent(ClickEvent
                                .runCommand(
                                        "/upa actorprofile rivals remove " + actor.getName() + " " + rival.getName()));

                Messenger.send(sender, rivalComponent.append(rivalRemoveComponent));
            }
        }

        Component rivalAddComponent = MiniMessage.miniMessage().deserialize("<dark_gray>[<green>+</green>]</dark_gray>")
                .clickEvent(ClickEvent.suggestCommand("/upa actorprofile rivals add " + actor.getName() + " "));
        Messenger.send(sender, rivalAddComponent);

        // --------------
        // Partners
        // --------------

        Messenger.sendMessage(sender, messageProvider.get("messages.actorprofile.partners-header"));

        if (profile.getPartners() != null) {
            for (var partnerId : profile.getPartners()) {
                var partner = GeopolUtils.findGeopolObject(partnerId);
                if (partner == null)
                    continue;

                Component partnerComponent = Messenger.getMessage(
                        messageProvider.get("messages.actorprofile.partners-line"),
                        Map.of("partner", partner.getName()), null);

                var removePartnerComponent = MiniMessage.miniMessage()
                        .deserialize(" <dark_gray>[<red>-</red>]</dark_gray>")
                        .clickEvent(ClickEvent
                                .runCommand(
                                        "/upa actorprofile partners remove " + actor.getName() + " " + partner.getName()));

                Messenger.send(sender, partnerComponent.append(removePartnerComponent));
            }
        }

        Component partnerAddComponent = MiniMessage.miniMessage().deserialize("<dark_gray>[<green>+</green>]</dark_gray>")
                .clickEvent(ClickEvent.suggestCommand("/upa actorprofile partners add " + actor.getName() + " "));
        Messenger.send(sender, partnerAddComponent);

        // --------------
        // Optional UnitedDungeons integration
        // --------------

        if (plugin.isUnitedDungeonsEnabled()) {
            var utils = new DungeonsActorComponentUtils();
            utils.sendHostileDungeonsComponent(sender, messageProvider, profile, actor);
            utils.sendFriendlyDungeonsComponent(sender, messageProvider, profile, actor);
        }
    }

}
