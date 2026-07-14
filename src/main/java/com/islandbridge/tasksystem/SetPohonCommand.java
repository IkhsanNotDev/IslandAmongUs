package com.islandbridge.tasksystem;

import com.islandbridge.IslandBridgeAmongUs;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetPohonCommand implements CommandExecutor {
    private final IslandBridgeAmongUs plugin;
    public SetPohonCommand(IslandBridgeAmongUs plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        Location base = player.getLocation().getBlock().getLocation();
        for (int i = 0; i < 3; i++) {
            Location loc = base.clone().add(0, i, 0);
            loc.getBlock().setType(Material.OAK_LOG, true);
            plugin.getTaskManager().getCustomTrees().add(loc);
        }
        plugin.getTaskManager().saveData();
        player.sendMessage("§a[Sukses] Tiang Pohon Oak Log (3 block ke atas) berhasil di-spawn dan didaftarkan!");
        return true;
    }
}