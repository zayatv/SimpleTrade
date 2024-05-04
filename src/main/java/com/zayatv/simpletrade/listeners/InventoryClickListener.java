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
            if (plugin.tradeInv.playerItems.get(player).size() >= placeableSlotsPlayer.length) return;
            plugin.tradeInv.playerItems.get(player).add(clickedItem);
            plugin.tradeInv.updateTradeInvItems(player, tradeInvPlayer, tradeInvTarget, placeableSlotsPlayer, placeableSlotsTarget);
            player.getInventory().setItem(e.getSlot(), null);
            plugin.tradeInv.isPlayerReady.put(target, false);
            target.getOpenInventory().getTopInventory().setItem(playerConfirmSlot, confirmItem);
            return;
        }

        if (e.getSlot() == 49 && e.getCurrentItem().isSimilar(cancelItem))
        {
            plugin.tradeInv.returnItems(player, target);
            plugin.tradeInv.closeInv(player, target);
            return;
        }

        if (e.getSlot() == econTradeSlot && e.getCurrentItem().isSimilar(econTradeItem) && plugin.isEconomyTradingEnabled())
        {
            plugin.tradeInv.isPlayerReady.put(player, false);
            plugin.tradeInv.setTradeStatusItem(player, target, tradeInvPlayer, tradeInvTarget);
            openSign(player);
            return;
        }

        if (e.getSlot() == playerConfirmSlot && e.getCurrentItem().isSimilar(confirmItem)) {
            plugin.tradeInv.isPlayerReady.put(player, true);
            plugin.tradeInv.setTradeStatusItem(player, target, tradeInvPlayer, tradeInvTarget);
        } else if (e.getSlot() == playerConfirmSlot && e.getCurrentItem().isSimilar(readyItem)) {
            plugin.tradeInv.isPlayerReady.put(player, false);
            plugin.tradeInv.setTradeStatusItem(player, target, tradeInvPlayer, tradeInvTarget);
            return;
        }

        if (plugin.tradeInv.isPlayerReady.get(target) && plugin.tradeInv.isPlayerReady.get(player)) {
            plugin.tradeInv.tradeItems(player, target);
            plugin.tradeInv.closeInv(player, target);
            return;
        }

        for (int j = 0; j < placeableSlotsPlayer.length; j++) {
            if (e.getSlot() == placeableSlotsPlayer[j] && e.getCurrentItem() != null) break;
            if (j == placeableSlotsPlayer.length - 1) return;
        }

        plugin.tradeInv.playerItems.get(player).remove(clickedItem);
        System.out.println(plugin.tradeInv.playerItems.get(player));
        plugin.tradeInv.updateTradeInvItems(player, tradeInvPlayer, tradeInvTarget, placeableSlotsPlayer, placeableSlotsTarget);

        if (clickedItem.getItemMeta().getPersistentDataContainer().has(plugin.tradeInv.econKey))
        {
            plugin.getEconomy().depositPlayer(player, clickedItem.getItemMeta().getPersistentDataContainer().get(plugin.tradeInv.econKey, PersistentDataType.DOUBLE));
            return;
        }

        player.getInventory().addItem(clickedItem);
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

            plugin.tradeInv.playerItems.get(player).add(econItem);

            plugin.getEconomy().withdrawPlayer(player, amount);

            plugin.tradeInv.isPlayerReady.put(plugin.openTrades.get(player), false);
            plugin.tradeInv.isPlayerReady.put(player, false);

            return closeSignActions(player);
        }).build();
        plugin.tradeInv.inEconomyMenu.add(player);
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
        pdc.set(plugin.tradeInv.econKey, PersistentDataType.DOUBLE, econAmount);
        item.setItemMeta(meta);

        return item;
    }
}