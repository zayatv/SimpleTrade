package com.zayatv.simpletrade.listeners;

import com.zayatv.simpletrade.SimpleTrade;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import de.rapha149.signgui.SignGUIResult;
import org.bukkit.NamespacedKey;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.ChatPaginator;

import java.util.*;

public class InventoryClickListener implements Listener {

    private final SimpleTrade plugin;

    private Map<Player, Boolean> isPlayerReady = new HashMap<>();
    private Map<Player, List<ItemStack>> playerItems = new HashMap<>();
    private List<Player> inEconomyMenu = new ArrayList<>();

    private NamespacedKey econKey;

    public InventoryClickListener(SimpleTrade plugin) {
        this.plugin = plugin;
        econKey = new NamespacedKey(plugin, "economy");
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
            System.out.println("help");
            if (i < items.size())
            {
                playerInv.setItem(playerSlots[i], items.get(i));
                targetInv.setItem(targetSlots[i], items.get(i));
                continue;
            }
            playerInv.setItem(playerSlots[i], null);
            targetInv.setItem(targetSlots[i], null);
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
        List<ItemStack> playerItemsList = playerItems.get(player);
        List<ItemStack> targetItemsList = playerItems.get(target);

        itemsToInventory(player, playerItemsList);
        itemsToInventory(target, targetItemsList);
    }

    private void tradeItems(Player player, Player target)
    {
        List<ItemStack> playerItemsList = playerItems.get(player);
        List<ItemStack> targetItemsList = playerItems.get(target);

        itemsToInventory(target, playerItemsList);
        itemsToInventory(player, targetItemsList);
    }

    private void itemsToInventory(Player player, List<ItemStack> items)
    {
        for (ItemStack item : items)
        {
            if (!item.getItemMeta().getPersistentDataContainer().has(econKey, PersistentDataType.LONG)) {
                player.getInventory().addItem(item);
                continue;
            }

            long econAmount = item.getItemMeta().getPersistentDataContainer().get(econKey, PersistentDataType.LONG);
            player.sendMessage(plugin.color("&6You got: " + econAmount + " Coins"));
        }
    }

    private void openSign(Player player)
    {
        SignGUI gui = SignGUI.builder().setLine(0, "Type amount below").setHandler((p, result) -> {
            String input = result.getLineWithoutColor(1);
            long amount;
            try {
                amount = Long.parseLong(input);
            } catch (Exception e) {
                return List.of(
                        SignGUIAction.openInventory(plugin, plugin.tradeInv.getTradeInventory(player))
                );
            }

            String econDisplayName = getEconomyDisplayName(amount);
            String econLore = getEconomyLore(amount);

            ItemStack econItem = plugin.metaManager.getEconItem();
            ItemMeta econMeta = plugin.metaManager.getEconMeta();
            econMeta.setDisplayName(plugin.color(econDisplayName));
            econMeta.setLore(loreSplit(plugin.color(econLore), plugin.getConfig().getInt("tradeInventory.items.econTradeItem.econTrade.loreLineLength")));

            PersistentDataContainer pdc = econMeta.getPersistentDataContainer();
            pdc.set(econKey, PersistentDataType.LONG, amount);

            econItem.setItemMeta(econMeta);

            playerItems.get(player).add(econItem);

            return List.of(
                    SignGUIAction.openInventory(plugin, plugin.tradeInv.getTradeInventory(player))
//                    SignGUIAction.runSync(plugin, () -> {
//                        plugin.tradeInv.openTradeInventory(player);
//                    })
            );
        }).build();
        inEconomyMenu.add(player);
        gui.open(player);
    }

    private String getEconomyDisplayName(long amount)
    {
        ConfigurationSection econTradeSection = plugin.getConfig().getConfigurationSection("tradeInventory.items.econTradeItem.econTrade");
        String econName = econTradeSection.getString("economyName");
        String econDisplayName = econTradeSection.getString("displayName");

        econDisplayName = econDisplayName.replace("${economyName}", econName).replace("${amount}", String.valueOf(amount));

        return econDisplayName;
    }

    private String getEconomyLore(long amount)
    {
        ConfigurationSection econTradeSection = plugin.getConfig().getConfigurationSection("tradeInventory.items.econTradeItem.econTrade");
        String econName = econTradeSection.getString("economyName");
        String econLore = econTradeSection.getString("lore");

        econLore = econLore.replace("${economyName}", econName).replace("${amount}", String.valueOf(amount));

        return econLore;
    }

    private List<String> loreSplit(String lore, int lineLength)
    {
        return Arrays.asList(lore.split("(?<=\\G.{" + lineLength + "})"));
    }
}
