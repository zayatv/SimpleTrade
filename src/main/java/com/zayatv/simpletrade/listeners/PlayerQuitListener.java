package com.zayatv.simpletrade.listeners;

import com.zayatv.simpletrade.SimpleTrade;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final SimpleTrade plugin;

    public PlayerQuitListener(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        Player player = e.getPlayer();

        if (!plugin.openTrades.containsKey(player)) return;

        Player target = plugin.openTrades.get(player);

        if (target == null) return;

        plugin.tradeInv.returnItems(player, target);

        plugin.openTrades.remove(player, target);
        plugin.openTrades.remove(target, player);
        plugin.tradeInv.isPlayerReady.remove(player);
        plugin.tradeInv.isPlayerReady.remove(target);
        plugin.tradeInv.playerItems.remove(player);
        plugin.tradeInv.playerItems.remove(target);
        plugin.tradeInv.inEconomyMenu.remove(target);
        if (target.getOpenInventory().getTitle().equalsIgnoreCase("Trade Menu")) target.getOpenInventory().close();
    }
}
