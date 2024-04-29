package com.zayatv.simpletrade.utils;

import com.zayatv.simpletrade.SimpleTrade;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TradeInv {

    private final SimpleTrade plugin;

    public TradeInv(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    public Inventory openTradeInventory(Player player) {
        Inventory inv = Bukkit.createInventory(player, 54, "Trade Menu");

        ItemStack unplaceableItem = plugin.metaManager.getUnplaceableItem();
        for (int i = 0; i < inv.getSize(); i++)
        {
            inv.setItem(i, unplaceableItem);
        }

        ItemStack emptyItem = plugin.metaManager.getEmptyItem();
        int[] emptySlots = getEmptySlots();
        for (int emptySlot : emptySlots) {
            inv.setItem(emptySlot, emptyItem);
        }

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("tradeInventory.items.tradeStatusItem.position");

        ItemStack confirmItem = plugin.metaManager.getConfirmItem();
        inv.setItem(getIndex(section), confirmItem);

        ItemStack waitingItem = plugin.metaManager.getWaitingItem();
        inv.setItem(getIndexMirrored(section), waitingItem);

        ItemStack econTradeItem = plugin.metaManager.getEconTradeItem();
        inv.setItem(getIndex(plugin.getConfig().getConfigurationSection("tradeInventory.items.econTradeItem.position")), econTradeItem);

        ItemStack cancelTradeItem = plugin.metaManager.getCancelTradeItem();
        inv.setItem(49, cancelTradeItem);

        return inv;
    }

    private int[] getEmptySlots()
    {
        List<Integer> emptySlots = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("tradeInventory.placeableSlots");

        for (String path : section.getKeys(false))
        {
            ConfigurationSection pathSection = section.getConfigurationSection(path);

            int inventoryIndex = getIndex(pathSection);
            int inventoryIndexMirrored = getIndexMirrored(pathSection);

            emptySlots.add(inventoryIndex);
            emptySlots.add(inventoryIndexMirrored);
        }
        return emptySlots.stream().mapToInt(i -> i).toArray();
    }

    public int[] getEmptySlotsPlayer()
    {
        List<Integer> emptySlots = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("tradeInventory.placeableSlots");

        for (String path : section.getKeys(false))
        {
            ConfigurationSection pathSection = section.getConfigurationSection(path);

            int inventoryIndex = getIndex(pathSection);

            emptySlots.add(inventoryIndex);
        }
        int[] arr = emptySlots.stream().mapToInt(i -> i).toArray();
        Arrays.sort(arr);
        return arr;
    }

    public int[] getEmptySlotsTarget()
    {
        List<Integer> emptySlots = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("tradeInventory.placeableSlots");

        for (String path : section.getKeys(false))
        {
            ConfigurationSection pathSection = section.getConfigurationSection(path);

            int inventoryIndex = getIndexMirrored(pathSection);

            emptySlots.add(inventoryIndex);
        }
        int[] arr = emptySlots.stream().mapToInt(i -> i).toArray();
        Arrays.sort(arr);
        return arr;
    }

    public int getIndex(ConfigurationSection section)
    {
        int row = section.getInt("row");
        int column = section.getInt("column");

        if (row > 6 || column > 4) throw new IllegalArgumentException();

        return (row - 1) * 9 + column - 1;
    }

    public int getIndexMirrored(ConfigurationSection section)
    {
        int row = section.getInt("row");
        int column = section.getInt("column");

        if (row > 6 || column > 4) throw new IllegalArgumentException();

        return row * 9 - column;
    }
}
