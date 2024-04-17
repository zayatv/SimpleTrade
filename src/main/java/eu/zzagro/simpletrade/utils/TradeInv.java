package eu.zzagro.simpletrade.utils;

import eu.zzagro.simpletrade.SimpleTrade;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TradeInv {

    private final SimpleTrade plugin;

    public TradeInv(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    public void openTradeInventory(Player player) {
        Inventory inv = Bukkit.createInventory(player, 54, "Trade Menu");

        ItemStack unplaceableItem = plugin.metaManager.unplaceableItem;
        ItemMeta unplaceableMeta = plugin.metaManager.getUnplaceableMeta();
        for (int i = 0; i < inv.getSize(); i++)
        {
            inv.setItem(i, unplaceableItem);
        }

        ItemStack emptyItem = plugin.metaManager.emptyItem;
        int[] emptySlots = getEmptySlots();
        for (int emptySlot : emptySlots) {
            inv.setItem(emptySlot, emptyItem);
        }

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("tradeInventory.confirmItem");

        ItemStack confirmItem = plugin.metaManager.confirmItem;
        ItemMeta confirmMeta = plugin.metaManager.getConfirmMeta();
        inv.setItem(getIndex(section), confirmItem);

        ItemStack waitingItem = plugin.metaManager.waitingItem;
        ItemMeta waitingMeta = plugin.metaManager.getWaitingMeta();
        inv.setItem(getIndexMirrored(section), waitingItem);

        ItemStack cancelTradeItem = plugin.metaManager.cancelTradeItem;
        ItemMeta cancelTradeMeta = plugin.metaManager.getCancelTradeMeta();
        inv.setItem(49, cancelTradeItem);

        player.openInventory(inv);
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
