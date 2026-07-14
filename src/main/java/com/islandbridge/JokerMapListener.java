// ============================================================
// FILE: JokerMapListener.java (FIXED - perbaiki operator !)
// ============================================================
package com.islandbridge;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class JokerMapListener implements Listener {
    private final IslandBridgeAmongUs plugin;

    public JokerMapListener(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMapUse(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        // Hanya tangani klik kanan
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return;

        // Cek apakah item adalah "Peta Mayat" milik Joker
        String expectedName = ChatColor.GOLD + "Peta Mayat";
        if (!expectedName.equals(meta.getDisplayName())) return;

        // Pastikan hanya Joker yang bisa menggunakan
        if (plugin.getGameManager().getRole(p) != RoleRegistry.Role.JOKER) {
            p.sendMessage(ChatColor.RED + "Item ini hanya untuk Joker!");
            return;
        }
        if (plugin.getGameManager().getState() != GameManager.GameState.GAME_RUNNING) {
            p.sendMessage(ChatColor.RED + "Map hanya bisa digunakan saat game berjalan!");
            return;
        }

        // Batalkan event agar tidak membuka map GUI
        e.setCancelled(true);

        // Ambil daftar mayat
        List<String> corpseList = plugin.getCorpseGlowManager().getCorpseLocations();
        if (corpseList.isEmpty()) {
            p.sendMessage(ChatColor.RED + "Belum ada mayat yang terdeteksi.");
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Tampilkan daftar
        p.sendMessage(ChatColor.LIGHT_PURPLE + "===== DAFTAR MAYAT =====");
        int i = 1;
        for (String loc : corpseList) {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "" + i + ". " + ChatColor.WHITE + loc);
            i++;
        }
        p.sendMessage(ChatColor.GRAY + "Total mayat: " + (i - 1));
        p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.0f);
    }
}