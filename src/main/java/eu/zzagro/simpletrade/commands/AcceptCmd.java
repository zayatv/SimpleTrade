package eu.zzagro.simpletrade.commands;

import eu.zzagro.simpletrade.SimpleTrade;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AcceptCmd implements CommandExecutor {

    private final SimpleTrade plugin;

    public AcceptCmd(SimpleTrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;
        Player target = plugin.getTargetMap.get(player);
        //Just here to check
        player.sendMessage("Player: " + player.getName() + ", Target: " + target.getName());
        if (target == null) return false;
        if (args.length != 1) player.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&cUsage: /accept <tradeUUID>"));

        if (plugin.targetUuidMap.containsKey(player)) {
            UUID uuid = plugin.targetUuidMap.get(player);
            if (args[0].equalsIgnoreCase(uuid.toString())) {
                Bukkit.getScheduler().cancelTask(TradeCmd.task.getTaskId());
                plugin.tradeInv.openTargetInv(target);
                plugin.tradeInv.openPlayerInv(player);
            } else {
                player.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&cThat trade doesn't exist"));
            }
        } else {
            player.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&cYou don't have an outgoing trade request!"));
        }

        return false;
    }
}
