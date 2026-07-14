package com.islandbridge.commands;

import com.islandbridge.IslandBridgeAmongUs;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteNPCCommand implements CommandExecutor {
    private final IslandBridgeAmongUs plugin;

    public DeleteNPCCommand(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Hanya player yang bisa menggunakan command ini!");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Gunakan: /delnpc <namaNPC>");
            return true;
        }
        String name = args[0];
        plugin.getTaskManager().deleteNPC(name);
        sender.sendMessage(ChatColor.GREEN + "NPC dengan nama " + name + " telah dihapus (jika ada).");
        return true;
    }
}