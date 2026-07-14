package com.islandbridge.listeners;

import com.islandbridge.IslandBridgeAmongUs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class MeetingListener implements Listener {
    private final IslandBridgeAmongUs plugin;

    public MeetingListener(IslandBridgeAmongUs plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getView().getTitle().equals(ChatColor.DARK_PURPLE + "Vote to Eject")) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null) return;
            if (clicked.getType().toString().contains("PLAYER_HEAD") && clicked.getItemMeta() instanceof SkullMeta meta && meta.getOwningPlayer() != null) {
                Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
                if (target != null) plugin.getVotingSystem().castVote(p, target);
            } else if (clicked.getType() == Material.BARRIER) {
                plugin.getVotingSystem().castSkip(p);
            }
        }
    }
}