package com.islandbridge;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manager untuk Fake Task (Impostor & Joker)
 * - Tidak ada buku / GUI
 * - Interaksi otomatis dengan NPC, pohon, mancing, chest, farm
 * - Progress disimpan per player
 * - Reward = 4 Oak Planks per fake task
 */
public class FakeTaskBook {

    private final IslandBridgeAmongUs plugin;
    private final Map<UUID, Integer> progressMap = new HashMap<>();
    private final int MAX_PROGRESS = 20; // Bisa diambil dari config nanti
    private final int FAKE_REWARD_AMOUNT = 4; // 4 Oak Planks per fake task

    public FakeTaskBook(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
    }

    /**
     * Dipanggil oleh TaskListener saat Impostor/Joker melakukan aksi task.
     *
     * @param player   pemain yang melakukan fake task
     * @param taskType jenis task (NPC, TREE, FISHING, CHEST, FARMING)
     */
    public void completeFakeTask(Player player, FakeTaskType taskType) {
        // Validasi role dan game state
        if (!isAllowed(player)) {
            player.sendMessage(ChatColor.RED + "Kamu tidak bisa melakukan fake task!");
            return;
        }
        if (plugin.getGameManager().getState() != GameManager.GameState.GAME_RUNNING) {
            player.sendMessage(ChatColor.RED + "Game tidak sedang berjalan!");
            return;
        }

        UUID uuid = player.getUniqueId();

        // Tambah progress
        int current = progressMap.getOrDefault(uuid, 0);
        current++;
        progressMap.put(uuid, current);

        // Beri reward (4 Oak Planks)
        giveFakeReward(player);

        // Feedback
        player.sendMessage(ChatColor.GREEN + "Fake task selesai! (" + current + "/" + MAX_PROGRESS + ")");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        // Cek apakah sudah mencapai target maksimum
        if (current >= MAX_PROGRESS) {
            finishFakeTasks(player);
        }
    }

    /**
     * Reset progress pemain (misal saat game berakhir)
     */
    public void resetProgress(Player player) {
        progressMap.remove(player.getUniqueId());
    }

    /**
     * Reset semua progress
     */
    public void resetAll() {
        progressMap.clear();
    }

    /**
     * Mendapatkan progress pemain saat ini
     */
    public int getProgress(Player player) {
        return progressMap.getOrDefault(player.getUniqueId(), 0);
    }

    // -------- Internal Helpers --------

    private boolean isAllowed(Player player) {
        RoleRegistry.Role role = plugin.getGameManager().getRole(player);
        return role == RoleRegistry.Role.IMPOSTOR || role == RoleRegistry.Role.JOKER;
    }

    /**
     * Memberikan 4 Oak Planks per fake task.
     */
    private void giveFakeReward(Player player) {
        ItemStack reward = new ItemStack(Material.OAK_PLANKS, FAKE_REWARD_AMOUNT);
        var leftover = player.getInventory().addItem(reward);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), reward);
            player.sendMessage(ChatColor.YELLOW + "Inventory penuh! Oak Planks dijatuhkan.");
        }
    }

    private void finishFakeTasks(Player player) {
        player.sendMessage(ChatColor.GOLD + "Selamat! Kamu telah menyelesaikan semua fake task!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        resetProgress(player);
    }

    // -------- Enum untuk tipe task --------

    public enum FakeTaskType {
        NPC,
        TREE,
        FISHING,
        CHEST,
        FARMING
    }
}