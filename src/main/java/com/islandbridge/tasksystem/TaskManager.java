package com.islandbridge.tasksystem;

import com.islandbridge.GameManager;
import com.islandbridge.IslandBridgeAmongUs;
import com.islandbridge.RoleRegistry;
import com.islandbridge.utils.ArmorUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskManager implements Listener {

    private final IslandBridgeAmongUs plugin;

    private final Set<Location> customTrees = new HashSet<>();
    private final List<Location> taskChests = new ArrayList<>();
    private final Set<Location> farmlands = new HashSet<>();
    private final Set<Location> swimWaterLocations = new HashSet<>();

    private final Map<UUID, Integer> woodCount = new HashMap<>();
    private final Map<UUID, Integer> fishRequired = new HashMap<>();
    private final Map<UUID, Integer> fishCaught = new HashMap<>();
    private final Map<UUID, String> activeTaskType = new HashMap<>();
    private final Set<UUID> hasActiveTask = new HashSet<>();
    private final Set<UUID> isInteractingWithNPC = new HashSet<>();
    private final Map<UUID, Boolean> isTaskNPC = new HashMap<>();
    private final Map<UUID, String> targetNPCName = new HashMap<>();

    // Task SWIM
    private final Map<UUID, Integer> swimTimeRequired = new HashMap<>();
    private final Map<UUID, Integer> swimTimeElapsed = new HashMap<>();
    private final Map<UUID, BukkitRunnable> swimTasks = new HashMap<>();

    // Task FARM
    private final Map<UUID, Integer> farmHarvestRequired = new HashMap<>();
    private final Map<UUID, Integer> farmHarvestCount = new HashMap<>();
    private final Map<UUID, Material> farmCropType = new HashMap<>();

    private List<String> npcDisplayNames = new ArrayList<>();
    private final Random random = new Random();

    private final Map<UUID, Location> npcSpawnLocations = new HashMap<>();
    private BukkitRunnable antiPushTask;
    private BukkitRunnable farmMaintenanceTask;

    public TaskManager(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
        loadNPCDisplayNames();
        startAntiPushTask();
        loadData();
        plantCropsOnAllFarmlands();
        startFarmMaintenance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // ======================= FARMLAND MAINTENANCE =======================

    public void plantCropsOnAllFarmlands() {
        Material[] crops = {Material.CARROTS, Material.POTATOES, Material.WHEAT, Material.BEETROOTS};
        int planted = 0;
        for (Location loc : farmlands) {
            Location above = loc.clone().add(0, 1, 0);
            if (above.getBlock().getType() == Material.AIR) {
                Material cropType = crops[random.nextInt(crops.length)];
                above.getBlock().setType(cropType);
                Ageable ageable = (Ageable) above.getBlock().getBlockData();
                ageable.setAge(ageable.getMaximumAge());
                above.getBlock().setBlockData(ageable);
                planted++;
            }
        }
        if (planted > 0) {
            plugin.getLogger().info("TaskManager: Menanam " + planted + " tanaman di farmland.");
        }
    }

    public void startFarmMaintenance() {
        if (farmMaintenanceTask != null) return;
        farmMaintenanceTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : farmlands) {
                    Location above = loc.clone().add(0, 1, 0);
                    Block cropBlock = above.getBlock();
                    if (cropBlock.getType() == Material.AIR) {
                        Material[] crops = {Material.CARROTS, Material.POTATOES, Material.WHEAT, Material.BEETROOTS};
                        Material cropType = crops[random.nextInt(crops.length)];
                        cropBlock.setType(cropType);
                        Ageable ageable = (Ageable) cropBlock.getBlockData();
                        ageable.setAge(ageable.getMaximumAge());
                        cropBlock.setBlockData(ageable);
                    } else {
                        BlockData data = cropBlock.getBlockData();
                        if (data instanceof Ageable ageable) {
                            ageable.setAge(ageable.getMaximumAge());
                            cropBlock.setBlockData(ageable);
                        }
                    }
                }
            }
        };
        farmMaintenanceTask.runTaskTimer(plugin, 20L, 20L);
    }

    private void stopFarmMaintenance() {
        if (farmMaintenanceTask != null) {
            farmMaintenanceTask.cancel();
            farmMaintenanceTask = null;
        }
    }

    // ======================= EVENT CEGAH TRAMPLE & FADE =======================

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (e.getBlock().getType() == Material.FARMLAND) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent e) {
        if (e.getBlock().getType() == Material.FARMLAND) {
            e.setCancelled(true);
        }
    }

    // ======================= AUTO DETECT OAK LOGS =======================
    public void autoDetectOakLogs(Player player, int radius) {
        int maxRadius = plugin.getConfig().getInt("task.max-autodetect-radius", 200);
        if (radius > maxRadius) {
            player.sendMessage(ChatColor.RED + "Radius terlalu besar! Maksimal " + maxRadius + " block.");
            player.sendMessage(ChatColor.YELLOW + "Kamu bisa mengubah batas di config.yml: task.max-autodetect-radius");
            return;
        }
        if (radius <= 0) {
            player.sendMessage(ChatColor.RED + "Radius harus lebih besar dari 0.");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Memindai pohon oak dalam radius " + radius + "... (proses background)");

        final int finalRadius = radius;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = player.getWorld();
            Location center = player.getLocation();
            int minX = center.getBlockX() - finalRadius;
            int maxX = center.getBlockX() + finalRadius;
            int minZ = center.getBlockZ() - finalRadius;
            int maxZ = center.getBlockZ() + finalRadius;
            int minY = world.getMinHeight();
            int maxY = world.getMaxHeight() - 1;

            int added = 0;
            int scanned = 0;
            List<Location> toAddSync = new ArrayList<>();

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                        continue;
                    }
                    for (int y = minY; y <= maxY; y++) {
                        scanned++;
                        Location check = new Location(world, x, y, z);
                        if (check.getBlock().getType() == Material.OAK_LOG) {
                            if (!customTrees.contains(check)) {
                                customTrees.add(check);
                                toAddSync.add(check);
                                added++;
                            }
                        }
                    }
                }
            }

            final int finalAdded = added;
            final int finalScanned = scanned;
            Bukkit.getScheduler().runTask(plugin, () -> {
                saveData();
                player.sendMessage(ChatColor.GREEN + "Auto-detect pohon oak selesai! Ditemukan " + finalAdded + " pohon dalam radius " + finalRadius + " block.");
                player.sendMessage(ChatColor.GRAY + "Memindai " + finalScanned + " posisi blok.");
                if (finalAdded == 0) {
                    player.sendMessage(ChatColor.YELLOW + "Tips: Pastikan ada OAK_LOG (kayu oak) di area yang sudah di-load (chunk loaded).");
                    player.sendMessage(ChatColor.YELLOW + "Kamu bisa menjelajah area terlebih dahulu agar chunk termuat, lalu jalankan autooak lagi.");
                }
            });
        });
    }

    // ======================= AUTO DETECT CHESTS =======================
    public int autoDetectChests(Player player, int radius) {
        int maxRadius = plugin.getConfig().getInt("task.max-autodetect-radius", 200);
        if (radius > maxRadius) {
            player.sendMessage(ChatColor.RED + "Radius terlalu besar! Maksimal " + maxRadius + " block.");
            player.sendMessage(ChatColor.YELLOW + "Kamu bisa mengubah batas di config.yml: task.max-autodetect-radius");
            return 0;
        }
        if (radius <= 0) {
            player.sendMessage(ChatColor.RED + "Radius harus lebih besar dari 0.");
            return 0;
        }

        player.sendMessage(ChatColor.YELLOW + "Memindai chest dalam radius " + radius + "... (proses background)");

        final int finalRadius = radius;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = player.getWorld();
            Location center = player.getLocation();
            int baseX = center.getBlockX();
            int baseZ = center.getBlockZ();
            int minY = world.getMinHeight();
            int maxY = world.getMaxHeight() - 1;

            int totalScanned = 0;
            List<Location> newChests = new ArrayList<>();

            for (int dx = -finalRadius; dx <= finalRadius; dx++) {
                for (int dz = -finalRadius; dz <= finalRadius; dz++) {
                    int checkX = baseX + dx;
                    int checkZ = baseZ + dz;

                    if (!world.isChunkLoaded(checkX >> 4, checkZ >> 4)) {
                        continue;
                    }

                    for (int y = minY; y <= maxY; y++) {
                        totalScanned++;
                        Location loc = new Location(world, checkX, y, checkZ);
                        Material mat = loc.getBlock().getType();

                        if (mat == Material.CHEST || mat == Material.TRAPPED_CHEST) {
                            Location chestLoc = loc.getBlock().getLocation();
                            if (!newChests.contains(chestLoc)) {
                                newChests.add(chestLoc);
                            }
                        }
                    }
                }
            }

            final int finalFound = newChests.size();
            final int finalScanned = totalScanned;

            Bukkit.getScheduler().runTask(plugin, () -> {
                int actuallyAdded = 0;
                for (Location loc : newChests) {
                    if (!taskChests.contains(loc)) {
                        taskChests.add(loc);
                        actuallyAdded++;
                    }
                }

                if (actuallyAdded > 0) {
                    saveData();
                    player.sendMessage(ChatColor.GREEN + "Berhasil menambahkan " + actuallyAdded + " chest baru ke daftar task chest.");
                    player.sendMessage(ChatColor.GRAY + "Total chest terdaftar sekarang: " + taskChests.size());
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Tidak ditemukan chest baru dalam radius " + finalRadius + " block.");
                    player.sendMessage(ChatColor.GRAY + "Total chest terdaftar sekarang: " + taskChests.size());
                }
                player.sendMessage(ChatColor.GRAY + "Memindai " + finalScanned + " posisi blok.");
            });
        });

        return 0;
    }

    public void addTaskChest(Location loc) {
        if (!taskChests.contains(loc)) {
            taskChests.add(loc);
            saveData();
        }
    }

    // ======================= AUTO DETECT FARMLAND + AIR =======================
    public void autoDetectFarmland(Player player, int radius) {
        int maxRadius = plugin.getConfig().getInt("task.max-autodetect-radius", 200);
        if (radius > maxRadius) {
            player.sendMessage(ChatColor.RED + "Radius terlalu besar! Maksimal " + maxRadius + " block.");
            player.sendMessage(ChatColor.YELLOW + "Kamu bisa mengubah batas di config.yml: task.max-autodetect-radius");
            return;
        }
        if (radius <= 0) {
            player.sendMessage(ChatColor.RED + "Radius harus > 0.");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Memindai farmland dan air dalam radius " + radius + "...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = player.getWorld();
            Location center = player.getLocation();
            int cx = center.getBlockX(), cz = center.getBlockZ();
            int addedFarmland = 0;
            int addedWater = 0;
            List<Location> newFarmlands = new ArrayList<>();
            List<Location> newWater = new ArrayList<>();

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int x = cx + dx, z = cz + dz;
                    if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
                    for (int y = world.getMinHeight(); y <= world.getMaxHeight(); y++) {
                        Location loc = new Location(world, x, y, z);
                        Material mat = loc.getBlock().getType();
                        if (mat == Material.FARMLAND) {
                            if (!farmlands.contains(loc)) {
                                farmlands.add(loc);
                                newFarmlands.add(loc);
                                addedFarmland++;
                            }
                        } else if (mat == Material.WATER || mat == Material.BUBBLE_COLUMN) {
                            if (!swimWaterLocations.contains(loc)) {
                                swimWaterLocations.add(loc);
                                newWater.add(loc);
                                addedWater++;
                            }
                        }
                    }
                }
            }

            final int finalAddedFarmland = addedFarmland;
            final int finalAddedWater = addedWater;
            final List<Location> newFarmLocs = new ArrayList<>(newFarmlands);
            Bukkit.getScheduler().runTask(plugin, () -> {
                saveData();
                player.sendMessage(ChatColor.GREEN + "Ditambahkan " + finalAddedFarmland + " farmland dan " + finalAddedWater + " air baru.");
                player.sendMessage(ChatColor.GRAY + "Total farmland: " + farmlands.size() + ", air: " + swimWaterLocations.size());

                Material[] crops = {Material.CARROTS, Material.POTATOES, Material.WHEAT, Material.BEETROOTS};
                int planted = 0;
                for (Location loc : newFarmLocs) {
                    Location above = loc.clone().add(0, 1, 0);
                    if (above.getBlock().getType() == Material.AIR) {
                        Material cropType = crops[random.nextInt(crops.length)];
                        above.getBlock().setType(cropType);
                        Ageable ageable = (Ageable) above.getBlock().getBlockData();
                        ageable.setAge(ageable.getMaximumAge());
                        above.getBlock().setBlockData(ageable);
                        planted++;
                    }
                }
                if (planted > 0) {
                    player.sendMessage(ChatColor.GREEN + "✅ " + planted + " tanaman telah ditanam di lahan baru!");
                }
            });
        });
    }

    public boolean isSwimWater(Location loc) {
        return swimWaterLocations.contains(loc);
    }

    // ======================= NPC METHODS =======================
    public void clearAllNPCs() {
        for (World world : Bukkit.getWorlds()) {
            List<Entity> toRemove = new ArrayList<>();
            for (Entity e : world.getEntities()) {
                if (e.hasMetadata("TaskNPC")) {
                    toRemove.add(e);
                }
            }
            for (Entity e : toRemove) {
                npcSpawnLocations.remove(e.getUniqueId());
                e.remove();
            }
        }
    }

    public void reloadAllNPCs() {
        clearAllNPCs();
        loadData();
        plantCropsOnAllFarmlands();
    }

    public void cancelAllActiveTasks() {
        for (UUID uuid : isInteractingWithNPC) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(ChatColor.RED + "Game berakhir! Task-mu dibatalkan.");
            }
        }
        isInteractingWithNPC.clear();
    }

    public void loadNPCDisplayNames() {
        npcDisplayNames.clear();
        ConfigurationSection npcs = plugin.getConfig().getConfigurationSection("task-npcs");
        if (npcs != null) {
            List<String> keys = new ArrayList<>(npcs.getKeys(false));
            for (String key : keys) {
                String name = npcs.getString(key + ".name");
                if (name != null) {
                    String cleanName = ChatColor.stripColor(name).trim().replace('_', ' ');
                    npcDisplayNames.add(cleanName);
                }
            }
        }
    }

    public String getRandomNPCName() {
        if (npcDisplayNames.isEmpty()) return null;
        return npcDisplayNames.get(random.nextInt(npcDisplayNames.size()));
    }

    public void startRandomTask(Player player) {
        UUID uuid = player.getUniqueId();
        if (hasActiveTask.contains(uuid)) return;
        hasActiveTask.add(uuid);
        isTaskNPC.put(uuid, false);

        String[] types = {"TEBANG", "MANCING", "SWIM", "FARM"};
        String chosenTask = types[random.nextInt(types.length)];
        activeTaskType.put(uuid, chosenTask);

        switch (chosenTask) {
            case "TEBANG":
                woodCount.put(uuid, 0);
                player.sendMessage("§6§l[Task] §eTask didapatkan: §fTebang 2 Oak Log!");
                break;
            case "MANCING":
                fishCaught.put(uuid, 0);
                int need = random.nextInt(5) + 1;
                fishRequired.put(uuid, need);
                player.sendMessage("§6§l[Task] §eTask didapatkan: §fPancing " + need + " ekor ikan!");
                break;
            case "SWIM":
                int seconds = random.nextInt(11) + 10;
                swimTimeRequired.put(uuid, seconds);
                swimTimeElapsed.put(uuid, 0);
                player.sendMessage("§6§l[Task] §eTask didapatkan: §fBerenang di air selama " + seconds + " detik!");
                startSwimTask(player);
                break;
            case "FARM":
                int crops = random.nextInt(5) + 3;
                farmHarvestRequired.put(uuid, crops);
                farmHarvestCount.put(uuid, 0);
                Material[] cropsList = {Material.CARROTS, Material.POTATOES, Material.WHEAT, Material.BEETROOTS};
                Material chosenCrop = cropsList[random.nextInt(cropsList.length)];
                farmCropType.put(uuid, chosenCrop);
                player.sendMessage("§6§l[Task] §eTask didapatkan: §fPanen " + crops + " " + chosenCrop.name().toLowerCase() + " dari ladang!");
                break;
        }
        plugin.getScoreboardManager().updateScoreboard(player);
    }

    // === SWIM TASK SCHEDULER ===
    public void startSwimTask(Player player) {
        UUID uuid = player.getUniqueId();
        cancelSwimTask(player);
        int required = swimTimeRequired.getOrDefault(uuid, 0);
        if (required <= 0) return;
        swimTimeElapsed.put(uuid, 0);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || plugin.getGameManager().getState() != GameManager.GameState.GAME_RUNNING) {
                    cancelSwimTask(player);
                    return;
                }
                if (!isSwimTask(player)) {
                    cancelSwimTask(player);
                    return;
                }
                // Deteksi air di block tempat kaki player berdiri
                Material blockMat = player.getLocation().getBlock().getType();
                boolean inWater = blockMat == Material.WATER || blockMat == Material.BUBBLE_COLUMN;
                if (inWater) {
                    int elapsed = swimTimeElapsed.getOrDefault(uuid, 0) + 1;
                    swimTimeElapsed.put(uuid, elapsed);
                    player.sendActionBar(ChatColor.AQUA + "Berenang: " + elapsed + "/" + required + " detik");
                    if (elapsed >= required) {
                        completeTask(player);
                        cancelSwimTask(player);
                    }
                } else {
                    int current = swimTimeElapsed.getOrDefault(uuid, 0);
                    if (current > 0) {
                        swimTimeElapsed.put(uuid, 0);
                        player.sendMessage("§cKamu keluar dari air! Timer direset.");
                        player.sendActionBar("");
                    }
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
        swimTasks.put(uuid, task);
    }

    public void cancelSwimTask(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitRunnable task = swimTasks.remove(uuid);
        if (task != null) task.cancel();
        swimTimeElapsed.remove(uuid);
        swimTimeRequired.remove(uuid);
    }

    // === CLEAR FISH FROM INVENTORY ===
    public void clearFishFromInventory(Player player) {
        Material[] fishTypes = {Material.COD, Material.SALMON, Material.PUFFERFISH, Material.TROPICAL_FISH};
        for (Material fish : fishTypes) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == fish) {
                    item.setAmount(0);
                }
            }
        }
        player.updateInventory();
    }

    // === CANCEL ALL TASKS FOR ALL PLAYERS (saat meeting) ===
    public void cancelAllTasksForAllPlayers() {
        for (UUID uuid : hasActiveTask) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                cancelSwimTask(p);
                String type = activeTaskType.get(uuid);
                if (type != null && !type.equals("SWIM") && !type.equals("NPC")) {
                    removeTaskTool(p, type);
                }
                if ("MANCING".equals(type)) {
                    clearFishFromInventory(p);
                }
                hasActiveTask.remove(uuid);
                woodCount.remove(uuid);
                fishRequired.remove(uuid);
                fishCaught.remove(uuid);
                activeTaskType.remove(uuid);
                isTaskNPC.remove(uuid);
                targetNPCName.remove(uuid);
                farmHarvestRequired.remove(uuid);
                farmHarvestCount.remove(uuid);
                farmCropType.remove(uuid);
                swimTimeRequired.remove(uuid);
                swimTimeElapsed.remove(uuid);
                p.sendMessage(ChatColor.RED + "Task-mu dibatalkan karena meeting!");
            }
        }
        hasActiveTask.clear();
        woodCount.clear();
        fishRequired.clear();
        fishCaught.clear();
        activeTaskType.clear();
        isTaskNPC.clear();
        targetNPCName.clear();
        farmHarvestRequired.clear();
        farmHarvestCount.clear();
        farmCropType.clear();
        swimTimeRequired.clear();
        swimTimeElapsed.clear();
        isInteractingWithNPC.clear();
    }

    // === START NPC TASK ===
    public void startNPCTask(Player player) {
        UUID uuid = player.getUniqueId();
        if (hasActiveTask.contains(uuid)) return;
        loadNPCDisplayNames();
        String target = getRandomNPCName();
        if (target == null) {
            player.sendMessage("§cBelum ada NPC Task yang terdaftar! Hubungi admin.");
            return;
        }
        plugin.getLogger().info("[TaskManager] Player " + player.getName() + " mendapat task NPC: " + target);

        targetNPCName.put(uuid, target);
        hasActiveTask.add(uuid);
        isTaskNPC.put(uuid, true);
        activeTaskType.put(uuid, "NPC");
        player.sendMessage("§6§l[Task] §eTask didapatkan: §fCari NPC dengan nama " + target + " dan interaksi!");
        plugin.getScoreboardManager().updateScoreboard(player);
    }

    // === COMPLETE TASK WITH REWARD (untuk Crewmate) ===
    public void completeTask(Player player) {
        UUID uuid = player.getUniqueId();
        if (!hasActiveTask.contains(uuid)) return;
        boolean wasNPC = isTaskNPC.getOrDefault(uuid, false);
        String type = activeTaskType.get(uuid);

        // Bersihkan state
        hasActiveTask.remove(uuid);
        woodCount.remove(uuid);
        fishRequired.remove(uuid);
        fishCaught.remove(uuid);
        activeTaskType.remove(uuid);
        isTaskNPC.remove(uuid);
        targetNPCName.remove(uuid);
        swimTimeRequired.remove(uuid);
        swimTimeElapsed.remove(uuid);
        farmHarvestRequired.remove(uuid);
        farmHarvestCount.remove(uuid);
        farmCropType.remove(uuid);
        cancelSwimTask(player);

        removeTaskTool(player, type);
        if ("MANCING".equals(type)) {
            clearFishFromInventory(player);
        }

        // Reward untuk Crewmate
        int rewardAmount = random.nextInt(4) + 5;
        ItemStack reward = new ItemStack(Material.OAK_PLANKS, rewardAmount);
        var leftover = player.getInventory().addItem(reward);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), reward);
            player.sendMessage("§e[Task] §cInventory penuh! Oak Planks dijatuhkan.");
        }
        player.updateInventory();
        player.sendMessage("§a§l[Task] Sukses! +" + rewardAmount + " Oak Planks.");
        plugin.getBridgeManager().addBridgeProgressFromTask(rewardAmount);
        plugin.getScoreboardManager().updateScoreboard(player);

        // Assign task berikutnya
        if (wasNPC) {
            startRandomTask(player);
        } else {
            startNPCTask(player);
        }
    }

    // === COMPLETE TASK WITHOUT REWARD (untuk Impostor/Joker) ===
    public void completeTaskNoReward(Player player) {
        UUID uuid = player.getUniqueId();
        if (!hasActiveTask.contains(uuid)) return;
        boolean wasNPC = isTaskNPC.getOrDefault(uuid, false);
        String type = activeTaskType.get(uuid);

        // Bersihkan state
        hasActiveTask.remove(uuid);
        woodCount.remove(uuid);
        fishRequired.remove(uuid);
        fishCaught.remove(uuid);
        activeTaskType.remove(uuid);
        isTaskNPC.remove(uuid);
        targetNPCName.remove(uuid);
        swimTimeRequired.remove(uuid);
        swimTimeElapsed.remove(uuid);
        farmHarvestRequired.remove(uuid);
        farmHarvestCount.remove(uuid);
        farmCropType.remove(uuid);
        cancelSwimTask(player);

        removeTaskTool(player, type);
        if ("MANCING".equals(type)) {
            clearFishFromInventory(player);
        }

        // Tidak ada reward Oak Planks (fake reward sudah diberikan oleh FakeTaskBook)
        player.sendMessage("§a§l[Task] Task nyata selesai! (Fake reward sudah didapat)");
        plugin.getScoreboardManager().updateScoreboard(player);

        // Assign task berikutnya
        if (wasNPC) {
            startRandomTask(player);
        } else {
            startNPCTask(player);
        }
    }

    private void removeTaskTool(Player player, String taskType) {
        Material toolMat = null;
        switch (taskType) {
            case "TEBANG": toolMat = Material.WOODEN_AXE; break;
            case "MANCING": toolMat = Material.FISHING_ROD; break;
            case "FARM": toolMat = Material.WOODEN_HOE; break;
            default: return;
        }
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == toolMat) {
                if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                    List<String> lore = item.getItemMeta().getLore();
                    if (lore != null && lore.stream().anyMatch(l -> l.contains("§cRole"))) {
                        continue;
                    }
                }
                item.setAmount(0);
                break;
            }
        }
        player.updateInventory();
    }

    public void completeNPCTask(Player player) {
        if (!hasActiveTask.contains(player.getUniqueId())) return;
        if (!isTaskNPC.getOrDefault(player.getUniqueId(), false)) return;
        completeTask(player);
    }

    public boolean checkNPCName(Player player, Entity npc) {
        String expected = targetNPCName.get(player.getUniqueId());
        if (expected == null) return false;
        String actual = npc.getCustomName();
        if (actual == null) return false;
        String cleanActual = ChatColor.stripColor(actual).trim();
        String cleanExpected = ChatColor.stripColor(expected).trim();
        return cleanActual.equalsIgnoreCase(cleanExpected);
    }

    public ItemStack getToolForTask(Player player) {
        String type = getTaskType(player);
        switch (type) {
            case "TEBANG": {
                Material[] axes = {Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE};
                return new ItemStack(axes[random.nextInt(axes.length)]);
            }
            case "MANCING":
                return new ItemStack(Material.FISHING_ROD);
            case "FARM":
                return new ItemStack(Material.WOODEN_HOE);
            default:
                return new ItemStack(Material.BREAD);
        }
    }

    public String getNPCKey(Location loc) {
        return "npc_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    public void registerNPC(Location loc, String displayName, long durationSeconds) {
        String cleanName = displayName.replace('_', ' ');
        String key = getNPCKey(loc);
        plugin.getConfig().set("task-npcs." + key + ".location", loc);
        plugin.getConfig().set("task-npcs." + key + ".name", cleanName);
        plugin.getConfig().set("task-npcs." + key + ".duration", durationSeconds);
        plugin.saveConfig();
        loadNPCDisplayNames();

        spawnNPC(loc, cleanName, durationSeconds);
    }

    private void spawnNPC(Location loc, String displayName, long durationSeconds) {
        for (Entity e : loc.getWorld().getNearbyEntities(loc, 1.0, 1.0, 1.0)) {
            if (e.hasMetadata("TaskNPC")) {
                plugin.getLogger().info("TaskManager: NPC sudah ada di lokasi " + loc + ", skip spawn.");
                return;
            }
        }

        String formattedName = ChatColor.translateAlternateColorCodes('&', displayName);

        Entity npc = trySpawnMannequin(loc, formattedName);
        if (npc == null) {
            npc = spawnArmorStand(loc, formattedName);
        }

        if (npc != null) {
            npc.setCustomName(formattedName);
            npc.setCustomNameVisible(true);
            npc.setInvulnerable(true);
            npc.setGravity(false);
            setEntityCollidable(npc, false);
            setEntityPersistent(npc, true);
            if (npc instanceof ArmorStand) {
                ((ArmorStand) npc).setRemoveWhenFarAway(false);
            }
            npc.setMetadata("TaskNPC", new FixedMetadataValue(plugin, true));
            npc.setMetadata("NPCDurasi", new FixedMetadataValue(plugin, durationSeconds));

            npcSpawnLocations.put(npc.getUniqueId(), loc.clone());
            plugin.getLogger().info("TaskManager: NPC " + formattedName + " di-spawn di " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        }
    }

    private Entity trySpawnMannequin(Location loc, String displayName) {
        try {
            EntityType mannequinType = EntityType.valueOf("MANNEQUIN");
            if (mannequinType == null) return null;

            Entity entity = loc.getWorld().spawnEntity(loc, mannequinType);
            if (!(entity instanceof Mannequin mannequin)) {
                entity.remove();
                return null;
            }

            mannequin.setGravity(false);
            setEntityCollidable(mannequin, false);
            setEntityPersistent(mannequin, true);

            return mannequin;
        } catch (IllegalArgumentException e) {
            return null;
        } catch (Exception e) {
            plugin.getLogger().warning("TaskManager: Gagal spawn Mannequin: " + e.getMessage());
            return null;
        }
    }

    private Entity spawnArmorStand(Location loc, String displayName) {
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setCustomName(displayName);
        stand.setCustomNameVisible(true);
        stand.setArms(true);
        stand.setBasePlate(false);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        setEntityCollidable(stand, false);
        setEntityPersistent(stand, true);
        stand.setRemoveWhenFarAway(false);
        ArmorUtil.applyRandomArmor(stand);
        return stand;
    }

    private void startAntiPushTask() {
        if (antiPushTask != null) return;
        antiPushTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity e : world.getEntities()) {
                        if (e.hasMetadata("TaskNPC")) {
                            Location spawn = npcSpawnLocations.get(e.getUniqueId());
                            if (spawn != null) {
                                e.setVelocity(new Vector(0, 0, 0));
                                if (e.getLocation().distance(spawn) > 0.1) {
                                    e.teleport(spawn);
                                }
                            }
                        }
                    }
                }
            }
        };
        antiPushTask.runTaskTimer(plugin, 0L, 2L);
    }

    private void stopAntiPushTask() {
        if (antiPushTask != null) {
            antiPushTask.cancel();
            antiPushTask = null;
        }
    }

    private void setEntityCollidable(Entity entity, boolean collidable) {
        try {
            Method method = Entity.class.getMethod("setCollidable", boolean.class);
            method.invoke(entity, collidable);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            plugin.getLogger().warning("Gagal setCollidable: " + e.getMessage());
        }
    }

    private void setEntityPersistent(Entity entity, boolean persistent) {
        try {
            Method method = Entity.class.getMethod("setPersistent", boolean.class);
            method.invoke(entity, persistent);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            plugin.getLogger().warning("Gagal setPersistent: " + e.getMessage());
        }
    }

    // ======================= METODE LAINNYA =======================
    public void deleteNPC(String displayName) {
        String cleanInput = displayName.replace('_', ' ');
        ConfigurationSection npcs = plugin.getConfig().getConfigurationSection("task-npcs");
        if (npcs != null) {
            String toRemove = null;
            List<String> keys = new ArrayList<>(npcs.getKeys(false));
            for (String key : keys) {
                String name = npcs.getString(key + ".name");
                if (name != null) {
                    String cleanName = ChatColor.stripColor(name).trim().replace('_', ' ');
                    if (cleanName.equalsIgnoreCase(cleanInput)) {
                        toRemove = key;
                        break;
                    }
                }
            }
            if (toRemove != null) {
                Location loc = (Location) npcs.get(toRemove + ".location");
                if (loc != null && loc.getWorld() != null) {
                    for (Entity e : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                        if (e.hasMetadata("TaskNPC")) {
                            npcSpawnLocations.remove(e.getUniqueId());
                            e.remove();
                        }
                    }
                }
                npcs.set(toRemove, null);
                plugin.saveConfig();
                loadNPCDisplayNames();
            }
        }
    }

    public List<String> listNPCs() {
        List<String> result = new ArrayList<>();
        ConfigurationSection npcs = plugin.getConfig().getConfigurationSection("task-npcs");
        if (npcs != null) {
            List<String> keys = new ArrayList<>(npcs.getKeys(false));
            for (String key : keys) {
                String name = npcs.getString(key + ".name");
                long duration = npcs.getLong(key + ".duration");
                result.add(name + " - " + duration + " detik");
            }
        }
        return result;
    }

    public List<String> listNPCsWithCoordinates() {
        List<String> result = new ArrayList<>();
        ConfigurationSection npcs = plugin.getConfig().getConfigurationSection("task-npcs");
        if (npcs != null) {
            List<String> keys = new ArrayList<>(npcs.getKeys(false));
            for (String key : keys) {
                Location loc = (Location) npcs.get(key + ".location");
                String rawName = npcs.getString(key + ".name");
                if (rawName == null) rawName = "Unknown";
                String formattedName = ChatColor.translateAlternateColorCodes('&', rawName);
                String coord = "";
                if (loc != null) {
                    coord = " (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
                }
                result.add(ChatColor.GREEN + "• " + formattedName + ChatColor.GRAY + coord);
            }
        }
        return result;
    }

    public void loadData() {
        if (plugin.getConfig().contains("custom-trees")) {
            List<?> trees = plugin.getConfig().getList("custom-trees");
            if (trees != null) {
                customTrees.clear();
                for (Object obj : trees) {
                    Location loc = convertToLocation(obj);
                    if (loc != null) customTrees.add(loc);
                }
            }
        }
        if (plugin.getConfig().contains("task-chests")) {
            List<?> chests = plugin.getConfig().getList("task-chests");
            if (chests != null) {
                taskChests.clear();
                for (Object obj : chests) {
                    Location loc = convertToLocation(obj);
                    if (loc != null) taskChests.add(loc);
                }
            }
        }
        if (plugin.getConfig().contains("farmlands")) {
            List<?> list = plugin.getConfig().getList("farmlands");
            if (list != null) {
                farmlands.clear();
                for (Object obj : list) {
                    Location loc = convertToLocation(obj);
                    if (loc != null) farmlands.add(loc);
                }
            }
        }
        if (plugin.getConfig().contains("swim_water")) {
            List<?> list = plugin.getConfig().getList("swim_water");
            if (list != null) {
                swimWaterLocations.clear();
                for (Object obj : list) {
                    Location loc = convertToLocation(obj);
                    if (loc != null) swimWaterLocations.add(loc);
                }
            }
        }
        if (plugin.getConfig().contains("task-npcs")) {
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("task-npcs");
            if (section != null) {
                List<String> keys = new ArrayList<>(section.getKeys(false));
                for (String key : keys) {
                    Object locObj = section.get(key + ".location");
                    Location loc = convertToLocation(locObj);
                    if (loc == null) {
                        loc = getLocationFromKey(key);
                        if (loc != null) {
                            section.set(key + ".location", loc);
                            plugin.saveConfig();
                            plugin.getLogger().info("TaskManager: Lokasi untuk " + key + " disimpan dari key.");
                        } else {
                            plugin.getLogger().warning("TaskManager: Gagal mendapatkan lokasi untuk " + key + ", NPC dilewati.");
                            continue;
                        }
                    }
                    String name = section.getString(key + ".name");
                    if (name == null) {
                        plugin.getLogger().warning("TaskManager: Nama tidak ditemukan untuk " + key + ", NPC dilewati.");
                        continue;
                    }
                    String cleanName = name.replace('_', ' ');
                    long duration = section.getLong(key + ".duration", 10);

                    boolean found = false;
                    for (Entity entity : loc.getWorld().getNearbyEntities(loc, 1.0, 1.0, 1.0)) {
                        if (entity.hasMetadata("TaskNPC")) {
                            entity.setCustomName(ChatColor.translateAlternateColorCodes('&', cleanName));
                            entity.setCustomNameVisible(true);
                            entity.setMetadata("TaskNPC", new FixedMetadataValue(plugin, true));
                            entity.setMetadata("NPCDurasi", new FixedMetadataValue(plugin, duration));
                            npcSpawnLocations.put(entity.getUniqueId(), loc.clone());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        spawnNPC(loc, cleanName, duration);
                    }
                }
            }
        }
        loadNPCDisplayNames();
        plantCropsOnAllFarmlands();
    }

    private Location getLocationFromKey(String key) {
        if (!key.startsWith("npc_")) return null;
        String[] parts = key.substring(4).split("_");
        if (parts.length != 3) return null;
        try {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            World world = Bukkit.getWorld("world");
            if (world == null) {
                List<World> worlds = Bukkit.getWorlds();
                if (worlds.isEmpty()) {
                    plugin.getLogger().warning("TaskManager: Tidak ada world yang tersedia!");
                    return null;
                }
                world = worlds.get(0);
                plugin.getLogger().warning("TaskManager: World 'world' tidak ditemukan, gunakan " + world.getName() + " sebagai fallback.");
            }
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("TaskManager: Gagal parse koordinat dari key " + key + ": " + e.getMessage());
            return null;
        }
    }

    private Location convertToLocation(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Location) return (Location) obj;
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.startsWith("world:")) {
                str = str.substring(6);
            }
            String[] parts = str.split(",");
            if (parts.length >= 4) {
                World w = Bukkit.getWorld(parts[0]);
                if (w == null) {
                    List<World> worlds = Bukkit.getWorlds();
                    if (!worlds.isEmpty()) {
                        w = worlds.get(0);
                        plugin.getLogger().warning("TaskManager: World '" + parts[0] + "' tidak ditemukan, gunakan " + w.getName() + " sebagai fallback.");
                    } else {
                        return null;
                    }
                }
                try {
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);
                    Location loc = new Location(w, x, y, z);
                    if (parts.length >= 6) {
                        loc.setYaw(Float.parseFloat(parts[4]));
                        loc.setPitch(Float.parseFloat(parts[5]));
                    }
                    return loc;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            if (str.startsWith("Location{world=")) {
                try {
                    Pattern pattern = Pattern.compile(
                            "world=CraftWorld\\{name=(.*?)\\},x=(-?\\d+\\.?\\d*), y=(-?\\d+\\.?\\d*), z=(-?\\d+\\.?\\d*)"
                    );
                    Matcher matcher = pattern.matcher(str);
                    if (matcher.find()) {
                        String worldName = matcher.group(1);
                        double x = Double.parseDouble(matcher.group(2));
                        double y = Double.parseDouble(matcher.group(3));
                        double z = Double.parseDouble(matcher.group(4));
                        World w = Bukkit.getWorld(worldName);
                        if (w != null) return new Location(w, x, y, z);
                    }
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    public void saveData() {
        plugin.getConfig().set("custom-trees", new ArrayList<>(customTrees));
        plugin.getConfig().set("task-chests", taskChests);
        plugin.getConfig().set("farmlands", new ArrayList<>(farmlands));
        plugin.getConfig().set("swim_water", new ArrayList<>(swimWaterLocations));
        plugin.saveConfig();
    }

    public void resetAllPlayerTasks() {
        hasActiveTask.clear();
        woodCount.clear();
        fishRequired.clear();
        fishCaught.clear();
        activeTaskType.clear();
        isInteractingWithNPC.clear();
        isTaskNPC.clear();
        targetNPCName.clear();
        swimTimeRequired.clear();
        swimTimeElapsed.clear();
        farmHarvestRequired.clear();
        farmHarvestCount.clear();
        farmCropType.clear();
        for (BukkitRunnable task : swimTasks.values()) {
            task.cancel();
        }
        swimTasks.clear();
    }

    public void initializeTasksForGame() {
        resetAllPlayerTasks();
        plantCropsOnAllFarmlands();
    }

    public void resetTasks() {
        resetAllPlayerTasks();
    }

    public void assignInitialTask(Player player) {
        if (hasTask(player)) return;
        startNPCTask(player);
    }

    public void assignTask(Player player) {
        assignInitialTask(player);
    }

    // ===== EXPORT / IMPORT =====
    public void exportToJson(JsonObject root) {
        JsonObject taskData = new JsonObject();

        JsonArray trees = new JsonArray();
        for (Location loc : customTrees) {
            trees.add(loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        }
        taskData.add("custom_trees", trees);

        JsonArray chests = new JsonArray();
        for (Location loc : taskChests) {
            chests.add(loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        }
        taskData.add("task_chests", chests);

        JsonArray farmlandArr = new JsonArray();
        for (Location loc : farmlands) {
            farmlandArr.add(loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        }
        taskData.add("farmlands", farmlandArr);

        JsonArray waterArr = new JsonArray();
        for (Location loc : swimWaterLocations) {
            waterArr.add(loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        }
        taskData.add("swim_water", waterArr);

        JsonObject npcs = new JsonObject();
        ConfigurationSection npcSection = plugin.getConfig().getConfigurationSection("task-npcs");
        if (npcSection != null) {
            for (String key : npcSection.getKeys(false)) {
                JsonObject npc = new JsonObject();
                Object locObj = npcSection.get(key + ".location");
                String locStr = null;
                if (locObj instanceof Location) {
                    Location loc = (Location) locObj;
                    locStr = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
                } else if (locObj instanceof String) {
                    locStr = (String) locObj;
                }
                if (locStr != null) {
                    npc.addProperty("location", locStr);
                }
                npc.addProperty("name", npcSection.getString(key + ".name"));
                npc.addProperty("duration", npcSection.getLong(key + ".duration"));
                npcs.add(key, npc);
            }
        }
        taskData.add("task_npcs", npcs);

        root.add("task_data", taskData);
    }

    public void importFromJson(JsonObject root) {
        if (!root.has("task_data")) {
            plugin.getLogger().warning("TaskManager: Tidak ada objek 'task_data' dalam JSON, lewati.");
            return;
        }
        JsonObject taskData = root.getAsJsonObject("task_data");

        if (taskData.has("custom_trees")) {
            JsonArray trees = taskData.getAsJsonArray("custom_trees");
            customTrees.clear();
            for (int i = 0; i < trees.size(); i++) {
                String[] parts = trees.get(i).getAsString().split(",");
                World w = Bukkit.getWorld(parts[0]);
                if (w != null) {
                    Location loc = new Location(w, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                    customTrees.add(loc);
                }
            }
        }

        if (taskData.has("task_chests")) {
            JsonArray chests = taskData.getAsJsonArray("task_chests");
            taskChests.clear();
            for (int i = 0; i < chests.size(); i++) {
                String[] parts = chests.get(i).getAsString().split(",");
                World w = Bukkit.getWorld(parts[0]);
                if (w != null) {
                    Location loc = new Location(w, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                    taskChests.add(loc);
                }
            }
        }

        if (taskData.has("farmlands")) {
            JsonArray arr = taskData.getAsJsonArray("farmlands");
            farmlands.clear();
            for (int i = 0; i < arr.size(); i++) {
                String[] parts = arr.get(i).getAsString().split(",");
                World w = Bukkit.getWorld(parts[0]);
                if (w != null) {
                    Location loc = new Location(w, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                    farmlands.add(loc);
                }
            }
        }

        if (taskData.has("swim_water")) {
            JsonArray arr = taskData.getAsJsonArray("swim_water");
            swimWaterLocations.clear();
            for (int i = 0; i < arr.size(); i++) {
                String[] parts = arr.get(i).getAsString().split(",");
                World w = Bukkit.getWorld(parts[0]);
                if (w != null) {
                    Location loc = new Location(w, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                    swimWaterLocations.add(loc);
                }
            }
        }

        if (taskData.has("task_npcs")) {
            JsonObject npcs = taskData.getAsJsonObject("task_npcs");
            plugin.getConfig().set("task-npcs", null);
            for (String key : npcs.keySet()) {
                JsonObject npc = npcs.getAsJsonObject(key);
                String locStr = npc.get("location").getAsString();
                String[] parts = locStr.split(",");
                World w = Bukkit.getWorld(parts[0]);
                if (w != null) {
                    Location loc = new Location(w, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                    String name = npc.get("name").getAsString();
                    long duration = npc.get("duration").getAsLong();
                    String cleanName = name.replace('_', ' ');
                    plugin.getConfig().set("task-npcs." + key + ".location", loc);
                    plugin.getConfig().set("task-npcs." + key + ".name", cleanName);
                    plugin.getConfig().set("task-npcs." + key + ".duration", duration);
                }
            }
            plugin.saveConfig();
            reloadAllNPCs();
            loadNPCDisplayNames();
        }

        saveData();
        plantCropsOnAllFarmlands();
    }

    // ======================= GETTERS =======================
    public boolean hasTask(Player player) { return hasActiveTask.contains(player.getUniqueId()); }
    public String getTaskType(Player player) { return activeTaskType.getOrDefault(player.getUniqueId(), "NONE"); }
    public Set<Location> getCustomTrees() { return customTrees; }
    public List<Location> getTaskChests() { return taskChests; }
    public Set<Location> getFarmlands() { return farmlands; }
    public Set<Location> getSwimWaterLocations() { return swimWaterLocations; }

    public Map<UUID, Integer> getWoodCount() { return woodCount; }
    public Map<UUID, Integer> getFishRequired() { return fishRequired; }
    public Map<UUID, Integer> getFishCaught() { return fishCaught; }
    public Set<UUID> getIsInteractingWithNPC() { return isInteractingWithNPC; }
    public boolean isTaskNPC(Player player) { return isTaskNPC.getOrDefault(player.getUniqueId(), false); }
    public String getTargetNPC(Player player) { return targetNPCName.get(player.getUniqueId()); }

    // SWIM
    public boolean isSwimTask(Player p) { return "SWIM".equals(getTaskType(p)); }
    public int getSwimRequired(Player p) { return swimTimeRequired.getOrDefault(p.getUniqueId(), 0); }
    public int getSwimElapsed(Player p) { return swimTimeElapsed.getOrDefault(p.getUniqueId(), 0); }
    public void setSwimElapsed(Player p, int val) { swimTimeElapsed.put(p.getUniqueId(), val); }
    public void resetSwim(Player p) { swimTimeRequired.remove(p.getUniqueId()); swimTimeElapsed.remove(p.getUniqueId()); }

    // FARM
    public boolean isFarmTask(Player p) { return "FARM".equals(getTaskType(p)); }
    public int getFarmRequired(Player p) { return farmHarvestRequired.getOrDefault(p.getUniqueId(), 0); }
    public int getFarmCount(Player p) { return farmHarvestCount.getOrDefault(p.getUniqueId(), 0); }
    public void incrementFarmCount(Player p) {
        UUID u = p.getUniqueId();
        farmHarvestCount.put(u, farmHarvestCount.getOrDefault(u, 0) + 1);
    }
    public Material getFarmCropType(Player p) { return farmCropType.get(p.getUniqueId()); }
    public void resetFarm(Player p) {
        UUID u = p.getUniqueId();
        farmHarvestRequired.remove(u);
        farmHarvestCount.remove(u);
        farmCropType.remove(u);
    }
    public boolean isFarmland(Location loc) { return farmlands.contains(loc); }

    public void shutdown() {
        stopAntiPushTask();
        stopFarmMaintenance();
        for (BukkitRunnable task : swimTasks.values()) {
            task.cancel();
        }
        swimTasks.clear();
    }
}