package org.unitedlands.politics.integrations.UnitedTrade.listeners;

import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.classes.MessageProvider;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.trade.classes.events.ShopOpenEvent;
import org.unitedlands.trade.classes.events.TradeOrderBookPreTakeEvent;
import org.unitedlands.trade.classes.events.TradeOrderCompletedEvent;
import org.unitedlands.trade.classes.events.TradeOrderFailedEvent;
import org.unitedlands.utils.Messenger;

public class TradeEventListeners implements Listener {

    private final UnitedPolitics plugin;
    private final MessageProvider messageProvider;

    public TradeEventListeners(UnitedPolitics plugin, MessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
    }

    @EventHandler
    public void onShopOpen(ShopOpenEvent event) {

        var player = event.getPlayer();
        var shopPoint = event.getShopPoint();

        var playerTown = plugin.getGeopolWrapper().getTownByPlayer(player);
        if (player == null)
            return;

        var location = shopPoint.getLocation();
        if (location == null)
            return;

        ITownWrapper tradeTown = plugin.getGeopolWrapper().getTownAtLocation(location);
        if (tradeTown == null)
            return;

        var score = plugin.getReputationManager().getTotalReputationScore(tradeTown.getUUID(), playerTown.getUUID());
        var minimum = shopPoint.getMinReputation();

        if (score < minimum) {
            event.setCancelled(true);
            Messenger.sendMessage(player,
                    messageProvider.get("integration-mechanics.UnitedTrade.minimum-fail-message"),
                    Map.of("name", tradeTown.getName(), "minimum", minimum + ""),
                    messageProvider.get("messages.prefix"));
        }
    }

    @EventHandler
    public void onBookPrePickup(TradeOrderBookPreTakeEvent event) {

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

        if (plugin.getConfig().getBoolean("integration-mechanics.UnitedTrade.use-minimum-town-reputation")) {

            double minimum = tradePoint.getMinReputation();
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

            double minimum = tradePoint.getMinReputation();
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

        var config = plugin.getConfig();

        // Bonus payment handling

        if (config.getBoolean("settings.ut-trade-complete.reputation-bonus.enabled", false)) {

            var minimum = config.getDouble("settings.ut-trade-complete.reputation-bonus.minimum", 50);
            var limit = config.getDouble("settings.ut-trade-complete.reputation-bonus.limit", 100);
            var factor = config.getDouble("settings.ut-trade-complete.reputation-bonus.factor", 0.5);
            var reason = config.getString("settings.ut-trade-complete.reputation-bonus.reason", "");

            var payment = event.getPayment();
            var score = plugin.getReputationManager().getTotalReputationScore(tradeTown.getUUID(),
                    playerTown.getUUID());

            if (score >= minimum) {
                var percentage = Math.max(0, Math.min(1, (score - minimum) / (limit - minimum)));
                if (percentage > 0) {
                    var bonus = payment * factor * percentage;
                    event.setBonus(bonus);
                    event.setBonusReason(reason);
                }
            }
        }

        // Reputation handling

        var amount = tradePoint.getReputationOnComplete();
        if (amount == 0)
            return;

        plugin.getReputationManager().handleReputationChange(tradeTown, playerTown, amount, "ut-trade-complete",
                event.getPlayer(), true);

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

        var amount = tradePoint.getReputationOnFail();
        if (amount == 0)
            return;

        plugin.getReputationManager().handleReputationChange(tradeTown, playerTown, amount, "ut-trade-failed",
                event.getPlayer(), true);

    }

}
