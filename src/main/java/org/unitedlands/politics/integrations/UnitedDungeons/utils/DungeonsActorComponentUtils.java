package org.unitedlands.politics.integrations.UnitedDungeons.utils;

import java.util.Map;

import org.bukkit.command.CommandSender;
import org.unitedlands.dungeons.UnitedDungeons;
import org.unitedlands.interfaces.IMessageProvider;
import org.unitedlands.politics.models.ActorProfile;
import org.unitedlands.politics.wrappers.interfaces.IGeopolObjectWrapper;
import org.unitedlands.utils.Messenger;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class DungeonsActorComponentUtils {


    public void sendHostileDungeonsComponent(CommandSender sender, IMessageProvider messageProvider, ActorProfile profile, IGeopolObjectWrapper actor)
    {
        Messenger.sendMessage(sender, messageProvider.get("messages.actorprofile.hostiledungeons-header"));

        if (profile.getHostileDungeons() != null) {
            for (var dungeonId : profile.getHostileDungeons()) {
                var dungeon = UnitedDungeons.getInstance().getDungeonManager().getDungeon(dungeonId);
                if (dungeon == null)
                    continue;

                Component dungeonComponent = Messenger.getMessage(
                        messageProvider.get("messages.actorprofile.hostiledungeons-line"),
                        Map.of("dungeon", dungeon.getName()), null);

                var dungeonRemoveComponent = MiniMessage.miniMessage()
                        .deserialize(" <dark_gray>[<red>-</red>]</dark_gray>")
                        .clickEvent(ClickEvent
                                .runCommand(
                                        "/upa actorprofile hostiledungeon remove " + actor.getName() + " " + dungeon.getName()));

                Messenger.send(sender, dungeonComponent.append(dungeonRemoveComponent));
            }
        }

        Component hostileDungeonAddComponent = MiniMessage.miniMessage().deserialize("<dark_gray>[<green>+</green>]</dark_gray>")
                .clickEvent(ClickEvent.suggestCommand("/upa actorprofile hostiledungeon add " + actor.getName() + " "));
        Messenger.send(sender, hostileDungeonAddComponent);

    }

        public void sendFriendlyDungeonsComponent(CommandSender sender, IMessageProvider messageProvider, ActorProfile profile, IGeopolObjectWrapper actor)
    {
        Messenger.sendMessage(sender, messageProvider.get("messages.actorprofile.friendlydungeons-header"));

        if (profile.getFriendlyDungeons() != null) {
            for (var dungeonId : profile.getFriendlyDungeons()) {
                var dungeon = UnitedDungeons.getInstance().getDungeonManager().getDungeon(dungeonId);
                if (dungeon == null)
                    continue;

                Component dungeonComponent = Messenger.getMessage(
                        messageProvider.get("messages.actorprofile.friendlydungeons-line"),
                        Map.of("dungeon", dungeon.getName()), null);

                var dungeonRemoveComponent = MiniMessage.miniMessage()
                        .deserialize(" <dark_gray>[<red>-</red>]</dark_gray>")
                        .clickEvent(ClickEvent
                                .runCommand(
                                        "/upa actorprofile friendlydungeon remove " + actor.getName() + " " + dungeon.getName()));

                Messenger.send(sender, dungeonComponent.append(dungeonRemoveComponent));
            }
        }

        Component hostileDungeonAddComponent = MiniMessage.miniMessage().deserialize("<dark_gray>[<green>+</green>]</dark_gray>")
                .clickEvent(ClickEvent.suggestCommand("/upa actorprofile friendlydungeon add " + actor.getName() + " "));
        Messenger.send(sender, hostileDungeonAddComponent);

    }
}
