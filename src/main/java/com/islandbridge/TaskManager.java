package com.islandbridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class TaskManager {

    private final IslandBridgeAmongUs plugin;

    private final Set<Location> customTrees = new HashSet<>();
    private final List<Location> taskChests = new ArrayList<>();

    private final Map<UUID, Integer> woodCount = new HashMap<>();
    private final Map<UUID, Integer> fishRequired = new HashMap<>();
    private final Map<UUID, Integer> fishCaught = new HashMap<>();
    private final Map<UUID, String> activeTaskType = new HashMap<>();
    private final Set<UUID> hasActiveTask = new HashSet<>();
    private final Set<UUID> isInteractingWithNPC = new HashSet<>();

    private final Set<Location> activeTasks = new HashSet<>();

    public TaskManager(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
    }

    // ========== Main Task Methods ==========
    public void startRandomTask(Player player) {
        UUID uuid = player.getUniqueId();
        if (hasActiveTask.contains(uuid)) return;
        hasActiveTask.add(uuid);
        String[] types = {"TEBANG", "MANCING"};
        String chosenTask = types[new Random().nextInt(types.length)];
        activeTaskType.put(uuid, chosenTask);
        if (chosenTask.equals("TEBANG")) {
            woodCount.put(uuid, 0);
            player.sendMessage("§6§l[Task] §eTask didapatkan: §fTebang 2 Oak Log di area tiang 3 block!");
        } else {
            fishCaught.put(uuid, 0);
            int randomFish = new Random().nextInt(6) + 3;
            fishRequired.put(uuid, randomFish);
            player.sendMessage("§6§l[Task] §eTask didapatkan: §fPancing " + randomFish + " ekor ikan biasa (Cod/Salmon)!");
        }
    }

    public void completeTask(Player player) {
        UUID uuid = player.getUniqueId();
        if (!hasActiveTask.contains(uuid)) return;
        hasActiveTask.remove(uuid);
        woodCount.remove(uuid);
        fishRequired.remove(uuid);
        fishCaught.remove(uuid);
        activeTaskType.remove(uuid);
        ItemStack reward = new ItemStack(Material.OAK_PLANKS, 1);
        var leftover = player.getInventory().addItem(reward);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), reward);
            player.sendMessage("§e[Task] §cInventory penuh! Oak Planks dijatuhkan di dekatmu.");
        }
        player.updateInventory();
        player.sendMessage("§a§l[Task] Sukses! Seluruh target Task selesai, 1 Oak Planks dimasukkan!");
    }

    public ItemStack getToolForTask(Player player) {
        String type = getTaskType(player);
        if (type.equals("TEBANG")) {
            Material[] axes = {Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE};
            return new ItemStack(axes[new Random().nextInt(axes.length)]);
        } else if (type.equals("MANCING")) {
            return new ItemStack(Material.FISHING_ROD);
        }
        return new ItemStack(Material.BREAD);
    }

    public String getNPCKey(Location loc) {
        return "npc_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    public void loadData() {
        // Load custom trees
        if (plugin.getConfig().contains("custom-trees")) {
            List<?> trees = plugin.getConfig().getList("custom-trees");
            if (trees != null) {
                for (Object loc : trees) customTrees.add((Location) loc);
            }
        }
        // Load task chests
        if (plugin.getConfig().contains("task-chests")) {
            List<?> chests = plugin.getConfig().getList("task-chests");
            if (chests != null) {
                for (Object loc : chests) taskChests.add((Location) loc);
            }
        }
        // Load registered NPCs
        if (plugin.getConfig().contains("registered-npcs")) {
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("registered-npcs");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    Location loc = (Location) section.get(key + ".location");
                    String name = section.getString(key + ".name");
                    long duration = section.getLong(key + ".duration");
                    if (loc != null && loc.getWorld() != null) {
                        boolean found = false;
                        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                            if (entity instanceof ArmorStand stand) {
                                stand.setMetadata("TaskNPC", new FixedMetadataValue(plugin, true));
                                stand.setMetadata("NPCDurasi", new FixedMetadataValue(plugin, duration));
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            ArmorStand newNpc = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                            newNpc.setCustomName(name);
                            newNpc.setCustomNameVisible(true);
                            newNpc.setArms(true);
                            newNpc.setBasePlate(false);
                            newNpc.setGravity(true);
                            newNpc.setMetadata("TaskNPC", new FixedMetadataValue(plugin, true));
                            newNpc.setMetadata("NPCDurasi", new FixedMetadataValue(plugin, duration));
                        }
                    }
                }
            }
        }
    }

    public void saveData() {
        plugin.getConfig().set("custom-trees", new ArrayList<>(customTrees));
        plugin.getConfig().set("task-chests", taskChests);
        plugin.saveConfig();
    }

    public void resetAllPlayerTasks() {
        hasActiveTask.clear();
        woodCount.clear();
        fishRequired.clear();
        fishCaught.clear();
        activeTaskType.clear();
        isInteractingWithNPC.clear();
    }

    // Untuk kompatibilitas dengan GameManager
    public void initializeTasksForGame() {
        resetAllPlayerTasks();
        activeTasks.clear();
    }

    public void resetTasks() {
        resetAllPlayerTasks();
        activeTasks.clear();
    }

    // Getters
    public Set<Location> getCustomTrees() { return customTrees; }
    public List<Location> getTaskChests() { return taskChests; }
    public Map<UUID, Integer> getWoodCount() { return woodCount; }
    public Map<UUID, Integer> getFishRequired() { return fishRequired; }
    public Map<UUID, Integer> getFishCaught() { return fishCaught; }
    public Set<UUID> getIsInteractingWithNPC() { return isInteractingWithNPC; }
    public boolean hasTask(Player player) { return hasActiveTask.contains(player.getUniqueId()); }
    public String getTaskType(Player player) { return activeTaskType.getOrDefault(player.getUniqueId(), "NONE"); }
}