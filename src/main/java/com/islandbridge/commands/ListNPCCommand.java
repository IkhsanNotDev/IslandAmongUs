package com.islandbridge.commands;

import com.islandbridge.IslandBridgeAmongUs;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ListNPCCommand implements CommandExecutor {
    private final IslandBridgeAmongUs plugin;

    public ListNPCCommand(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Hanya player yang bisa menggunakan command ini!");
            return true;
        }
        List<String> npcs = plugin.getTaskManager().listNPCs();
        if (npcs.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Belum ada NPC Task yang terdaftar.");
        } else {
            sender.sendMessage(ChatColor.GOLD + "=== Daftar NPC Task ===");
            for (String npc : npcs) {
                sender.sendMessage(ChatColor.AQUA + "- " + npc);
            }
        }
        return true;
    }
}