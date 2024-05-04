package com.zayatv.simpletrade.utils;

import com.zayatv.simpletrade.SimpleTrade;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;

public class ConfigValidator {

    private final SimpleTrade plugin;

    public ConfigValidator(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    public void validate()
    {
        denyMessage();
        tradeTextBold();
        fullInventoryError();
        blacklistedItems();
    }

    private void denyMessage()
    {
        if (plugin.getConfig().contains("messages.trade.denied")) return;
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("messages.trade");
        section.set("denied", "&cYou denied the trade request from ${player}");
    }

    private void tradeTextBold()
    {
        if (plugin.getConfig().contains("messages.trade.tradeText.accept.bold"))
        {
            plugin.getConfig().set("messages.trade.tradeText.accept.bold", null);
        }

        if (plugin.getConfig().contains("messages.trade.tradeText.deny.bold"))
        {
            plugin.getConfig().set("messages.trade.tradeText.deny.bold", null);
        }
    }

    private void fullInventoryError()
    {
        if (plugin.getConfig().contains("messages.trade.errorMessages.fullInventory")) return;
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("messages.trade.errorMessages");
        section.set("fullInventory", "&cYour or the other player's inventory doesn't contain enough space to store the traded items!");
    }

    private void blacklistedItems()
    {
        if (plugin.getConfig().contains("tradeInventory.blacklistedItems")) return;
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("tradeInventory");
        section.set("blacklistedItems", Arrays.asList("Anvil", "Dirt"));
    }
}
