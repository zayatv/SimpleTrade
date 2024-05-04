package com.zayatv.simpletrade.listeners;

import com.zayatv.simpletrade.SimpleTrade;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public class InventoryOpenListener implements Listener {

    private final SimpleTrade plugin;

    public InventoryOpenListener(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e)
    {
        Player player = (Player) e.getPlayer();

        if (!e.getView().getTitle().equalsIgnoreCase("Trade Menu")) return;
        Player target = plugin.openTrades.get(player);
        if (target == null)
            return;

        if (plugin.tradeInv.inEconomyMenu.contains(player))
        {
            plugin.tradeInv.inEconomyMenu.remove(player);

            Inventory tradeInvPlayer = e.getView().getTopInventory();
            Inventory tradeInvTarget = target.getOpenInventory().getTopInventory();

            int[] placeableSlotsPlayer = plugin.tradeInv.getEmptySlotsPlayer();
            int[] placeableSlotsTarget = plugin.tradeInv.getEmptySlotsTarget();

            plugin.tradeInv.updateTradeInvItems(player, target, tradeInvPlayer, tradeInvTarget, placeableSlotsPlayer, placeableSlotsTarget);
            plugin.tradeInv.setTradeStatusItem(player, target, tradeInvPlayer, tradeInvTarget);
            return;
        }

        plugin.tradeInv.isPlayerReady.put(player, false);
        plugin.tradeInv.isPlayerReady.put(target, false);

        plugin.tradeInv.playerItems.put(player, new ArrayList<>());
        plugin.tradeInv.playerItems.put(target, new ArrayList<>());
    }
}
