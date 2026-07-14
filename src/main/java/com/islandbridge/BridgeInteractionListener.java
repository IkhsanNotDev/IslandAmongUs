package com.islandbridge;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class BridgeInteractionListener implements Listener {

    private final IslandBridgeAmongUs plugin;
    private final BridgeManager bridgeManager;

    public BridgeInteractionListener(IslandBridgeAmongUs plugin, BridgeManager bridgeManager) {
        this.plugin = plugin;
        this.bridgeManager = bridgeManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        Entity target = event.getEntity();
        if (!target.hasMetadata("bridgeNPC")) return;

        event.setCancelled(true);
        bridgeManager.depositPlanks(player);
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        Entity target = event.getRightClicked();
        if (!target.hasMetadata("bridgeNPC")) return;

        event.setCancelled(true);
        bridgeManager.depositPlanks(event.getPlayer());
    }
}