package eu.zzagro.simpletrade.commands;

import eu.zzagro.simpletrade.SimpleTrade;
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

    public UUID uuid;

    public static BukkitTask task;

    private final SimpleTrade plugin;

    public TradeCmd(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        if(!command.getName().equalsIgnoreCase("trade")) return false;

        Player player = (Player) sender;

        if (!player.hasPermission("simpletrade.trade")) return false;

        if (args.length != 1)
        {
            player.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&cUsage: /trade <player>"));
            return false;
        }

        if (Bukkit.getPlayerExact(args[0]) == null) {
            player.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&6" + args[0] + " &cisn't online or doesn't exist!"));
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == player) {
            player.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&cYou can't trade with yourself!"));
            return false;
        }

        if (!(plugin.getTargetMap.containsKey(player))) {
            target.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&6" + player.getName() + " &ahas sent you a trade request!"));

            uuid = UUID.randomUUID();
            plugin.targetUuidMap.put(target, uuid);
            plugin.playerUuidMap.put(player, uuid);

            plugin.getPlayerMap.put(target, player);
            plugin.getTargetMap.put(player, target);
            player.sendMessage("Player: " + plugin.getPlayerMap.get(target) + ", Target: " + plugin.getTargetMap.get(player));
            target.sendMessage("Player: " + plugin.getPlayerMap.get(target) + ", Target: " + plugin.getTargetMap.get(player));

            target.spigot().sendMessage(tradeText(uuid));
            player.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&aYou've sent a trade request to &6" + target.getName()));

            tradeExpired(player, target, 20*15);
        } else if (!(plugin.getTargetMap.get(player).getName().equalsIgnoreCase(target.getDisplayName()) && plugin.getPlayerMap.get(target).getName().equalsIgnoreCase(player.getDisplayName()))) {
            target.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&6" + player.getName() + " &ahas sent you a trade request!"));

            uuid = UUID.randomUUID();
            plugin.targetUuidMap.put(target, uuid);
            plugin.playerUuidMap.put(player, uuid);

            plugin.getPlayerMap.put(target, player);
            plugin.getTargetMap.put(player, target);

            target.spigot().sendMessage(tradeText(uuid));
            player.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&aYou've sent a trade request to &6" + target.getName()));

            tradeExpired(player, target, 20*15);
        } else {
            player.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&cYou already have an outgoing trade request to this player!"));
        }

        return false;
    }

    private void tradeExpired(Player player, Player target, int time)
    {
        task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            plugin.targetUuidMap.remove(target);
            plugin.getPlayerMap.remove(target);
            plugin.getTargetMap.remove(player);
            player.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&cYour trade request to &6" + target.getName() + " &cexpired!"));
            target.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&cThe trade request from &6" + player.getName() + " &cexpired!"));
        }, 20*15);
    }

    private TextComponent tradeText(UUID uuid)
    {
        TextComponent accept = new TextComponent();
        accept.setText("[ACCEPT] ");
        accept.setColor(ChatColor.GREEN);
        accept.setBold(true);
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept " + uuid));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to accept").create()));

        TextComponent deny = new TextComponent();
        deny.setText("[DENY]");
        deny.setColor(ChatColor.RED);
        deny.setBold(true);
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/deny " + uuid));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to deny").create()));

        TextComponent text = new TextComponent();
        text.setText(SimpleTrade.color(SimpleTrade.prefix + "&6Do you want to accept? "));
        text.addExtra(accept);
        accept.addExtra(deny);

        return text;
    }
}
