package com.islandbridge.tasksystem;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.block.BlockBreakEvent;

public class WeaponListener implements Listener {
    private boolean isAxe(Material mat) { return mat.name().endsWith("_AXE"); }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager && isAxe(damager.getInventory().getItemInMainHand().getType())) {
            event.setCancelled(true);
            damager.sendMessage("§cAxe ini tumpul untuk bertarung! Hanya bisa untuk memotong Oak Log.");
        }
    }

    @EventHandler
    public void onAxeBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material hand = player.getInventory().getItemInMainHand().getType();
        Block block = event.getBlock();
        if (isAxe(hand) && block.getType() != Material.OAK_LOG) {
            event.setCancelled(true);
            player.sendMessage("§cAxe ini dikunci untuk dunia luar! Hanya dapat menghancurkan Oak Log Task.");
        }
    }
}