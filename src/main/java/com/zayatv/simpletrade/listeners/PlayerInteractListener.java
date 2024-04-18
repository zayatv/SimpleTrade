package com.zayatv.simpletrade.listeners;

import com.zayatv.simpletrade.SimpleTrade;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class PlayerInteractListener implements Listener {

    private final SimpleTrade plugin;

    public PlayerInteractListener(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent e) {
        if(!(e.getRightClicked() instanceof Player)) return;

        Player player = e.getPlayer();
        Player target = (Player) e.getRightClicked();

        if (player.isSneaking()) {
            if (player.hasPermission("simpletrade.trade") && target.hasPermission("simpletrade.trade")) {
                player.performCommand("trade " + target.getName());
            }
        }
    }
}
