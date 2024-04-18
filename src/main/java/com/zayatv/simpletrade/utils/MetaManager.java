package com.zayatv.simpletrade.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MetaManager {

    private ItemStack unplaceableItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
    private ItemStack confirmItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
    private ItemStack cancelTradeItem = new ItemStack(Material.BARRIER);
    private ItemStack waitingItem = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1);
    private ItemStack readyItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
    private ItemStack emptyItem = new ItemStack(Material.AIR);

    public ItemStack getUnplaceableItem()
    {
        unplaceableItem.setItemMeta(getUnplaceableMeta());
        return unplaceableItem;
    }

    public ItemStack getConfirmItem()
    {
        confirmItem.setItemMeta(getConfirmMeta());
        return confirmItem;
    }

    public ItemStack getCancelTradeItem()
    {
        cancelTradeItem.setItemMeta(getCancelTradeMeta());
        return cancelTradeItem;
    }

    public ItemStack getWaitingItem()
    {
        waitingItem.setItemMeta(getWaitingMeta());
        return waitingItem;
    }

    public ItemStack getReadyItem()
    {
        readyItem.setItemMeta(getReadyMeta());
        return readyItem;
    }

    public ItemStack getEmptyItem()
    {
        return emptyItem;
    }

    private ItemMeta unplaceableMeta;
    private ItemMeta confirmMeta;
    private ItemMeta cancelTradeMeta;
    private ItemMeta waitingMeta;
    private ItemMeta readyMeta;

    public ItemMeta getUnplaceableMeta() {
        if (unplaceableMeta == null) {
            unplaceableMeta = unplaceableItem.getItemMeta();
            unplaceableMeta.setDisplayName("");
        }
        return unplaceableMeta;
    }

    public ItemMeta getConfirmMeta() {
        if (confirmMeta == null) {
            confirmMeta = confirmItem.getItemMeta();
            confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm trade");
        }
        return confirmMeta;
    }

    public ItemMeta getCancelTradeMeta() {
        if (cancelTradeMeta == null) {
            cancelTradeMeta = cancelTradeItem.getItemMeta();
            cancelTradeMeta.setDisplayName(ChatColor.RED + "Cancel trade");
        }
        return cancelTradeMeta;
    }

    public ItemMeta getWaitingMeta() {
        if (waitingMeta == null) {
            waitingMeta = waitingItem.getItemMeta();
            waitingMeta.setDisplayName(ChatColor.RED + "Waiting for other players confirmation!");
        }
        return waitingMeta;
    }

    public ItemMeta getReadyMeta() {
        if (readyMeta == null) {
            readyMeta = readyItem.getItemMeta();
            readyMeta.setDisplayName(ChatColor.GREEN + "Confirmed");
        }
        return readyMeta;
    }
}
