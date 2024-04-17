package eu.zzagro.simpletrade.listeners;

import eu.zzagro.simpletrade.SimpleTrade;
import eu.zzagro.simpletrade.commands.TradeCmd;
import eu.zzagro.simpletrade.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
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

        if (!isPlayerReady.containsKey(player))
        {
            isPlayerReady.put(player, false);
            isPlayerReady.put(target, false);
        }

        ItemStack confirmItem = plugin.metaManager.confirmItem;
        ItemStack readyItem = plugin.metaManager.readyItem;
        ItemStack waitingItem = plugin.metaManager.waitingItem;
        ItemStack cancelItem = plugin.metaManager.cancelTradeItem;

        if (e.getSlot() == 49 && e.getCurrentItem().isSimilar(cancelItem))
        {
            closeInv(player, target);
            return;
        }

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("tradeInventory.confirmItem");
        int playerConfirmSlot = plugin.tradeInv.getIndex(section);
        int targetConfirmSlot = plugin.tradeInv.getIndexMirrored(section);

        if (e.getSlot() == playerConfirmSlot && e.getCurrentItem().isSimilar(confirmItem)) {
            isPlayerReady.put(player, true);
            target.getOpenInventory().setItem(targetConfirmSlot, readyItem);
            e.getView().setItem(playerConfirmSlot, readyItem);

            System.out.println(isPlayerReady.get(player) + " : " + player.getName() + ", " + isPlayerReady.get(target) + " : " + target.getName());
        } else if (e.getSlot() == playerConfirmSlot && e.getCurrentItem().isSimilar(readyItem)) {
            isPlayerReady.put(player, false);
            target.getOpenInventory().setItem(targetConfirmSlot, waitingItem);
            e.getView().setItem(playerConfirmSlot, confirmItem);

            System.out.println(isPlayerReady.get(player) + " : " + player.getName() + ", " + isPlayerReady.get(target) + " : " + target.getName());
        }
        if (isPlayerReady.get(target) && isPlayerReady.get(player)) {
            closeInv(player, target);
            return;
        }

        if (e.getClickedInventory() instanceof PlayerInventory) {

        }
    }

    private void closeInv(Player player, Player target)
    {
        player.getOpenInventory().close();
        target.getOpenInventory().close();
        plugin.openTrades.remove(player, target);
        plugin.openTrades.remove(target, player);
    }
}
