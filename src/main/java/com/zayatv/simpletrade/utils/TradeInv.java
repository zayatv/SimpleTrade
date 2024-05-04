package com.zayatv.simpletrade.utils;

import com.zayatv.simpletrade.SimpleTrade;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.logging.Level;

public class TradeInv {

    private final SimpleTrade plugin;

    public Map<Player, Boolean> isPlayerReady = new HashMap<>();
    public Map<Player, List<ItemStack>> playerItems = new HashMap<>();
    public List<Player> inEconomyMenu = new ArrayList<>();

    public NamespacedKey econKey;

    public TradeInv(SimpleTrade plugin) {
        this.plugin = plugin;
        econKey = new NamespacedKey(plugin, "economy");
    }

    public void openTradeInventory(Player player) {
        player.openInventory(getTradeInventory(player));
    }

    public Inventory getTradeInventory(Player player) {
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

        if (plugin.isEconomyTradingEnabled())
        {
            ItemStack econTradeItem = plugin.metaManager.getEconTradeItem();
            inv.setItem(getIndex(plugin.getConfig().getConfigurationSection("tradeInventory.items.econTradeItem.position")), econTradeItem);
        }

        ItemStack cancelTradeItem = plugin.metaManager.getCancelTradeItem();
        inv.setItem(49, cancelTradeItem);

        return inv;
    }

    public void closeInv(Player player, Player target)
    {
        isPlayerReady.remove(player);
        isPlayerReady.remove(target);
        playerItems.remove(player);
        playerItems.remove(target);
        plugin.openTrades.remove(player, target);
        plugin.openTrades.remove(target, player);
        inEconomyMenu.remove(player);
        inEconomyMenu.remove(target);
        player.getOpenInventory().close();
        target.getOpenInventory().close();
    }

    public void updateTradeInvItems(Player player, Player target, Inventory playerInv, Inventory targetInv, int[] playerSlots, int[] targetSlots)
    {
        List<ItemStack> itemsPlayer = playerItems.get(player);
        List<ItemStack> itemsTarget = playerItems.get(target);
        for (int i = 0; i < playerSlots.length; i++)
        {
            if (i < itemsPlayer.size())
            {
                playerInv.setItem(playerSlots[i], itemsPlayer.get(i));
                targetInv.setItem(targetSlots[i], itemsPlayer.get(i));
            }
            else
            {
                playerInv.setItem(playerSlots[i], null);
                targetInv.setItem(targetSlots[i], null);
            }

            if (i < itemsTarget.size())
            {
                playerInv.setItem(targetSlots[i], itemsTarget.get(i));
                targetInv.setItem(playerSlots[i], itemsTarget.get(i));
            }
            else
            {
                playerInv.setItem(targetSlots[i], null);
                targetInv.setItem(playerSlots[i], null);
            }
        }
    }

    public void returnItems(Player player, Player target)
    {
        List<ItemStack> playerItemsList = playerItems.get(player);
        List<ItemStack> targetItemsList = playerItems.get(target);

        itemsToInventory(player, playerItemsList, false);
        itemsToInventory(target, targetItemsList, false);
    }

    public void tradeItems(Player player, Player target)
    {
        List<ItemStack> playerItemsList = plugin.tradeInv.playerItems.get(player);
        List<ItemStack> targetItemsList = plugin.tradeInv.playerItems.get(target);

        if (playerItemsList.size() > getEmptySlots(player) || targetItemsList.size() > getEmptySlots(target))
        {
            player.sendMessage(plugin.prefix() + plugin.getMessage("trade.errorMessages.fullInventory"));
            return;
        }

        plugin.tradeInv.itemsToInventory(target, playerItemsList, true);
        plugin.tradeInv.itemsToInventory(player, targetItemsList, true);
    }

    private int getEmptySlots(Player player)
    {
        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();
        int i = 0;
        for (ItemStack item : contents)
        {
            if (item != null && item.getType() != Material.AIR) {
                i++;
            }
        }
        return inv.getSize() - i;
    }

    public void itemsToInventory(Player player, List<ItemStack> items, boolean sendGainedMsg)
    {
        String itemsGained = "";
        double coinsGained = 0;

        for (ItemStack item : items)
        {
            if (!item.getItemMeta().getPersistentDataContainer().has(econKey, PersistentDataType.DOUBLE)) {
                player.getInventory().addItem(item);

                if (itemsGained.trim().isEmpty()) itemsGained = item.getItemMeta().getDisplayName();
                else itemsGained += ", " + item.getItemMeta().getDisplayName();

                continue;
            }

            double econAmount = item.getItemMeta().getPersistentDataContainer().get(econKey, PersistentDataType.DOUBLE);
            plugin.getEconomy().depositPlayer(player, econAmount);
            coinsGained += econAmount;
        }

        if (!sendGainedMsg) return;
        if (itemsGained != null) player.sendMessage(plugin.prefix() + plugin.getMessage("trade.itemsGained").replace("${items}", itemsGained));
        if (coinsGained > 0.0) player.sendMessage(plugin.prefix() + plugin.getMessage("trade.ecoGained").replace("${amount}", String.valueOf(coinsGained)));
    }

    public void setTradeStatusItem(Player player, Player target, Inventory tradeInvPlayer, Inventory tradeInvTarget)
    {
        ItemStack confirmItem = plugin.metaManager.getConfirmItem();
        ItemStack readyItem = plugin.metaManager.getReadyItem();
        ItemStack waitingItem = plugin.metaManager.getWaitingItem();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("tradeInventory.items.tradeStatusItem.position");
        int playerConfirmSlot = plugin.tradeInv.getIndex(section);
        int targetConfirmSlot = plugin.tradeInv.getIndexMirrored(section);

        if (isPlayerReady.get(player))
        {
            tradeInvPlayer.setItem(playerConfirmSlot, readyItem);
            tradeInvTarget.setItem(targetConfirmSlot, readyItem);
        }
        else
        {
            tradeInvPlayer.setItem(playerConfirmSlot, confirmItem);
            tradeInvTarget.setItem(targetConfirmSlot, waitingItem);
        }

        if (isPlayerReady.get(target))
        {
            tradeInvPlayer.setItem(targetConfirmSlot, readyItem);
            tradeInvTarget.setItem(playerConfirmSlot, readyItem);
        }
        else
        {
            tradeInvPlayer.setItem(targetConfirmSlot, waitingItem);
            tradeInvTarget.setItem(playerConfirmSlot, confirmItem);
        }
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

        if (row > 6 || column > 4)
        {
            plugin.getLogger().log(Level.SEVERE, "Your trade inventory index is out of bounds!");
            throw new IllegalArgumentException();
        }

        return (row - 1) * 9 + column - 1;
    }

    public int getIndexMirrored(ConfigurationSection section)
    {
        int row = section.getInt("row");
        int column = section.getInt("column");

        if (row > 6 || column > 4)
        {
            plugin.getLogger().log(Level.SEVERE, "Your trade inventory index is out of bounds!");
            throw new IllegalArgumentException();
        }

        return row * 9 - column;
    }
}
