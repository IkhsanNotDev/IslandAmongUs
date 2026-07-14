// ============================================================
// FILE: HeartbeatRadar.java (KOSONG - compass tidak digunakan)
// ============================================================
package com.islandbridge;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class HeartbeatRadar implements Listener {
    private final IslandBridgeAmongUs plugin;

    public HeartbeatRadar(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent e) {
        // COMPASS TIDAK BERFUNGSI - JOKER PAKAI MAP
        // Tidak ada aksi apapun
    }
}