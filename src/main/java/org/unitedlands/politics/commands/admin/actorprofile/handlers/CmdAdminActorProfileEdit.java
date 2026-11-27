package org.unitedlands.politics.commands.admin.actorprofile.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.unitedlands.annotations.UnitedSubCommand;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.commands.admin.actorprofile.CmdAdminActorProfile;
import org.unitedlands.politics.integrations.UnitedDungeons.utils.DungeonsActorComponentUtils;
import org.unitedlands.politics.managers.ActorProfileManager;
import org.unitedlands.politics.utils.ColorFormatter;
import org.unitedlands.registrars.command.UnitedCommandExecutor;
import org.unitedlands.unitedlands.classes.GeopolObject;
import org.unitedlands.unitedlands.managers.UnitedLandsDataManager;
import org.unitedlands.utils.United;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

@UnitedSubCommand(
    parent = CmdAdminActorProfile.class, 
    name = "edit", 
    usage = "/upa actorprofile edit <actor>"
)
public class CmdAdminActorProfileEdit implements UnitedCommandExecutor {

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

        // Argument can either be a UUID or a name, try UUID first and then fall back to
        // name.
        GeopolObject actor = null;
        try {
            actor = UnitedLandsDataManager.instance().getGeopolObject(UUID.fromString(args[0]));
        } catch (Exception ignore) {
            actor = UnitedLandsDataManager.instance().getGeopolObject(args[0]);
        }
        if (actor == null) {
            United.messenger().send(sender, "errors.general.geopol-obj-not-found", args[0]);
            return;
        }

        var profile = ActorProfileManager.instance().getActorProfile(actor.getUuid());
        if (profile == null) {
            United.messenger().send(sender, "errors.actorprofile.no-profile");
            return;
        }

        United.messenger().send(sender, "actorprofile.header", actor.getName());

        // --------------
        // Reactions
        // --------------

        United.messenger().send(sender, "actorprofile.reactions-header", actor.getName());

        if (profile.getEventReactions() != null) {
            for (var reaction : profile.getEventReactions()) {
                Component reactionComponent = MiniMessage.miniMessage().deserialize(
                        United.messenger().get("actorprofile.reactions-line",
                                reaction.getEventKey(),
                                reaction.getReactionReputationKey(),
                                ColorFormatter.getAmountColored(reaction.getAmount())));

                var reactionRemoveComponent = MiniMessage.miniMessage()
                        .deserialize(" <dark_gray>[<red>-</red>]</dark_gray>")
                        .clickEvent(ClickEvent.runCommand(
                                "/upa actorprofile reaction remove " + actor.getName() + " " + reaction.getEventKey()));

                United.messenger().send(sender, reactionComponent.append(reactionRemoveComponent));
            }
        }

        Component reactionAddComponent = MiniMessage.miniMessage()
                .deserialize("<dark_gray>[<green>+</green>]</dark_gray>")
                .clickEvent(ClickEvent.suggestCommand("/upa actorprofile reaction create " + actor.getName() + " "));
        United.messenger().send(sender, reactionAddComponent);

        // --------------
        // Rivals
        // --------------

        United.messenger().send(sender, "actorprofile.rivals-header");

        if (profile.getRivals() != null) {
            for (var rivalId : profile.getRivals()) {
                var rival = UnitedLandsDataManager.instance().getGeopolObject(rivalId);
                if (rival == null)
                    continue;

                Component rivalComponent = MiniMessage.miniMessage().deserialize(
                        United.messenger().get("actorprofile.rivals-line",
                                rival.getName()));

                var rivalRemoveComponent = MiniMessage.miniMessage()
                        .deserialize(" <dark_gray>[<red>-</red>]</dark_gray>")
                        .clickEvent(ClickEvent
                                .runCommand(
                                        "/upa actorprofile rivals remove " + actor.getName() + " " + rival.getName()));

                United.messenger().send(sender, rivalComponent.append(rivalRemoveComponent));
            }
        }

        Component rivalAddComponent = MiniMessage.miniMessage().deserialize("<dark_gray>[<green>+</green>]</dark_gray>")
                .clickEvent(ClickEvent.suggestCommand("/upa actorprofile rivals add " + actor.getName() + " "));
        United.messenger().send(sender, rivalAddComponent);

        // --------------
        // Partners
        // --------------

        United.messenger().send(sender, "actorprofile.partners-header");

        if (profile.getPartners() != null) {
            for (var partnerId : profile.getPartners()) {
                var partner = UnitedLandsDataManager.instance().getGeopolObject(partnerId);
                if (partner == null)
                    continue;

                Component partnerComponent = MiniMessage.miniMessage().deserialize(
                        United.messenger().get("actorprofile.partners-line",
                                partner.getName()));

                var removePartnerComponent = MiniMessage.miniMessage()
                        .deserialize(" <dark_gray>[<red>-</red>]</dark_gray>")
                        .clickEvent(ClickEvent
                                .runCommand(
                                        "/upa actorprofile partners remove " + actor.getName() + " "
                                                + partner.getName()));

                United.messenger().send(sender, partnerComponent.append(removePartnerComponent));
            }
        }

        Component partnerAddComponent = MiniMessage.miniMessage()
                .deserialize("<dark_gray>[<green>+</green>]</dark_gray>")
                .clickEvent(ClickEvent.suggestCommand("/upa actorprofile partners add " + actor.getName() + " "));
        United.messenger().send(sender, partnerAddComponent);

        // --------------
        // Optional UnitedDungeons integration
        // --------------

        if (UnitedPolitics.instance().isUnitedDungeonsEnabled()) {
            var utils = new DungeonsActorComponentUtils();
            utils.sendHostileDungeonsComponent(sender, profile, actor);
            utils.sendFriendlyDungeonsComponent(sender, profile, actor);
        }
    }

}
