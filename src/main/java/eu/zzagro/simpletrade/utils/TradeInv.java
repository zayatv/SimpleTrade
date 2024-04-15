package eu.zzagro.simpletrade.utils;

import eu.zzagro.simpletrade.SimpleTrade;
import eu.zzagro.simpletrade.commands.TradeCmd;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TradeInv {

    private final SimpleTrade plugin;

    public TradeInv(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    public void openPlayerInv(Player player) {
        player.sendMessage("Open Player Inv");
        //Inventory inv = Bukkit.createInventory(player, 54, "Trade " + TradeCmd.playerNameMap.get(target).getName());
        Inventory inv = Bukkit.createInventory(player, 54, "Trade Menu");

        ItemStack emptyItem = plugin.metaManager.emptyItem;
        ItemMeta emptyMeta = plugin.metaManager.getEmptyMeta();
        int[] emptySlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 13, 22, 31, 40, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        for (int emptySlot : emptySlots) {
            inv.setItem(emptySlot, emptyItem);
        }

        ItemStack confirmItem = plugin.metaManager.confirmItem;
        ItemMeta confirmMeta = plugin.metaManager.getConfirmMeta();
        inv.setItem(39, confirmItem);

        ItemStack waitingItem = plugin.metaManager.waitingItem;
        ItemMeta waitingMeta = plugin.metaManager.getWaitingMeta();
        inv.setItem(41, waitingItem);

        ItemStack cancelTradeItem = plugin.metaManager.cancelTradeItem;
        ItemMeta cancelTradeMeta = plugin.metaManager.getCancelTradeMeta();
        inv.setItem(49, cancelTradeItem);

        player.openInventory(inv);
    }

    public void openTargetInv(Player target) {
        target.sendMessage("Open Target Inv");
        //Inventory inv = Bukkit.createInventory(target, 54, "Trade " + TradeCmd.targetNameMap.get(player).getName());
        Inventory inv = Bukkit.createInventory(target, 54, "Trade Menu");

        ItemStack emptyItem = plugin.metaManager.emptyItem;
        ItemMeta emptyMeta = plugin.metaManager.getEmptyMeta();
        int[] emptySlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 13, 22, 31, 40, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        for (int emptySlot : emptySlots) {
            inv.setItem(emptySlot, emptyItem);
        }

        ItemStack confirmItem = plugin.metaManager.confirmItem;
        ItemMeta confirmMeta = plugin.metaManager.getConfirmMeta();
        inv.setItem(39, confirmItem);

        ItemStack waitingItem = plugin.metaManager.waitingItem;
        ItemMeta waitingMeta = plugin.metaManager.getWaitingMeta();
        inv.setItem(41, waitingItem);

        ItemStack cancelTradeItem = plugin.metaManager.cancelTradeItem;
        ItemMeta cancelTradeMeta = plugin.metaManager.getCancelTradeMeta();
        inv.setItem(49, cancelTradeItem);

        target.openInventory(inv);
    }
}
