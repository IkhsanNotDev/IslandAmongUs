package com.islandbridge.commands;

import com.islandbridge.IslandBridgeAmongUs;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkinCommand implements CommandExecutor {
    private final IslandBridgeAmongUs plugin;

    public SkinCommand(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (args.length < 1) {
            p.sendMessage(ChatColor.RED + "Gunakan: /skin <namaPremium>");
            return true;
        }
        String targetSkin = args[0];
        plugin.getSkinWalker().setSkin(p, targetSkin);
        p.sendMessage(ChatColor.GREEN + "Skin disimpan sebagai " + targetSkin + " (akan dipulihkan setelah sabotase skin walker)");
        return true;
    }
}