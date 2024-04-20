package com.zayatv.simpletrade;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.zayatv.simpletrade.commands.AcceptCmd;
import com.zayatv.simpletrade.commands.TradeCmd;
import com.zayatv.simpletrade.listeners.InventoryClickListener;
import com.zayatv.simpletrade.listeners.PlayerInteractListener;
import com.zayatv.simpletrade.utils.MetaManager;
import com.zayatv.simpletrade.utils.Pair;
import com.zayatv.simpletrade.utils.TradeInv;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SimpleTrade extends JavaPlugin {

    public String prefix = ChatColor.translateAlternateColorCodes('&', "&bSimpleTrade &7>> ");

    public ProtocolManager protocolManager;

    public Map<Pair<Player, Player>, UUID> tradeMap = new HashMap<>();
    public Map<Player, Player> openTrades = new HashMap<>();

    public TradeInv tradeInv = new TradeInv(this);
    public MetaManager metaManager = new MetaManager(this);

    private File configFile;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();

        getCommand("trade").setExecutor(new TradeCmd(this));
        getCommand("accept").setExecutor(new AcceptCmd(this));

        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);

        createConfig();
    }

    @Override
    public void onDisable() {
        saveResource("trade.yml", false);
    }

    public String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
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
}
