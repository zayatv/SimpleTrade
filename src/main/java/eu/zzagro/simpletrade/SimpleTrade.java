package eu.zzagro.simpletrade;

import eu.zzagro.simpletrade.commands.AcceptCmd;
import eu.zzagro.simpletrade.commands.TradeCmd;
import eu.zzagro.simpletrade.listeners.InventoryClickListener;
import eu.zzagro.simpletrade.listeners.PlayerInteractListener;
import eu.zzagro.simpletrade.utils.MetaManager;
import eu.zzagro.simpletrade.utils.Pair;
import eu.zzagro.simpletrade.utils.TradeInv;
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

    public static String prefix = ChatColor.translateAlternateColorCodes('&', "&bSimpleTrade &7>> ");

    private File configFile;
    private FileConfiguration config;

    public Map<Pair<Player, Player>, UUID> tradeMap = new HashMap<>();
    public Map<Player, Player> openTrades = new HashMap<>();

    public TradeInv tradeInv = new TradeInv(this);
    public MetaManager metaManager = new MetaManager();

    @Override
    public void onEnable() {
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

    public static String color(String message) {
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
