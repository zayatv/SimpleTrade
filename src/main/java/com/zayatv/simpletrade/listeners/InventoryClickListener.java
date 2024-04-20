package com.zayatv.simpletrade.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.zayatv.simpletrade.SimpleTrade;
import com.zayatv.simpletrade.utils.SignGUIAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class InventoryClickListener implements Listener {

    private final SimpleTrade plugin;

    private Map<Player, Boolean> isPlayerReady = new HashMap<>();
    private Map<Player, List<ItemStack>> playerItems = new HashMap<>();
    private List<Player> inEconomyMenu = new ArrayList<>();

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

        Inventory tradeInvPlayer = player.getOpenInventory().getTopInventory();
        Inventory tradeInvTarget = target.getOpenInventory().getTopInventory();
        ItemStack clickedItem = e.getCurrentItem();

        int[] placeableSlotsPlayer = plugin.tradeInv.getEmptySlotsPlayer();
        int[] placeableSlotsTarget = plugin.tradeInv.getEmptySlotsTarget();

        ItemStack confirmItem = plugin.metaManager.getConfirmItem();
        ItemStack readyItem = plugin.metaManager.getReadyItem();
        ItemStack waitingItem = plugin.metaManager.getWaitingItem();
        ItemStack cancelItem = plugin.metaManager.getCancelTradeItem();
        ItemStack econTradeItem = plugin.metaManager.getEconTradeItem();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("tradeInventory.items.tradeStatusItem.position");
        int playerConfirmSlot = plugin.tradeInv.getIndex(section);
        int targetConfirmSlot = plugin.tradeInv.getIndexMirrored(section);

        int econTradeSlot = plugin.tradeInv.getIndex(plugin.getConfig().getConfigurationSection("tradeInventory.items.econTradeItem.position"));

        if (e.getClickedInventory() instanceof PlayerInventory) {
            if (playerItems.get(player).size() >= placeableSlotsPlayer.length) return;
            playerItems.get(player).add(clickedItem);
            updateTradeInvItems(player, tradeInvPlayer, tradeInvTarget, placeableSlotsPlayer, placeableSlotsTarget);
            player.getInventory().setItem(e.getSlot(), null);
            isPlayerReady.put(target, false);
            target.getOpenInventory().getTopInventory().setItem(playerConfirmSlot, confirmItem);
            return;
        }

        if (e.getSlot() == 49 && e.getCurrentItem().isSimilar(cancelItem))
        {
            returnItems(player, target);
            closeInv(player, target);
            return;
        }

        if (e.getSlot() == econTradeSlot && e.getCurrentItem().isSimilar(econTradeItem))
        {
            System.out.println("Econ trade");
            openSign(player);
            return;
        }

        if (e.getSlot() == playerConfirmSlot && e.getCurrentItem().isSimilar(confirmItem)) {
            isPlayerReady.put(player, true);
            target.getOpenInventory().getTopInventory().setItem(targetConfirmSlot, readyItem);
            e.getView().getTopInventory().setItem(playerConfirmSlot, readyItem);
        } else if (e.getSlot() == playerConfirmSlot && e.getCurrentItem().isSimilar(readyItem)) {
            isPlayerReady.put(player, false);
            target.getOpenInventory().getTopInventory().setItem(targetConfirmSlot, waitingItem);
            e.getView().getTopInventory().setItem(playerConfirmSlot, confirmItem);
            return;
        }

        if (isPlayerReady.get(target) && isPlayerReady.get(player)) {
            tradeItems(player, target);
            closeInv(player, target);
            return;
        }

        for (int j = 0; j < placeableSlotsPlayer.length; j++) {
            if (e.getSlot() == placeableSlotsPlayer[j] && e.getCurrentItem() != null) break;
            if (j == placeableSlotsPlayer.length - 1) return;
        }

        playerItems.get(player).remove(clickedItem);
        System.out.println(playerItems.get(player));
        updateTradeInvItems(player, tradeInvPlayer, tradeInvTarget, placeableSlotsPlayer, placeableSlotsTarget);
        player.getInventory().addItem(clickedItem);
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e)
    {
        Player player = (Player) e.getPlayer();

        if (!e.getView().getTitle().equalsIgnoreCase("Trade Menu")) return;
        Player target = plugin.openTrades.get(player);
        if (inEconomyMenu.contains(player))
        {
            inEconomyMenu.remove(player);

            Inventory tradeInvPlayer = player.getOpenInventory().getTopInventory();
            Inventory tradeInvTarget = target.getOpenInventory().getTopInventory();

            int[] placeableSlotsPlayer = plugin.tradeInv.getEmptySlotsPlayer();
            int[] placeableSlotsTarget = plugin.tradeInv.getEmptySlotsTarget();

            updateTradeInvItems(player, tradeInvPlayer, tradeInvTarget, placeableSlotsPlayer, placeableSlotsTarget);
            return;
        }

        if (target == null) return;

        isPlayerReady.put(player, false);
        isPlayerReady.put(target, false);

        playerItems.put(player, new ArrayList<>());
        playerItems.put(target, new ArrayList<>());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e)
    {
        Player player = (Player) e.getPlayer();

        if (!e.getView().getTitle().equalsIgnoreCase("Trade Menu") && !plugin.openTrades.containsKey(player)) return;
        if (inEconomyMenu.contains(player)) return;

        Player target = plugin.openTrades.get(player);

        if (target == null) return;

        returnItems(player, target);

        plugin.openTrades.remove(player, target);
        plugin.openTrades.remove(target, player);
        isPlayerReady.remove(player);
        isPlayerReady.remove(target);
        playerItems.remove(player);
        playerItems.remove(target);
        inEconomyMenu.remove(target);
        if (target.getOpenInventory().getTitle().equalsIgnoreCase("Trade Menu")) target.getOpenInventory().close();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e)
    {
        Player player = e.getPlayer();

        if (!plugin.openTrades.containsKey(player)) return;

        Player target = plugin.openTrades.get(player);

        if (target == null) return;

        returnItems(player, target);

        plugin.openTrades.remove(player, target);
        plugin.openTrades.remove(target, player);
        isPlayerReady.remove(player);
        isPlayerReady.remove(target);
        playerItems.remove(player);
        playerItems.remove(target);
        inEconomyMenu.remove(target);
        if (target.getOpenInventory().getTitle().equalsIgnoreCase("Trade Menu")) target.getOpenInventory().close();
    }

    private void updateTradeInvItems(Player player, Inventory playerInv, Inventory targetInv, int[] playerSlots, int[] targetSlots)
    {
        List<ItemStack> items = playerItems.get(player);
        for (int i = 0; i < playerSlots.length; i++)
        {
            if (items.size() <= i)
            {
                playerInv.setItem(playerSlots[i], null);
                targetInv.setItem(targetSlots[i], null);
                continue;
            }
            playerInv.setItem(playerSlots[i], items.get(i));
            targetInv.setItem(targetSlots[i], items.get(i));
        }
    }

    private void closeInv(Player player, Player target)
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

    private void returnItems(Player player, Player target)
    {
        player.getInventory().addItem(playerItems.get(player).toArray(new ItemStack[playerItems.get(player).size()]));
        target.getInventory().addItem(playerItems.get(target).toArray(new ItemStack[playerItems.get(target).size()]));
    }

    private void tradeItems(Player player, Player target)
    {
        player.getInventory().addItem(playerItems.get(target).toArray(new ItemStack[playerItems.get(target).size()]));
        target.getInventory().addItem(playerItems.get(player).toArray(new ItemStack[playerItems.get(player).size()]));
    }

    private void openSign(Player player)
    {
        SignGUIAPI signGUIAPI = new SignGUIAPI(event -> {
            plugin.tradeInv.openTradeInventory(event.getPlayer());
            System.out.println(event.getLines());
        }, Arrays.asList("First Line", "Second Line", "Third Line", "Fourth Line"), player.getUniqueId(), plugin);
        inEconomyMenu.add(player);
        signGUIAPI.open();
    }
}
