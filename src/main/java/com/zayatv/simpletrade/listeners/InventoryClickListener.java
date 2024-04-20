package com.zayatv.simpletrade.listeners;

import com.zayatv.simpletrade.SimpleTrade;
import com.zayatv.simpletrade.utils.SignGUIAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class InventoryClickListener implements Listener {

    private final SimpleTrade plugin;

    private Map<Player, Boolean> isPlayerReady = new HashMap<>();
    private Map<Player, List<ItemStack>> playerItems = new HashMap<>();

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

        if (!isPlayerReady.containsKey(player) || !isPlayerReady.containsKey(target))
        {
            isPlayerReady.put(player, false);
            isPlayerReady.put(target, false);
        }

        if (!playerItems.containsKey(player) || !playerItems.containsKey(target))
        {
            playerItems.put(player, new ArrayList<>());
            playerItems.put(target, new ArrayList<>());
        }

        int[] placeableSlotsPlayer = plugin.tradeInv.getEmptySlotsPlayer();
        int[] placeableSlotsTarget = plugin.tradeInv.getEmptySlotsTarget();

        ItemStack confirmItem = plugin.metaManager.getConfirmItem();
        ItemStack readyItem = plugin.metaManager.getReadyItem();
        ItemStack waitingItem = plugin.metaManager.getWaitingItem();
        ItemStack cancelItem = plugin.metaManager.getCancelTradeItem();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("tradeInventory.items.tradeStatusItem.position");
        int playerConfirmSlot = plugin.tradeInv.getIndex(section);
        int targetConfirmSlot = plugin.tradeInv.getIndexMirrored(section);

        if (e.getClickedInventory() instanceof PlayerInventory) {
//            for (int i = 0; i < placeableSlotsPlayer.length; i++)
//            {
//                if (tradeInvPlayer.getContents()[placeableSlotsPlayer[i]] != null) continue;
//                tradeInvPlayer.setItem(placeableSlotsPlayer[i], clickedItem);
//                tradeInvTarget.setItem(placeableSlotsTarget[i], clickedItem);
//                break;
//            }
            playerItems.get(player).add(clickedItem);
            updateTradeInvItems(player, tradeInvPlayer, tradeInvTarget, placeableSlotsPlayer, placeableSlotsTarget);
            player.getInventory().setItem(e.getSlot(), null);
            isPlayerReady.put(target, false);
            target.getOpenInventory().getTopInventory().setItem(targetConfirmSlot, confirmItem);
            return;
        }

        if (e.getSlot() == 49 && e.getCurrentItem().isSimilar(cancelItem))
        {
            closeInv(player, target);
            returnItems(player, target);
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
        }

        if (isPlayerReady.get(target) && isPlayerReady.get(player)) {
            tradeItems(player, target);
            closeInv(player, target);
            return;
        }

        for (int i = 0; i < placeableSlotsPlayer.length; i++)
        {
            if (e.getSlot() != placeableSlotsPlayer[i]) continue;
            if (e.getCurrentItem() == null) continue;

            playerItems.get(player).remove(i);

            updateTradeInvItems(player, tradeInvPlayer, tradeInvTarget, placeableSlotsPlayer, placeableSlotsTarget);

            player.getInventory().addItem(clickedItem);
            return;
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e)
    {
        Player player = (Player) e.getPlayer();

        if (!e.getView().getTitle().equalsIgnoreCase("Trade Menu") && !plugin.openTrades.containsKey(player)) return;

        Player target = plugin.openTrades.get(player);

        if (target == null) return;

        returnItemsPlayer(player, plugin.tradeInv.getEmptySlotsPlayer(), e.getView().getTopInventory());
        returnItemsPlayer(target, plugin.tradeInv.getEmptySlotsPlayer(), target.getOpenInventory().getTopInventory());

        plugin.openTrades.remove(player, target);
        plugin.openTrades.remove(target, player);
        isPlayerReady.remove(player);
        isPlayerReady.remove(target);
        playerItems.remove(player);
        playerItems.remove(target);
        if (target.getOpenInventory().getTitle().equalsIgnoreCase("Trade Menu")) target.getOpenInventory().close();
    }

    private void updateTradeInvItems(Player player, Inventory playerInv, Inventory targetInv, int[] playerSlots, int[] targetSlots)
    {
        List<ItemStack> items = playerItems.get(player);
        for (int i = 0; i < playerSlots.length; i++)
        {
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
        player.getOpenInventory().close();
        target.getOpenInventory().close();
    }

    private void returnItemsPlayer(Player player, int[] slots, Inventory inv)
    {
        if (!player.getOpenInventory().getTitle().equalsIgnoreCase("Trade Menu")) return;

        for (int slot : slots) {
            if (inv.getItem(slot) == null) continue;
            player.getInventory().addItem(inv.getItem(slot));
            inv.setItem(slot, null);
        }
    }

    private void returnItems(Player player, Player target)
    {
        returnItemsPlayer(player, plugin.tradeInv.getEmptySlotsPlayer(), player.getOpenInventory().getTopInventory());
        returnItemsPlayer(target, plugin.tradeInv.getEmptySlotsPlayer(), target.getOpenInventory().getTopInventory());
    }

    private void tradeItems(Player player, Player target)
    {
        returnItemsPlayer(player, plugin.tradeInv.getEmptySlotsTarget(), player.getOpenInventory().getTopInventory());
        returnItemsPlayer(target, plugin.tradeInv.getEmptySlotsTarget(), target.getOpenInventory().getTopInventory());
    }

    private void openSign(Player player)
    {
        SignGUIAPI signGUIAPI = new SignGUIAPI(event -> plugin.tradeInv.openTradeInventory(event.getPlayer()), Arrays.asList("First Line"), player.getUniqueId(), plugin);
        signGUIAPI.open();
    }
}
