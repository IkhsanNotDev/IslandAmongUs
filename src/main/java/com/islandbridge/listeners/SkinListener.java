package com.islandbridge.listeners;

import com.islandbridge.IslandBridgeAmongUs;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SkinListener implements Listener {
    private final IslandBridgeAmongUs plugin;
    public SkinListener(IslandBridgeAmongUs plugin) { this.plugin = plugin; }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Skin sudah diatur saat game dimulai, tidak perlu melakukan apapun di sini
    }
}