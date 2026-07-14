package com.islandbridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

public class SabotageGUIListener implements Listener {
    private final IslandBridgeAmongUs plugin;

    public SabotageGUIListener(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
    }

    public void openSabotageGUI(Player impostor) {
        if (plugin.getGameManager().getState() != GameManager.GameState.GAME_RUNNING) return;
        if (plugin.getGameManager().getRole(impostor) != RoleRegistry.Role.IMPOSTOR) return;

        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_RED + "Pilih Sabotase");
        ItemStack lamp = new ItemStack(Material.REDSTONE_LAMP);
        lamp.editMeta(meta -> {
            meta.setDisplayName(ChatColor.RED + "Matikan Lampu");
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Semua lampu padam selama 30 detik"));
        });
        gui.setItem(2, lamp);

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Skin Walker");
        skullMeta.setLore(Arrays.asList(ChatColor.GRAY + "Ubah skin semua player menjadi default dan sembunyikan nametag selama 60 detik"));
        skull.setItemMeta(skullMeta);
        gui.setItem(6, skull);

        impostor.openInventory(gui);
        impostor.sendMessage(ChatColor.GREEN + "GUI sabotase dibuka (Lampu & Skin Walker)");
        impostor.playSound(impostor.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!e.getView().getTitle().equals(ChatColor.DARK_RED + "Pilih Sabotase")) return;
        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null) return;
        if (clicked.getType() == Material.REDSTONE_LAMP) {
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 1.0f, 0.8f);
            plugin.getSabotageManager().sabotageLights(p);
        } else if (clicked.getType() == Material.PLAYER_HEAD && clicked.getItemMeta().getDisplayName().contains("Skin Walker")) {
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SKELETON_DEATH, 1.0f, 0.6f);
            plugin.getSabotageManager().sabotageSkinWalker(p);
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Sabotase Skin Walker dimulai!");
        }
    }
}