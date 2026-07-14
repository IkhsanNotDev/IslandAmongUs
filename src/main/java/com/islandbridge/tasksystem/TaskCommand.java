package com.islandbridge.tasksystem;

import com.islandbridge.IslandBridgeAmongUs;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TaskCommand implements CommandExecutor {
    private final IslandBridgeAmongUs plugin;
    public TaskCommand(IslandBridgeAmongUs plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cGunakan: /task <player>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cPlayer tidak online!");
            return true;
        }
        plugin.getTaskManager().assignTask(target);
        sender.sendMessage("§aTask acak diberikan ke " + target.getName());
        return true;
    }
}