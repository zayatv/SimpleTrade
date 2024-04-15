package eu.zzagro.simpletrade.listeners;

import eu.zzagro.simpletrade.SimpleTrade;
import eu.zzagro.simpletrade.commands.TradeCmd;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent e) {
        Player player = e.getPlayer();
        Player target = (Player) e.getRightClicked();

        if (player.isSneaking()) {
            if (player.hasPermission("simpletrade.trade") && target.hasPermission("simpletrade.trade")) {
                player.performCommand("trade " + target.getName());
            }
        }
    }
}
