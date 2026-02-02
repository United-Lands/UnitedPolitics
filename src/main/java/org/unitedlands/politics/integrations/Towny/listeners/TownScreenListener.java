package org.unitedlands.politics.integrations.Towny.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.models.ReputationScoreEntry;
import org.unitedlands.politics.utils.ColorFormatter;
import org.unitedlands.politics.wrappers.Towny.TownyNationWrapper;
import org.unitedlands.politics.wrappers.Towny.TownyTownWrapper;
import org.unitedlands.utils.Logger;

import com.palmergames.bukkit.towny.event.statusscreen.NationStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;

import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TownScreenListener implements Listener {

    private final UnitedPolitics plugin;

    public TownScreenListener(UnitedPolitics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTownStatusScreen(TownStatusScreenEvent event) {

        var screen = event.getStatusScreen();
        var config = plugin.getConfig();

        if (!config.getBoolean("settings.townscreen.enabled", false))
            return;

        var sender = event.getCommandSender();
        if (sender instanceof Player) {
            var player = (Player) sender;

            Logger.log(player.getName());

            var playerTown = plugin.getGeopolWrapper().getTownByPlayer(player);
            if (playerTown == null)
                return;

            var town = new TownyTownWrapper(event.getTown());

            if (playerTown.getUUID().equals(town.getUUID()))
                return;

            var townOpinion = plugin.getReputationManager().getTotalReputationScore(town.getUUID(),
                    playerTown.getUUID());
            var playerTownOpinion = plugin.getReputationManager().getTotalReputationScore(playerTown.getUUID(),
                    town.getUUID());

            var afterComponentName = config.getString("settings.townscreen.after-component");
            var componentToAppendAfter = screen.getComponentOrNull(afterComponentName);

            var contentObserver = config.getString("settings.townscreen.content-observer");
            contentObserver = contentObserver.replace("{score}", ColorFormatter.getAmountColored(townOpinion));

            var componentObserver = MiniMessage.miniMessage().deserialize(contentObserver);
            List<String> observerHoverString = new ArrayList<>();

            List<ReputationScoreEntry> allRecords = new ArrayList<>();
            allRecords.addAll(
                    plugin.getReputationManager().getReputationScoreEntries(town.getUUID(), playerTown.getUUID()));
            allRecords.addAll(plugin.getReputationManager().calculateStaticReputationScoreEntries(town.getUUID(),
                    playerTown.getUUID()));
            for (var record : allRecords) {
                observerHoverString.add("<gray>" + record.getDescription() + ":</gray> "
                        + ColorFormatter.getAmountColored(record.getModifier()) + " <gray>(" + ColorFormatter.getAmountColored(record.getDecayRate()) + "/d)</gray>");
            }
            componentObserver = componentObserver.hoverEvent(
                    HoverEvent.showText(MiniMessage.miniMessage().deserialize(String.join("\n", observerHoverString))));

            var contentSubject = config.getString("settings.townscreen.content-subject");
            contentSubject = contentSubject.replace("{score}", ColorFormatter.getAmountColored(playerTownOpinion));

            var componentSubject = MiniMessage.miniMessage().deserialize(contentSubject);
            List<String> subjectHoverString = new ArrayList<>();
            List<ReputationScoreEntry> subjectRecords = new ArrayList<>();
            subjectRecords.addAll(
                    plugin.getReputationManager().getReputationScoreEntries(playerTown.getUUID(), town.getUUID()));
            subjectRecords.addAll(plugin.getReputationManager()
                    .calculateStaticReputationScoreEntries(playerTown.getUUID(), town.getUUID()));
            for (var record : subjectRecords) {
                subjectHoverString.add("<gray>" + record.getDescription() + ":</gray> "
                        + ColorFormatter.getAmountColored(record.getModifier()) + " <gray>(" + ColorFormatter.getAmountColored(record.getDecayRate()) + "/d)</gray>");
            }
            componentSubject = componentSubject.hoverEvent(
                    HoverEvent.showText(MiniMessage.miniMessage().deserialize(String.join("\n", subjectHoverString))));

            var divider = MiniMessage.miniMessage().deserialize(" <grey>|</grey> ");
            componentToAppendAfter = componentToAppendAfter.appendNewline().append(componentObserver).append(divider)
                    .append(componentSubject);

            screen.replaceComponent(afterComponentName, componentToAppendAfter);
        }

    }

    @EventHandler
    public void onNationStatusScreen(NationStatusScreenEvent event) {

        var screen = event.getStatusScreen();
        var config = plugin.getConfig();

        if (!config.getBoolean("settings.nationscreen.enabled", false))
            return;

        var sender = event.getCommandSender();
        if (sender instanceof Player) {
            var player = (Player) sender;

            var playerTown = plugin.getGeopolWrapper().getTownByPlayer(player);
            if (playerTown == null)
                return;

            var nation = new TownyNationWrapper(event.getNation());

            var nationOpinion = plugin.getReputationManager().getTotalReputationScore(nation.getUUID(),
                    playerTown.getUUID());
            var playerTownOpinion = plugin.getReputationManager().getTotalReputationScore(playerTown.getUUID(),
                    nation.getUUID());

            var afterComponentName = config.getString("settings.nationscreen.after-component");
            var componentToAppendAfter = screen.getComponentOrNull(afterComponentName);

            var contentObserver = config.getString("settings.nationscreen.content-observer");
            contentObserver = contentObserver.replace("{score}", ColorFormatter.getAmountColored(nationOpinion));

            var componentObserver = MiniMessage.miniMessage().deserialize(contentObserver);
            List<String> observerHoverString = new ArrayList<>();

            List<ReputationScoreEntry> observerRecords = new ArrayList<>();
            observerRecords.addAll(
                    plugin.getReputationManager().getReputationScoreEntries(nation.getUUID(), playerTown.getUUID()));
            observerRecords.addAll(plugin.getReputationManager().calculateStaticReputationScoreEntries(nation.getUUID(),
                    playerTown.getUUID()));

            for (var record : observerRecords) {
                observerHoverString.add("<gray>" + record.getDescription() + ":</gray> "
                        + ColorFormatter.getAmountColored(record.getModifier())  + " <gray>(" + ColorFormatter.getAmountColored(record.getDecayRate()) + "/d)</gray>");
            }
            componentObserver = componentObserver.hoverEvent(
                    HoverEvent.showText(MiniMessage.miniMessage().deserialize(String.join("\n", observerHoverString))));

            var contentSubject = config.getString("settings.nationscreen.content-subject");
            contentSubject = contentSubject.replace("{score}", ColorFormatter.getAmountColored(playerTownOpinion));

            var componentSubject = MiniMessage.miniMessage().deserialize(contentSubject);
            List<String> subjectHoverString = new ArrayList<>();
            List<ReputationScoreEntry> subjectRecords = new ArrayList<>();
            subjectRecords.addAll(
                    plugin.getReputationManager().getReputationScoreEntries(playerTown.getUUID(), nation.getUUID()));
            subjectRecords.addAll(plugin.getReputationManager()
                    .calculateStaticReputationScoreEntries(playerTown.getUUID(), nation.getUUID()));
            for (var record : subjectRecords) {
                subjectHoverString.add("<gray>" + record.getDescription() + ":</gray> "
                        + ColorFormatter.getAmountColored(record.getModifier())  + " <gray>(" + ColorFormatter.getAmountColored(record.getDecayRate()) + "/d)</gray>");
            }
            componentSubject = componentSubject.hoverEvent(
                    HoverEvent.showText(MiniMessage.miniMessage().deserialize(String.join("\n", subjectHoverString))));

            var divider = MiniMessage.miniMessage().deserialize(" <grey>|</grey> ");
            componentToAppendAfter = componentToAppendAfter.appendNewline().append(componentObserver).append(divider)
                    .append(componentSubject);

            screen.replaceComponent(afterComponentName, componentToAppendAfter);

        }

    }
}
