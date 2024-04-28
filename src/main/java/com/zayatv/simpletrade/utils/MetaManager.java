package com.zayatv.simpletrade.utils;

import com.zayatv.simpletrade.SimpleTrade;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MetaManager {

    private final SimpleTrade plugin;

    private ItemStack unplaceableItem;
    private ItemStack cancelTradeItem;
    private ItemStack confirmItem;
    private ItemStack waitingItem;
    private ItemStack readyItem;
    private ItemStack emptyItem = new ItemStack(Material.AIR);
    private ItemStack econTradeItem;
    private ItemStack econItem;

    public MetaManager(SimpleTrade plugin) {
        this.plugin = plugin;
        unplaceableItem = new ItemStack(getMaterial("unplaceableItem"), 1);
        cancelTradeItem = new ItemStack(getMaterial("cancelTradeItem"), 1);
        confirmItem = new ItemStack(getMaterial("tradeStatusItem.confirmTradeItem"), 1);
        waitingItem = new ItemStack(getMaterial("tradeStatusItem.waitingItem"), 1);
        readyItem = new ItemStack(getMaterial("tradeStatusItem.readyItem"), 1);
        econTradeItem = new ItemStack(getMaterial("econTradeItem"), 1);
        econItem = new ItemStack(getMaterial("econTradeItem.econTrade"), 1);
    }

    public ItemStack getUnplaceableItem()
    {
        unplaceableItem.setItemMeta(getUnplaceableMeta());
        return unplaceableItem;
    }

    public ItemStack getCancelTradeItem()
    {
        cancelTradeItem.setItemMeta(getCancelTradeMeta());
        return cancelTradeItem;
    }

    public ItemStack getConfirmItem()
    {
        confirmItem.setItemMeta(getConfirmMeta());
        return confirmItem;
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

    public ItemStack getEconTradeItem()
    {
        econTradeItem.setItemMeta(getEconTradeMeta());
        return econTradeItem;
    }

    public ItemStack getEconItem()
    {
        econItem.setItemMeta(getEconMeta());
        return econItem;
    }

    private ItemMeta unplaceableMeta;
    private ItemMeta cancelTradeMeta;
    private ItemMeta confirmMeta;
    private ItemMeta waitingMeta;
    private ItemMeta readyMeta;
    private ItemMeta econTradeMeta;
    private ItemMeta econMeta;

    public ItemMeta getUnplaceableMeta() {
        if (unplaceableMeta == null) {
            unplaceableMeta = unplaceableItem.getItemMeta();
            unplaceableMeta.setDisplayName(plugin.color(getItemName("unplaceableItem")));
        }
        return unplaceableMeta;
    }

    public ItemMeta getCancelTradeMeta() {
        if (cancelTradeMeta == null) {
            cancelTradeMeta = cancelTradeItem.getItemMeta();
            cancelTradeMeta.setDisplayName(plugin.color(getItemName("cancelTradeItem")));
        }
        return cancelTradeMeta;
    }

    public ItemMeta getConfirmMeta() {
        if (confirmMeta == null) {
            confirmMeta = confirmItem.getItemMeta();
            confirmMeta.setDisplayName(plugin.color(getItemName("tradeStatusItem.confirmTradeItem")));
        }
        return confirmMeta;
    }

    public ItemMeta getWaitingMeta() {
        if (waitingMeta == null) {
            waitingMeta = waitingItem.getItemMeta();
            waitingMeta.setDisplayName(plugin.color(getItemName("tradeStatusItem.waitingItem")));
        }
        return waitingMeta;
    }

    public ItemMeta getReadyMeta() {
        if (readyMeta == null) {
            readyMeta = readyItem.getItemMeta();
            readyMeta.setDisplayName(plugin.color(getItemName("tradeStatusItem.readyItem")));
        }
        return readyMeta;
    }

    public ItemMeta getEconTradeMeta()
    {
        if (econTradeMeta == null)
        {
            econTradeMeta = econTradeItem.getItemMeta();
            econTradeMeta.setDisplayName(plugin.color(getItemName("econTradeItem")));
        }
        return econTradeMeta;
    }

    public ItemMeta getEconMeta()
    {
        if (econMeta == null)
        {
            econMeta = econItem.getItemMeta();
            econMeta.setDisplayName(plugin.color(getItemName("econTradeItem.econTrade")));
        }
        return econMeta;
    }

    private Material getMaterial(String itemName)
    {
        return Material.matchMaterial(plugin.getConfig().getString("tradeInventory.items." + itemName + ".material"));
    }

    private String getItemName(String itemName)
    {
        return plugin.getConfig().getString("tradeInventory.items." + itemName + ".displayName");
    }
}
