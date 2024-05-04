package com.zayatv.simpletrade.listeners;

import com.zayatv.simpletrade.SimpleTrade;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {

    private final SimpleTrade plugin;

    public InventoryCloseListener(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e)
    {
        Player player = (Player) e.getPlayer();

        if (!e.getView().getTitle().equalsIgnoreCase("Trade Menu") && !plugin.openTrades.containsKey(player)) return;
        if (plugin.tradeInv.inEconomyMenu.contains(player)) return;

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
