package com.islandbridge.tasksystem;

import com.islandbridge.IslandBridgeAmongUs;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetChestCommand implements CommandExecutor {
    private final IslandBridgeAmongUs plugin;
    public SetChestCommand(IslandBridgeAmongUs plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        Location loc = player.getLocation().getBlock().getLocation();
        Block block = loc.getBlock();
        block.setType(Material.CHEST, true);
        if (block.getState() instanceof Chest chest) {
            plugin.getTaskManager().getTaskChests().add(chest.getLocation());
            plugin.getTaskManager().saveData();
            player.sendMessage("§a[Sukses] Task Chest berhasil diletakkan tepat di posisimu berdiri!");
        }
        return true;
    }
}