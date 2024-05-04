package com.zayatv.simpletrade.commands;

import com.zayatv.simpletrade.utils.Pair;
import com.zayatv.simpletrade.SimpleTrade;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeCmd implements CommandExecutor {

    private Map<Player, Long> lastSendTradeRequest = new HashMap<>();
    private Map<Player, Long> lastSendTradeAccept = new HashMap<>();

    private final SimpleTrade plugin;

    public TradeCmd(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        if(!command.getName().equalsIgnoreCase("trade")) return false;

        if (args.length >= 1 && args[0].equalsIgnoreCase("accept"))
        {
            Player target = (Player) sender;

            if (!target.hasPermission("simpletrade.trade"))
            {
                target.sendMessage(plugin.prefix() + plugin.getMessage("noPermission"));
                return false;
            }

            if (args.length != 2)
            {
                target.sendMessage(plugin.prefix() + plugin.color("&cUsage: /trade accept <tradeUUID>"));
                return false;
            }

            if (lastSendTradeAccept.containsKey(target) && System.currentTimeMillis() - lastSendTradeAccept.get(target) <= plugin.getConfig().getLong("cooldowns.accept")) return false;
            lastSendTradeAccept.put(target, System.currentTimeMillis());

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

            if (args[1].equalsIgnoreCase(uuid.toString())) {
                BukkitTask task = plugin.taskMap.containsKey(playerTargetPair) ? plugin.taskMap.get(playerTargetPair) : plugin.taskMap.get(playerTargetPair.reversed());
                Bukkit.getScheduler().cancelTask(task.getTaskId());
                plugin.openTrades.put(player, target);
                plugin.openTrades.put(target, player);
                plugin.tradeMap.remove(playerTargetPair);
                plugin.tradeInv.openTradeInventory(player);
                plugin.tradeInv.openTradeInventory(target);
            } else {
                target.sendMessage(plugin.prefix() + plugin.getMessage("trade.errorMessages.wrongTradeUUID"));
                return false;
            }

            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("deny"))
        {
            Player target = (Player) sender;

            if (!target.hasPermission("simpletrade.trade"))
            {
                target.sendMessage(plugin.prefix() + plugin.getMessage("noPermission"));
                return false;
            }

            if (args.length != 2)
            {
                target.sendMessage(plugin.prefix() + plugin.color("&cUsage: /trade deny <tradeUUID>"));
                return false;
            }

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

            if (args[1].equalsIgnoreCase(uuid.toString())) {
                BukkitTask task = plugin.taskMap.containsKey(playerTargetPair) ? plugin.taskMap.get(playerTargetPair) : plugin.taskMap.get(playerTargetPair.reversed());
                Bukkit.getScheduler().cancelTask(task.getTaskId());
                plugin.tradeMap.remove(playerTargetPair);
                target.sendMessage(plugin.prefix() + plugin.getMessage("trade.denied").replace("${player}", player.getName()));
            } else {
                target.sendMessage(plugin.prefix() + plugin.getMessage("trade.errorMessages.wrongTradeUUID"));
                return false;
            }

            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("simpletrade.trade"))
        {
            player.sendMessage(plugin.prefix() + plugin.getMessage("noPermission"));
            return false;
        }

        if (args.length != 1)
        {
            player.sendMessage(plugin.prefix() + plugin.color("&cUsage: /trade <player>"));
            return false;
        }

        if (Bukkit.getPlayerExact(args[0]) == null) {
            player.sendMessage(plugin.prefix() + plugin.getMessage("trade.errorMessages.invalidPlayer").replace("${player}", player.getName()));
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == player) {
            player.sendMessage(plugin.prefix() + plugin.getMessage("trade.errorMessages.tradeSelf"));
            return false;
        }

        Pair<Player, Player> playerTargetPair = new Pair<>(player, target);
        boolean outgoingRequest = plugin.tradeMap.containsKey(playerTargetPair);
        boolean incomingRequest = plugin.tradeMap.containsKey(playerTargetPair.reversed());
        UUID uuid = plugin.tradeMap.get(playerTargetPair.reversed());

        if (incomingRequest)
        {
            player.performCommand("trade accept " + uuid);
            lastSendTradeRequest.put(player, System.currentTimeMillis());
            return true;
        }

        if (lastSendTradeRequest.containsKey(player) && System.currentTimeMillis() - lastSendTradeRequest.get(player) <= plugin.getConfig().getLong("cooldowns.trade")) return false;
        lastSendTradeRequest.put(player, System.currentTimeMillis());

        if (!outgoingRequest) {
            target.sendMessage(plugin.prefix() + plugin.getMessage("trade.received").replace("${player}", player.getName()));

            uuid = UUID.randomUUID();
            plugin.tradeMap.put(playerTargetPair, uuid);

            target.spigot().sendMessage(tradeText(uuid));
            player.sendMessage(plugin.prefix() + plugin.getMessage("trade.sent").replace("${player}", target.getName()));

            tradeExpired(playerTargetPair, (long) (20 * plugin.getConfig().getDouble("cooldowns.secondsUntilTradeExpires")));
            return true;
        }

        player.sendMessage(plugin.prefix() + plugin.getMessage("trade.errorMessages.requestExists").replace("${player}", target.getName()));

        return false;
    }

    private void tradeExpired(Pair<Player, Player> pair, long time)
    {
        BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            plugin.tradeMap.remove(pair);
            pair.getKey().sendMessage(plugin.prefix() + plugin.getMessage("trade.expiredTo").replace("${player}", pair.getValue().getName()));
            pair.getValue().sendMessage(plugin.prefix() + plugin.getMessage("trade.expiredFrom").replace("${player}", pair.getKey().getName()));
        }, time);

        plugin.taskMap.put(pair, task);
    }

    private TextComponent tradeText(UUID uuid)
    {
        TextComponent accept = new TextComponent();
        accept.setText(plugin.getMessage("trade.tradeText.accept.text") + " ");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade accept " + uuid));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(plugin.getMessage("trade.tradeText.accept.hover")).create()));

        TextComponent deny = new TextComponent();
        deny.setText(plugin.getMessage("trade.tradeText.deny.text"));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade deny " + uuid));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(plugin.getMessage("trade.tradeText.deny.hover")).create()));

        TextComponent text = new TextComponent();
        text.setText(plugin.prefix() + plugin.getMessage("trade.tradeText.text") + " ");
        text.addExtra(accept);
        text.addExtra(deny);

        return text;
    }
}
