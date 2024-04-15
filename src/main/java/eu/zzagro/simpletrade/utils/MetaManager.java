package eu.zzagro.simpletrade.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MetaManager {

    public ItemStack emptyItem = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
    public ItemStack confirmItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
    public ItemStack cancelTradeItem = new ItemStack(Material.BARRIER);
    public ItemStack waitingItem = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1);
    public ItemStack readyItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);

    private ItemMeta emptyMeta;
    private ItemMeta confirmMeta;
    private ItemMeta cancelTradeMeta;
    private ItemMeta waitingMeta;
    private ItemMeta readyMeta;

    public ItemMeta getEmptyMeta() {
        if (emptyMeta == null) {
            emptyMeta = emptyItem.getItemMeta();
            emptyMeta.setDisplayName("");
            emptyItem.setItemMeta(emptyMeta);
        }
        return emptyMeta;
    }

    public ItemMeta getConfirmMeta() {
        if (confirmMeta == null) {
            confirmMeta = confirmItem.getItemMeta();
            confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm trade");
            confirmItem.setItemMeta(confirmMeta);
        }
        return confirmMeta;
    }

    public ItemMeta getCancelTradeMeta() {
        if (cancelTradeMeta == null) {
            cancelTradeMeta = cancelTradeItem.getItemMeta();
            cancelTradeMeta.setDisplayName(ChatColor.RED + "Cancel trade");
            cancelTradeItem.setItemMeta(cancelTradeMeta);
        }
        return cancelTradeMeta;
    }

    public ItemMeta getWaitingMeta() {
        if (waitingMeta == null) {
            waitingMeta = waitingItem.getItemMeta();
            waitingMeta.setDisplayName(ChatColor.RED + "Waiting for other players confirmation!");
            waitingItem.setItemMeta(waitingMeta);
        }
        return waitingMeta;
    }

    public ItemMeta getReadyMeta() {
        if (readyMeta == null) {
            readyMeta = readyItem.getItemMeta();
            readyMeta.setDisplayName(ChatColor.GREEN + "Confirmed");
            readyItem.setItemMeta(readyMeta);
        }
        return readyMeta;
    }
}
