package com.zayatv.simpletrade.listeners;

import com.zayatv.simpletrade.SimpleTrade;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class PlayerPickupItemListener implements Listener {

    private final SimpleTrade plugin;

    public PlayerPickupItemListener(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e)
    {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();

        if (!(plugin.openTrades.containsKey(player)) && !(plugin.openTrades.containsValue(player))) return;

        e.setCancelled(true);
    }
}
