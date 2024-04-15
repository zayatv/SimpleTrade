package eu.zzagro.simpletrade.commands;

import eu.zzagro.simpletrade.SimpleTrade;
import eu.zzagro.simpletrade.utils.Pair;
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

        Player target = (Player) sender;
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
        if (args.length != 1) target.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&cUsage: /accept <tradeUUID>"));

        if (args[0].equalsIgnoreCase(uuid.toString())) {
            Bukkit.getScheduler().cancelTask(TradeCmd.task.getTaskId());
            plugin.tradeInv.openTargetInv(player);
            plugin.tradeInv.openPlayerInv(target);
            plugin.tradeMap.remove(playerTargetPair);
        } else {
            target.sendMessage(SimpleTrade.prefix + SimpleTrade.color("&cThat trade doesn't exist"));
        }

        return false;
    }
}
