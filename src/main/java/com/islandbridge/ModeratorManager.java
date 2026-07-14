package com.islandbridge;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ModeratorManager {
    private final IslandBridgeAmongUs plugin;

    public ModeratorManager(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
    }

    public void setSpectator(Player p) {
        p.setGameMode(GameMode.SPECTATOR);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (p.isOnline()) p.setGameMode(GameMode.ADVENTURE);
            }
        }.runTaskLater(plugin, 200L);
    }
}