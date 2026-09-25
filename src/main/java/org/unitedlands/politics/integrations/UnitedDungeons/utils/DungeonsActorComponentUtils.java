package org.unitedlands.politics.integrations.UnitedDungeons.utils;

import org.bukkit.command.CommandSender;
import org.unitedlands.dungeons.UnitedDungeons;
import org.unitedlands.politics.models.ActorProfile;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.utils.United;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class DungeonsActorComponentUtils {

    public void sendHostileDungeonsComponent(CommandSender sender, ActorProfile profile, IGeopolObjectWrapper actor) {
        United.messenger().send(sender, "actorprofile.hostiledungeons-header");

        if (profile.getHostileDungeons() != null) {
            for (var dungeonId : profile.getHostileDungeons()) {
                var dungeon = UnitedDungeons.getInstance().getDungeonManager().getDungeon(dungeonId);
                if (dungeon == null)
                    continue;

                Component dungeonComponent = MiniMessage.miniMessage().deserialize(
                        United.messenger().get("actorprofile.hostiledungeons-line", dungeon.getName()));

                var dungeonRemoveComponent = MiniMessage.miniMessage()
                        .deserialize(" <dark_gray>[<red>-</red>]</dark_gray>")
                        .clickEvent(ClickEvent
                                .runCommand(
                                        "/upa actorprofile hostiledungeon remove " + actor.getName() + " "
                                                + dungeon.getName()));

                United.messenger().send(sender, dungeonComponent.append(dungeonRemoveComponent));
            }
        }

        Component hostileDungeonAddComponent = MiniMessage.miniMessage()
                .deserialize("<dark_gray>[<green>+</green>]</dark_gray>")
                .clickEvent(ClickEvent.suggestCommand("/upa actorprofile hostiledungeon add " + actor.getName() + " "));
        United.messenger().send(sender, hostileDungeonAddComponent);

    }

    public void sendFriendlyDungeonsComponent(CommandSender sender, ActorProfile profile, IGeopolObjectWrapper actor) {

        United.messenger().send(sender, "actorprofile.friendlydungeons-header");

        if (profile.getFriendlyDungeons() != null) {
            for (var dungeonId : profile.getFriendlyDungeons()) {
                var dungeon = UnitedDungeons.getInstance().getDungeonManager().getDungeon(dungeonId);
                if (dungeon == null)
                    continue;

                Component dungeonComponent = MiniMessage.miniMessage().deserialize(
                        United.messenger().get("actorprofile.friendlydungeons-line", dungeon.getName()));

                var dungeonRemoveComponent = MiniMessage.miniMessage()
                        .deserialize(" <dark_gray>[<red>-</red>]</dark_gray>")
                        .clickEvent(ClickEvent
                                .runCommand(
                                        "/upa actorprofile friendlydungeon remove " + actor.getName() + " "
                                                + dungeon.getName()));

                United.messenger().send(sender, dungeonComponent.append(dungeonRemoveComponent));
            }
        }

        Component hostileDungeonAddComponent = MiniMessage.miniMessage()
                .deserialize("<dark_gray>[<green>+</green>]</dark_gray>")
                .clickEvent(
                        ClickEvent.suggestCommand("/upa actorprofile friendlydungeon add " + actor.getName() + " "));
        United.messenger().send(sender, hostileDungeonAddComponent);

    }
}
