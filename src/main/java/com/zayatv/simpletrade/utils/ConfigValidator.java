package com.zayatv.simpletrade.utils;

import com.zayatv.simpletrade.SimpleTrade;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigValidator {

    private final SimpleTrade plugin;

    public ConfigValidator(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    public void validate()
    {
        denyMessage();
    }

    private void denyMessage()
    {
        if (plugin.getConfig().contains("messages.trade.denied")) return;
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("messages.trade");
        section.set("denied", "&cYou denied the trade request from ${player}");
    }
}
