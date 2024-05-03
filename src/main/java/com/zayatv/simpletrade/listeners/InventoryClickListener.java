package com.zayatv.simpletrade.listeners;

import com.zayatv.simpletrade.SimpleTrade;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import org.bukkit.Material;
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

        if (e.getSlot() == econTradeSlot && e.getCurrentItem().isSimilar(econTradeItem) && plugin.isEconomyTradingEnabled())
        {
            isPlayerReady.put(player, false);
            setTradeStatusItem(player, target, tradeInvPlayer, tradeInvTarget);
            openSign(player);
            return;
        }

        if (e.getSlot() == playerConfirmSlot && e.getCurrentItem().isSimilar(confirmItem)) {
            isPlayerReady.put(player, true);
            setTradeStatusItem(player, target, tradeInvPlayer, tradeInvTarget);
        } else if (e.getSlot() == playerConfirmSlot && e.getCurrentItem().isSimilar(readyItem)) {
            isPlayerReady.put(player, false);
            setTradeStatusItem(player, target, tradeInvPlayer, tradeInvTarget);
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

        if (clickedItem.getItemMeta().getPersistentDataContainer().has(econKey))
        {
            plugin.getEconomy().depositPlayer(player, clickedItem.getItemMeta().getPersistentDataContainer().get(econKey, PersistentDataType.DOUBLE));
            return;
        }

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

            Inventory tradeInvPlayer = e.getView().getTopInventory();
            Inventory tradeInvTarget = target.getOpenInventory().getTopInventory();

            int[] placeableSlotsPlayer = plugin.tradeInv.getEmptySlotsPlayer();
            int[] placeableSlotsTarget = plugin.tradeInv.getEmptySlotsTarget();

            updateTradeInvItems(player, tradeInvPlayer, tradeInvTarget, placeableSlotsPlayer, placeableSlotsTarget);
            setTradeStatusItem(player, target, tradeInvPlayer, tradeInvTarget);
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

        itemsToInventory(player, playerItemsList, false);
        itemsToInventory(target, targetItemsList, false);
    }

    private void tradeItems(Player player, Player target)
    {
        List<ItemStack> playerItemsList = playerItems.get(player);
        List<ItemStack> targetItemsList = playerItems.get(target);

        itemsToInventory(target, playerItemsList, true);
        itemsToInventory(player, targetItemsList, true);
    }

    private void itemsToInventory(Player player, List<ItemStack> items, boolean sendGainedMsg)
    {
        String itemsGained = null;
        double coinsGained = 0;

        for (ItemStack item : items)
        {
            if (!item.getItemMeta().getPersistentDataContainer().has(econKey, PersistentDataType.DOUBLE)) {
                player.getInventory().addItem(item);

                if (itemsGained == null) itemsGained = item.getItemMeta().getDisplayName();
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

    private void openSign(Player player)
    {
        SignGUI gui = SignGUI.builder().setLines(null, "^^^^^^", "Enter amount", "---------------").setHandler((p, result) -> {
            String input = result.getLineWithoutColor(0);
            double amount = 0;
            String abbreviation = isAmountAbbreviation(input);

            try {
                amount = Double.parseDouble(input);
            } catch (Exception e) {
                if (abbreviation == null) return closeSignActions(player);
            }

            if (amount == 0)
            {
                try {
                    amount = Double.parseDouble(input.substring(0, input.length() - 1)) * plugin.getConfig().getDouble("tradeInventory.items.econTradeItem.econTrade.abbreviations." + abbreviation);
                } catch (Exception e) {
                    return closeSignActions(player);
                }
            }

            if (amount < 1) return closeSignActions(player);
            if (!plugin.getEconomy().has(player, amount)) return closeSignActions(player);

            String amountAbbreviation = abbreviation == null ? input : input.substring(0, input.length() - 1) + abbreviation;
            String econDisplayName = getEconomyDisplayName(amountAbbreviation);
            String econLore = getEconomyLore(amountAbbreviation);

            ItemStack econItem = getEconItem(econDisplayName, econLore, amount);

            playerItems.get(player).add(econItem);

            plugin.getEconomy().withdrawPlayer(player, amount);

            isPlayerReady.put(plugin.openTrades.get(player), false);
            isPlayerReady.put(player, false);

            return closeSignActions(player);
        }).build();
        inEconomyMenu.add(player);
        gui.open(player);
    }

    private String isAmountAbbreviation(String input)
    {
        List<String> abbreviationKeys = new ArrayList<>(plugin.getConfig().getConfigurationSection("tradeInventory.items.econTradeItem.econTrade.abbreviations").getKeys(false));
        for (String key : abbreviationKeys)
        {
            if (input.substring(input.length() - 1).equalsIgnoreCase(key))
            {
                return key;
            }
        }
        return null;
    }

    private List<SignGUIAction> closeSignActions(Player player)
    {
        return List.of(
                SignGUIAction.runSync(plugin, () -> {
                    plugin.tradeInv.openTradeInventory(player);
                })
        );
    }

    private String getEconomyDisplayName(String amount)
    {
        ConfigurationSection econTradeSection = plugin.getConfig().getConfigurationSection("tradeInventory.items.econTradeItem.econTrade");
        String econName = econTradeSection.getString("economyName");
        String econDisplayName = econTradeSection.getString("displayName");

        econDisplayName = econDisplayName.replace("${economyName}", econName).replace("${amount}", String.valueOf(amount));

        return econDisplayName;
    }

    private String getEconomyLore(String amount)
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

    private ItemStack getEconItem(String displayName, String lore, double econAmount)
    {
        ItemStack item = new ItemStack(Material.matchMaterial(plugin.getConfig().getString("tradeInventory.items.econTradeItem.econTrade.material")), 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(plugin.color(displayName));
        meta.setLore(loreSplit(plugin.color(lore), plugin.getConfig().getInt("tradeInventory.items.econTradeItem.econTrade.loreLineLength")));

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(econKey, PersistentDataType.DOUBLE, econAmount);
        item.setItemMeta(meta);

        return item;
    }

    private void setTradeStatusItem(Player player, Player target, Inventory tradeInvPlayer, Inventory tradeInvTarget)
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
}