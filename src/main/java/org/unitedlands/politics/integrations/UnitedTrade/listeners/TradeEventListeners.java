package org.unitedlands.politics.integrations.UnitedTrade.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.unitedlands.politics.UnitedPolitics;
import org.unitedlands.politics.managers.ReputationManager;
import org.unitedlands.politics.wrappers.interfaces.ITownWrapper;
import org.unitedlands.trade.classes.TradePoint;
import org.unitedlands.trade.classes.events.ShopOpenEvent;
import org.unitedlands.trade.classes.events.TradeOrderCompletedEvent;
import org.unitedlands.trade.classes.events.TradeOrderFailedEvent;
import org.unitedlands.trade.classes.events.TradePointValidationEvent;
import org.unitedlands.utils.United;

import net.kyori.adventure.text.minimessage.MiniMessage;

public class TradeEventListeners implements Listener {

    @EventHandler
    public void onShopOpen(ShopOpenEvent event) {

        var player = event.getPlayer();
        var shopPoint = event.getShopPoint();

        var playerTown = UnitedPolitics.instance().getGeopolWrapper().getTownByPlayer(player);
        if (player == null)
            return;

        var location = shopPoint.getLocation();
        if (location == null)
            return;

        ITownWrapper tradeTown = UnitedPolitics.instance().getGeopolWrapper().getTownAtLocation(location);
        if (tradeTown == null)
            return;

        var score = ReputationManager.instance().getTotalReputationScore(tradeTown.getUUID(), playerTown.getUUID());
        var minimum = shopPoint.getMinReputation();

        if (score < minimum) {
            event.setCancelled(true);
            United.messenger().send(player, "integration-mechanics.UnitedTrade.minimum-fail-message",
                    tradeTown.getName(), String.valueOf(minimum));
        }
    }

    @EventHandler
    public void onTradePointValidation(TradePointValidationEvent event) {

        var tradePoint = event.getTradePoint();
        if (tradePoint == null)
            return;

        var location = tradePoint.getLocation();
        if (location == null)
            return;

        validateReputation(event, event.getPlayer(), tradePoint, location);

    }

    private void validateReputation(TradePointValidationEvent event, Player player, TradePoint tradePoint,
            Location location) {

        ITownWrapper tradeTown = UnitedPolitics.instance().getGeopolWrapper().getTownAtLocation(location);
        if (tradeTown == null)
            return;

        ITownWrapper playerTown = UnitedPolitics.instance().getGeopolWrapper().getTownByPlayer(player);
        if (player == null)
            return;

        if (UnitedPolitics.instance().getConfig()
                .getBoolean("integration-mechanics.UnitedTrade.use-minimum-town-reputation")) {

            double minimum = tradePoint.getMinReputation();
            double defaultReputation = UnitedPolitics.instance().getConfig()
                    .getDouble("integration-mechanics.UnitedTrade.default-reputation", 0.0);

            double score = defaultReputation;
            try {
                score = ReputationManager.instance().getTotalReputationScore(tradeTown.getUUID(),
                        playerTown.getUUID());
            } catch (Exception ignore) {
                United.logger().warning("Could not get town reputation for player " + player.getName()
                        + ", using default reputation " + score);
                // Use fallback default score
            }

            if (score < minimum) {
                event.setValid(false);
                event.getMessages().add(
                        MiniMessage.miniMessage().deserialize(
                                United.messenger().get("integration-mechanics.UnitedTrade.minimum-fail-message",
                                        tradeTown.getName(), String.valueOf(minimum))));
            }

        }
        if (UnitedPolitics.instance().getConfig()
                .getBoolean("integration-mechanics.UnitedTrade.use-minimum-nation-reputation")) {

            var nation = tradeTown.getNation();
            if (nation == null)
                return;

            double minimum = tradePoint.getMinReputation();
            double defaultReputation = UnitedPolitics.instance().getConfig()
                    .getDouble("integration-mechanics.UnitedTrade.default-reputation", 0.0);

            double score = defaultReputation;
            try {
                score = ReputationManager.instance().getTotalReputationScore(nation.getUUID(),
                        playerTown.getUUID());
            } catch (Exception ignore) {
                United.logger().warning("Could not get town reputation for player " + player.getName()
                        + ", using default reputation " + score);
                // Use fallback default score
            }

            if (score < minimum) {
                event.setValid(false);
                event.getMessages().add(
                        MiniMessage.miniMessage().deserialize(
                                United.messenger().get("integration-mechanics.UnitedTrade.minimum-fail-message",
                                        nation.getName(), String.valueOf(minimum))));
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

        ITownWrapper tradeTown = UnitedPolitics.instance().getGeopolWrapper().getTownAtLocation(location);
        if (tradeTown == null)
            return;

        var player = event.getPlayer();

        ITownWrapper playerTown = UnitedPolitics.instance().getGeopolWrapper().getTownByPlayer(player);
        if (player == null)
            return;

        if (tradeTown.getUUID().equals(playerTown.getUUID()))
            return;

        var config = UnitedPolitics.instance().getConfig();

        // Bonus payment handling

        if (config.getBoolean("settings.ut-trade-complete.reputation-bonus.enabled", false)) {

            var minimum = config.getDouble("settings.ut-trade-complete.reputation-bonus.minimum", 50);
            var limit = config.getDouble("settings.ut-trade-complete.reputation-bonus.limit", 100);
            var factor = config.getDouble("settings.ut-trade-complete.reputation-bonus.factor", 0.5);
            var reason = config.getString("settings.ut-trade-complete.reputation-bonus.reason", "");
            double defaultReputation = UnitedPolitics.instance().getConfig()
                    .getDouble("integration-mechanics.UnitedTrade.default-reputation", 0.0);

            var payment = event.getPayment();

            double score = defaultReputation;
            try {
                score = ReputationManager.instance().getTotalReputationScore(tradeTown.getUUID(),
                        playerTown.getUUID());

            } catch (Exception ignore) {
                United.logger().warning("Could not get town reputation for player " + player.getName()
                        + ", using default reputation " + score);
                // Use fallback default score
            }

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

        ReputationManager.instance().handleReputationChange(tradeTown, playerTown, amount, "ut-trade-complete",
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

        ITownWrapper tradeTown = UnitedPolitics.instance().getGeopolWrapper().getTownAtLocation(location);
        if (tradeTown == null)
            return;

        var player = event.getPlayer();

        ITownWrapper playerTown = UnitedPolitics.instance().getGeopolWrapper().getTownByPlayer(player);
        if (player == null)
            return;

        if (tradeTown.getUUID().equals(playerTown.getUUID()))
            return;

        var amount = tradePoint.getReputationOnFail();
        if (amount == 0)
            return;

        ReputationManager.instance().handleReputationChange(tradeTown, playerTown, amount, "ut-trade-failed",
                event.getPlayer(), true);

    }

}
