package com.zayatv.simpletrade;

import com.zayatv.simpletrade.commands.TradeCmd;
import com.zayatv.simpletrade.listeners.*;
import com.zayatv.simpletrade.utils.ConfigValidator;
import com.zayatv.simpletrade.utils.MetaManager;
import com.zayatv.simpletrade.utils.Pair;
import com.zayatv.simpletrade.utils.TradeInv;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;

public final class SimpleTrade extends JavaPlugin {

    public Map<Pair<Player, Player>, UUID> tradeMap = new HashMap<>();
    public Map<Player, Player> openTrades = new HashMap<>();
    public Map<Pair<Player, Player>, BukkitTask> taskMap = new HashMap<>();

    public TradeInv tradeInv;
    public MetaManager metaManager;

    private ConfigValidator configValidator;

    private Economy economy = null;
    private boolean economyTradingEnabled;

    private File configFile;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        createConfig();

        economyTradingEnabled = getConfig().getBoolean("tradeInventory.items.econTradeItem.enabled");
        if (!setupEconomy()) economyTradingEnabled = false;

        metaManager = new MetaManager(this);
        tradeInv = new TradeInv(this);
        configValidator = new ConfigValidator(this);

        configValidator.validate();

        getCommand("trade").setExecutor(new TradeCmd(this));

        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerPickupItemListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryOpenListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(this), this);
    }

    @Override
    public void onDisable() {
        saveResource("trade.yml", false);
    }

    public String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String prefix()
    {
        return getMessage("prefix");
    }

    public String noPerms()
    {
        return getMessage("noPermission");
    }

    public String getMessage(String path)
    {
        return color(getConfig().getString("messages." + path));
    }

    public List<String> getBlacklistedItems()
    {
        return getConfig().getStringList("tradeInventory.blacklistedItems");
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    private void createConfig() {
        configFile = new File(getDataFolder(), "trade.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("trade.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy()
    {
        return economy;
    }

    public boolean isEconomyTradingEnabled()
    {
        return economyTradingEnabled;
    }
}
