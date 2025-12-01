package org.unitedlands.politics.integrations.UnitedTrade.listeners;

import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.classes.MessageProvider;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.trade.classes.events.TradeOrderBookPreTakeEvent;
import org.unitedlands.trade.classes.events.TradeOrderCompletedEvent;
import org.unitedlands.trade.classes.events.TradeOrderFailedEvent;
import org.unitedlands.utils.Logger;
import org.unitedlands.utils.Messenger;

public class TradeEventListeners implements Listener {

    private final UnitedPolitics plugin;
    private final MessageProvider messageProvider;

    public TradeEventListeners(UnitedPolitics plugin, MessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    @EventHandler
    public void onBookPrePickup(TradeOrderBookPreTakeEvent event) {

        var tradePoint = event.getTradePoint();
        if (tradePoint == null)
            return;
        
        Logger.log(tradePoint.getName());

        var location = tradePoint.getLocation();
        if (location == null)
            return;

        Logger.log(location.toString());

        ITownWrapper tradeTown = plugin.getGeopolWrapper().getTownAtLocation(location);
        if (tradeTown == null)
            return;

        Logger.log(tradeTown.getName());

        var player = event.getPlayer();
        ITownWrapper playerTown = plugin.getGeopolWrapper().getTownByPlayer(player);
        if (player == null)
            return;

        Logger.log(playerTown.getName());

        if (plugin.getConfig().getBoolean("integration-mechanics.UnitedTrade.use-minimum-town-reputation")) {

            double minimum = plugin.getConfig().getDouble("integration-mechanics.UnitedTrade.minimum-town-reputation",
                    0);
            var score = plugin.getReputationManager().getTotalReputationScore(tradeTown.getUUID(),
                    playerTown.getUUID());

            if (score < minimum) {
                Messenger.sendMessage(player,
                        messageProvider.get("integration-mechanics.UnitedTrade.minimum-fail-message"),
                        Map.of("name", tradeTown.getName(), "minimum", minimum + ""),
                        messageProvider.get("messages.prefix"));
                event.setCancelled(true);
                return;
            }

        }
        if (plugin.getConfig().getBoolean("integration-mechanics.UnitedTrade.use-minimum-nation-reputation")) {

            var nation = tradeTown.getNation();
            if (nation == null)
                return;

            double minimum = plugin.getConfig().getDouble("integration-mechanics.UnitedTrade.minimum-nation-reputation",
                    0);
            var score = plugin.getReputationManager().getTotalReputationScore(nation.getUUID(),
                    playerTown.getUUID());

            if (score < minimum) {
                Messenger.sendMessage(player,
                        messageProvider.get("integration-mechanics.UnitedTrade.minimum-fail-message"),
                        Map.of("name", nation.getName(), "minimum", minimum + ""),
                        messageProvider.get("messages.prefix"));
                event.setCancelled(true);
                return;
            }

        }

    }

    @EventHandler
    public void onTradeComplete(TradeOrderCompletedEvent event) {
        var tradePoint = event.getTradePoint();
        if (tradePoint == null)
            return;

        var location = tradePoint.getLocation();
        if (location == null)
            return;

        ITownWrapper tradeTown = plugin.getGeopolWrapper().getTownAtLocation(location);
        if (tradeTown == null)
            return;

        var player = event.getPlayer();

        ITownWrapper playerTown = plugin.getGeopolWrapper().getTownByPlayer(player);
        if (player == null)
            return;

        if (tradeTown.getUUID().equals(playerTown.getUUID()))
            return;

        var amount = plugin.getConfig().getDouble("modifiers.ut-trade-complete.amount");
        plugin.getReputationManager().handleReputationChange(tradeTown, playerTown, amount, "ut-trade-complete",
                event.getPlayer());

    }

    @EventHandler
    public void onTradeFailed(TradeOrderFailedEvent event) {
        var tradePoint = event.getTradePoint();
        if (tradePoint == null)
            return;

        var location = tradePoint.getLocation();
        if (location == null)
            return;

        ITownWrapper tradeTown = plugin.getGeopolWrapper().getTownAtLocation(location);
        if (tradeTown == null)
            return;

        var player = event.getPlayer();

        ITownWrapper playerTown = plugin.getGeopolWrapper().getTownByPlayer(player);
        if (player == null)
            return;

        if (tradeTown.getUUID().equals(playerTown.getUUID()))
            return;

        var amount = plugin.getConfig().getDouble("modifiers.ut-trade-failed.amount");
        plugin.getReputationManager().handleReputationChange(tradeTown, playerTown, amount, "ut-trade-failed",
                event.getPlayer());

    }

}
