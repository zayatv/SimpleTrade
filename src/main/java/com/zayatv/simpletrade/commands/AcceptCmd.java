package com.zayatv.simpletrade.commands;

import com.zayatv.simpletrade.SimpleTrade;
import com.zayatv.simpletrade.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AcceptCmd implements CommandExecutor {

    private final SimpleTrade plugin;

    private Map<Player, Long> lastSendTradeRequest = new HashMap<>();

    public AcceptCmd(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player target = (Player) sender;

        if (lastSendTradeRequest.containsKey(target) && System.currentTimeMillis() - lastSendTradeRequest.get(target) <= plugin.getConfig().getLong("cooldowns.accept")) return false;
        lastSendTradeRequest.put(target, System.currentTimeMillis());

        Player player = null;
        UUID uuid = null;
        Pair<Player, Player> playerTargetPair = new Pair<>(null, null);
        for (Pair<Player, Player> pair : plugin.tradeMap.keySet())
        {
            if (pair.getValue() != target) continue;
            player = pair.getKey();
            uuid = plugin.tradeMap.get(pair);
            playerTargetPair = pair;
        }

        if (player == null) return false;
        if (args.length != 1) target.sendMessage(plugin.prefix + plugin.color("&cUsage: /accept <tradeUUID>"));

        if (args[0].equalsIgnoreCase(uuid.toString())) {
            Bukkit.getScheduler().cancelTask(TradeCmd.task.getTaskId());
            plugin.openTrades.put(player, target);
            plugin.openTrades.put(target, player);
            plugin.tradeMap.remove(playerTargetPair);
            plugin.tradeInv.openTradeInventory(player);
            plugin.tradeInv.openTradeInventory(target);
        } else {
            target.sendMessage(plugin.prefix + plugin.color("&cThat trade doesn't exist"));
        }

        return false;
    }
}
