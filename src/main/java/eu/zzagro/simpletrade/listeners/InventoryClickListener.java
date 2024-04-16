package eu.zzagro.simpletrade.listeners;

import eu.zzagro.simpletrade.SimpleTrade;
import eu.zzagro.simpletrade.commands.TradeCmd;
import eu.zzagro.simpletrade.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class InventoryClickListener implements Listener {

    private final SimpleTrade plugin;

    private Map<Player, Boolean> isPlayerReady = new HashMap<>();
    private Map<Player, Boolean> isTargetReady = new HashMap<>();

    public InventoryClickListener(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        if (e.getClickedInventory() == null) return;
        if (e.getCurrentItem() == null) return;
        if (!e.getView().getTitle().equalsIgnoreCase("Trade Menu")) return;

        e.setCancelled(true);

        Player target = plugin.openTrades.get(player);

        if (target == null) {
            player.sendMessage("null");
            return;
        }

        isPlayerReady.put(player, false);

        ItemStack confirmItem = plugin.metaManager.confirmItem;
        ItemStack readyItem = plugin.metaManager.readyItem;
        ItemStack waitingItem = plugin.metaManager.waitingItem;

        if (e.getSlot() == 39 && e.getCurrentItem().isSimilar(confirmItem)) {
            isPlayerReady.put(player, true);
            target.getInventory().setItem(41, readyItem);
            player.getInventory().setItem(39, readyItem);
        } else if (e.getSlot() == 39 && e.getCurrentItem().isSimilar(readyItem)) {
            isPlayerReady.put(player, false);
            target.getInventory().setItem(41, waitingItem);
            player.getInventory().setItem(39, confirmItem);
        }
        if (isTargetReady.get(target) && isPlayerReady.get(player)) {
            player.getOpenInventory().close();
            target.getOpenInventory().close();
            plugin.openTrades.remove(player, target);
            plugin.openTrades.remove(target, player);
        }

        if (e.getClickedInventory() instanceof PlayerInventory) {

        }
    }
}
