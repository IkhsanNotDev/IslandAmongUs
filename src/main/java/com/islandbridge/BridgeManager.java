package com.islandbridge;

import com.google.gson.JsonObject;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public class BridgeManager {
    private final IslandBridgeAmongUs plugin;
    private Location pulau1, pulau2;
    private int lebarKiri = 3;
    private int lebarKanan = 3;
    private int totalBaris = 0;
    private int totalBlocks = 0;
    private int blocksBuilt = 0;
    private int targetPlank = 0;
    private int totalPlankDeposited = 0;
    private boolean isBuilt = false;
    private BossBar bossBar;
    private String npcName = "&b&l[NPC] &e&lPembangun Jembatan";
    private List<List<Location>> barisLocations = new ArrayList<>();
    private List<Location> allBlocksLinear = new ArrayList<>();

    private boolean saveScheduled = false;
    private final File bridgeFile;
    private YamlConfiguration bridgeConfig;

    // NPC sekarang disimpan sebagai Entity (bisa ArmorStand atau Mannequin)
    private Entity currentNPC = null;
    private UUID npcUUID = null;
    private Location npcSpawnLoc = null;

    private BukkitRunnable moveTask = null;
    private BukkitRunnable antiPushTask = null;

    public BridgeManager(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
        this.bridgeFile = new File(plugin.getDataFolder(), "bridge_data.yml");
        loadConfig();
        spawnNPCFromData();
        startAntiPushTask();
    }

    public void loadConfig() {
        if (!bridgeFile.exists()) {
            bridgeConfig = new YamlConfiguration();
            saveConfigSync();
        } else {
            bridgeConfig = YamlConfiguration.loadConfiguration(bridgeFile);
        }
        pulau1 = readLocation(bridgeConfig, "pulau1");
        pulau2 = readLocation(bridgeConfig, "pulau2");
        lebarKiri = bridgeConfig.getInt("lebar_kiri", 3);
        lebarKanan = bridgeConfig.getInt("lebar_kanan", 3);
        totalBaris = bridgeConfig.getInt("total_baris", 0);
        blocksBuilt = bridgeConfig.getInt("blocks_built", 0);
        totalPlankDeposited = bridgeConfig.getInt("total_plank_deposited", 0);
        targetPlank = bridgeConfig.getInt("target_plank", 0);
        isBuilt = bridgeConfig.getBoolean("is_built", false);
        npcName = bridgeConfig.getString("npc_name", "&b&l[NPC] &e&lPembangun Jembatan");

        String uuidStr = bridgeConfig.getString("npc.uuid");
        if (uuidStr != null && !uuidStr.isEmpty()) {
            try {
                npcUUID = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {}
        }
        npcSpawnLoc = readLocation(bridgeConfig, "npc.location");

        updateBossBar();
        recalculateBridge();
    }

    private void spawnNPCFromData() {
        if (npcSpawnLoc != null && npcSpawnLoc.getWorld() != null) {
            clearBridgeNPCs(npcSpawnLoc.getWorld());
            if (npcUUID != null) {
                for (Entity e : npcSpawnLoc.getWorld().getEntities()) {
                    if (e.hasMetadata("bridgeNPC") && e.getUniqueId().equals(npcUUID)) {
                        currentNPC = e;
                        break;
                    }
                }
            }
            if (currentNPC == null || currentNPC.isDead()) {
                spawnBridgeNPC(npcSpawnLoc, null);
            } else {
                currentNPC.setCustomName(ChatColor.translateAlternateColorCodes('&', npcName));
                currentNPC.setCustomNameVisible(true);
                currentNPC.setInvulnerable(true);
                currentNPC.setPersistent(true);
                currentNPC.setMetadata("bridgeNPC", new FixedMetadataValue(plugin, true));
                setEntityCollidable(currentNPC, false);
                if (currentNPC instanceof ArmorStand as) {
                    if (as.getHelmet() == null) {
                        com.islandbridge.utils.ArmorUtil.applyRandomArmor(as);
                    }
                }
            }
        }
    }

    private void clearBridgeNPCs(World world) {
        if (world == null) return;
        for (Entity e : world.getEntities()) {
            if (e.hasMetadata("bridgeNPC")) {
                e.remove();
            }
        }
    }

    private void saveConfigSync() {
        try {
            bridgeConfig.save(bridgeFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Gagal menyimpan bridge_data.yml: " + e.getMessage());
        }
    }

    private void scheduleSaveConfig() {
        if (saveScheduled) return;
        saveScheduled = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                flushSaveConfig();
            }
        }.runTaskLaterAsynchronously(plugin, 20L);
    }

    private void flushSaveConfig() {
        saveScheduled = false;
        writeLocation(bridgeConfig, "pulau1", pulau1);
        writeLocation(bridgeConfig, "pulau2", pulau2);
        bridgeConfig.set("lebar_kiri", lebarKiri);
        bridgeConfig.set("lebar_kanan", lebarKanan);
        bridgeConfig.set("total_baris", totalBaris);
        bridgeConfig.set("blocks_built", blocksBuilt);
        bridgeConfig.set("total_plank_deposited", totalPlankDeposited);
        bridgeConfig.set("target_plank", targetPlank);
        bridgeConfig.set("is_built", isBuilt);
        bridgeConfig.set("npc_name", npcName);
        bridgeConfig.set("npc.uuid", npcUUID != null ? npcUUID.toString() : null);
        if (npcSpawnLoc != null) {
            writeLocation(bridgeConfig, "npc.location", npcSpawnLoc);
        } else {
            bridgeConfig.set("npc.location", null);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                bridgeConfig.save(bridgeFile);
                plugin.getLogger().fine("BridgeManager: Konfigurasi tersimpan.");
            } catch (IOException e) {
                plugin.getLogger().warning("Gagal menyimpan bridge_data.yml: " + e.getMessage());
            }
        });
    }

    private void writeLocation(YamlConfiguration config, String path, Location loc) {
        if (loc == null) {
            config.set(path, null);
            return;
        }
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getBlockX());
        config.set(path + ".y", loc.getBlockY());
        config.set(path + ".z", loc.getBlockZ());
    }

    private Location readLocation(YamlConfiguration config, String path) {
        if (!config.contains(path + ".world")) return null;
        World world = Bukkit.getWorld(config.getString(path + ".world"));
        if (world == null) return null;
        int x = config.getInt(path + ".x");
        int y = config.getInt(path + ".y");
        int z = config.getInt(path + ".z");
        return new Location(world, x, y, z);
    }

    public void setPulau1(Location loc) { this.pulau1 = loc.clone(); recalculateBridge(); scheduleSaveConfig(); }
    public void setPulau2(Location loc) { this.pulau2 = loc.clone(); recalculateBridge(); scheduleSaveConfig(); }
    public void setLebarKiri(int lebar) { this.lebarKiri = lebar; recalculateBridge(); scheduleSaveConfig(); }
    public void setLebarKanan(int lebar) { this.lebarKanan = lebar; recalculateBridge(); scheduleSaveConfig(); }
    public void setNpcName(String name) { this.npcName = name; scheduleSaveConfig(); updateNPCName(); }

    private void updateNPCName() {
        if (currentNPC != null && !currentNPC.isDead()) {
            currentNPC.setCustomName(ChatColor.translateAlternateColorCodes('&', npcName));
        }
    }

    // ==================== SPAWN NPC (MANNEQUIN / ARMORSTAND) ====================
    public void spawnBridgeNPC(Location loc, String displayName) {
        if (loc == null) return;
        clearBridgeNPCs(loc.getWorld());
        if (moveTask != null) {
            moveTask.cancel();
            moveTask = null;
        }

        String coloredName = ChatColor.translateAlternateColorCodes('&', displayName != null ? displayName : npcName);
        World world = loc.getWorld();

        // Coba spawn Mannequin
        Entity npc = trySpawnMannequin(loc, coloredName);
        if (npc == null) {
            // Fallback ke ArmorStand
            npc = spawnArmorStand(loc, coloredName);
        }

        if (npc != null) {
            npc.setCustomName(coloredName);
            npc.setCustomNameVisible(true);
            npc.setInvulnerable(true);
            npc.setPersistent(true);
            npc.setMetadata("bridgeNPC", new FixedMetadataValue(plugin, true));
            setEntityCollidable(npc, false);

            currentNPC = npc;
            npcUUID = npc.getUniqueId();
            npcSpawnLoc = loc.clone();

            scheduleSaveConfig();
        }
    }

    private Entity trySpawnMannequin(Location loc, String name) {
        try {
            // Cek apakah EntityType MANNEQUIN tersedia
            EntityType type;
            try {
                type = EntityType.valueOf("MANNEQUIN");
            } catch (IllegalArgumentException e) {
                // Versi server tidak mendukung Mannequin
                return null;
            }
            Entity entity = loc.getWorld().spawnEntity(loc, type);
            // Coba cast ke Mannequin, jika gagal berarti bukan Mannequin (tapi seharusnya)
            if (entity instanceof Mannequin) {
                // Tidak ada setProfile, biarkan default
                return entity;
            } else {
                entity.remove();
                return null;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Gagal spawn Mannequin: " + e.getMessage());
            return null;
        }
    }

    private Entity spawnArmorStand(Location loc, String name) {
        ArmorStand as = loc.getWorld().spawn(loc, ArmorStand.class);
        as.setArms(true);
        as.setBasePlate(false);
        as.setGravity(false);
        as.setMarker(false);
        as.setRemoveWhenFarAway(false);
        setEntityCollidable(as, false);
        com.islandbridge.utils.ArmorUtil.applyRandomArmor(as);
        return as;
    }

    public void setNPCLocation(Location loc) {
        if (loc == null) return;
        npcSpawnLoc = loc.clone();
        spawnBridgeNPC(loc, null);
    }

    // ==================== ANTI-PUSH (NPC tidak bisa didorong) ====================
    private void startAntiPushTask() {
        if (antiPushTask != null) return;
        antiPushTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (currentNPC == null || currentNPC.isDead()) return;
                // Reset velocity agar tidak terdorong
                currentNPC.setVelocity(new Vector(0, 0, 0));
                // Jika posisi menyimpang lebih dari 0.1 block, teleport balik ke posisi spawn
                if (npcSpawnLoc != null && currentNPC.getLocation().distance(npcSpawnLoc) > 0.1) {
                    // Hanya teleport jika tidak sedang dalam gerakan (moveTask null)
                    if (moveTask == null) {
                        currentNPC.teleport(npcSpawnLoc);
                    }
                }
            }
        };
        antiPushTask.runTaskTimer(plugin, 0L, 1L);
    }

    private void stopAntiPushTask() {
        if (antiPushTask != null) {
            antiPushTask.cancel();
            antiPushTask = null;
        }
    }

    // ==================== ANIMASI GERAK DENGAN KAKI BERGERAK ====================
    public void moveNPCSmoothly(Location target) {
        moveNPCSmoothly(target, 0.3);
    }

    public void moveNPCSmoothly(Location target, double speed) {
        if (currentNPC == null || currentNPC.isDead()) return;
        if (target == null) return;
        if (moveTask != null) {
            moveTask.cancel();
            moveTask = null;
        }

        // Hentikan anti-push sementara agar NPC bisa bergerak
        stopAntiPushTask();

        final Location start = currentNPC.getLocation().clone();
        final Location end = target.clone();
        final double spd = speed;
        double distance = start.distance(end);
        if (distance < 0.1) {
            currentNPC.teleport(end);
            startAntiPushTask();
            return;
        }

        final int steps = (int) Math.ceil(distance / spd);
        final int totalSteps = Math.max(steps, 1);
        final boolean isArmorStand = currentNPC instanceof ArmorStand;

        moveTask = new BukkitRunnable() {
            int step = 0;
            float phase = 0;

            @Override
            public void run() {
                if (currentNPC == null || currentNPC.isDead()) {
                    cancel();
                    moveTask = null;
                    startAntiPushTask();
                    return;
                }
                step++;
                double progress = Math.min(1.0, (double) step / totalSteps);
                double x = start.getX() + (end.getX() - start.getX()) * progress;
                double y = start.getY() + (end.getY() - start.getY()) * progress;
                double z = start.getZ() + (end.getZ() - start.getZ()) * progress;
                Location newLoc = new Location(start.getWorld(), x, y, z);
                newLoc.setYaw(end.getYaw());
                newLoc.setPitch(end.getPitch());
                currentNPC.teleport(newLoc);

                // Animasi kaki (hanya untuk ArmorStand)
                if (isArmorStand) {
                    ArmorStand as = (ArmorStand) currentNPC;
                    phase += 0.5f;
                    float angle = (float) Math.sin(phase) * 0.6f;
                    as.setRightLegPose(new EulerAngle(angle, 0, 0));
                    as.setLeftLegPose(new EulerAngle(-angle, 0, 0));
                }

                // Efek partikel jejak
                currentNPC.getWorld().spawnParticle(Particle.PORTAL, newLoc.clone().add(0, 1, 0), 2, 0.2, 0.2, 0.2, 0);

                if (step >= totalSteps) {
                    currentNPC.teleport(end);
                    // Reset pose kaki jika ArmorStand
                    if (isArmorStand) {
                        ((ArmorStand) currentNPC).setRightLegPose(EulerAngle.ZERO);
                        ((ArmorStand) currentNPC).setLeftLegPose(EulerAngle.ZERO);
                    }
                    cancel();
                    moveTask = null;
                    // Nyalakan anti-push kembali setelah gerakan selesai
                    startAntiPushTask();
                }
            }
        };
        moveTask.runTaskTimer(plugin, 0L, 1L);
    }

    // ==================== LOGIKA JEMBATAN (TIDAK BERUBAH) ====================
    private void recalculateBridge() {
        if (pulau1 == null || pulau2 == null) {
            totalBaris = 0;
            totalBlocks = 0;
            barisLocations.clear();
            allBlocksLinear.clear();
            return;
        }
        barisLocations.clear();
        allBlocksLinear.clear();
        int x1 = pulau1.getBlockX(), z1 = pulau1.getBlockZ();
        int x2 = pulau2.getBlockX(), z2 = pulau2.getBlockZ();
        int y = pulau1.getBlockY();
        boolean isAlongX = Math.abs(x1 - x2) > Math.abs(z1 - z2);
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(z2 - z1));
        for (int i = 0; i <= steps; i++) {
            double t = (steps == 0) ? 0 : (double) i / steps;
            int cx = x1 + (int) Math.round(t * (x2 - x1));
            int cz = z1 + (int) Math.round(t * (z2 - z1));
            List<Location> baris = new ArrayList<>();
            if (isAlongX) {
                for (int w = -lebarKiri; w <= lebarKanan; w++) {
                    Location loc = new Location(pulau1.getWorld(), cx, y, cz + w);
                    baris.add(loc);
                    allBlocksLinear.add(loc);
                }
            } else {
                for (int w = -lebarKiri; w <= lebarKanan; w++) {
                    Location loc = new Location(pulau1.getWorld(), cx + w, y, cz);
                    baris.add(loc);
                    allBlocksLinear.add(loc);
                }
            }
            barisLocations.add(baris);
        }
        totalBaris = barisLocations.size();
        totalBlocks = allBlocksLinear.size();
        updateBossBar();
        scheduleSaveConfig();
    }

    public void setTargetByCrewmateCount(int crewmateCount) {
        if (totalBlocks == 0) recalculateBridge();
        targetPlank = crewmateCount * 25;
        totalPlankDeposited = 0;
        blocksBuilt = 0;
        isBuilt = false;
        scheduleSaveConfig();
        updateBossBar();
    }

    public void depositPlanks(Player player) {
        if (plugin.getGameManager().getState() != GameManager.GameState.GAME_RUNNING) {
            player.sendMessage(ChatColor.RED + "Game sedang tidak berjalan!");
            return;
        }
        if (isBuilt) {
            player.sendMessage(ChatColor.GOLD + "Jembatan sudah selesai dibangun!");
            return;
        }
        if (totalPlankDeposited >= targetPlank) {
            player.sendMessage(ChatColor.GOLD + "Jembatan sudah selesai!");
            return;
        }

        int currentPlanks = countOakPlanks(player);
        if (currentPlanks < 1) {
            player.sendMessage(ChatColor.RED + "Kamu tidak memiliki Oak Plank!");
            return;
        }

        int depositAmount = new Random().nextInt(3) + 3;
        if (depositAmount > currentPlanks) depositAmount = currentPlanks;
        if (depositAmount > (targetPlank - totalPlankDeposited)) depositAmount = targetPlank - totalPlankDeposited;

        removeOakPlanks(player, depositAmount);
        totalPlankDeposited += depositAmount;

        double progress = (double) totalPlankDeposited / targetPlank;
        int targetBlocks = (int) Math.ceil(progress * totalBlocks);
        if (targetBlocks > totalBlocks) targetBlocks = totalBlocks;

        if (targetBlocks > blocksBuilt) {
            for (int i = blocksBuilt; i < targetBlocks; i++) {
                Location blockLoc = allBlocksLinear.get(i);
                animateBlockPlace(blockLoc);
            }
            blocksBuilt = targetBlocks;
        }

        if (blocksBuilt > 0 && blocksBuilt <= totalBlocks) {
            Location lastBlock = allBlocksLinear.get(blocksBuilt - 1);
            Location npcLoc = lastBlock.clone().add(0, 1.5, 0);
            if (currentNPC != null && !currentNPC.isDead()) {
                moveNPCSmoothly(npcLoc);
            }
        }

        scheduleSaveConfig();
        updateBossBar();

        player.sendMessage(ChatColor.GREEN + "✔ Menyetor " + depositAmount + " Oak Plank! (" + totalPlankDeposited + "/" + targetPlank + ")");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

        if (totalPlankDeposited >= targetPlank && !isBuilt) {
            isBuilt = true;
            scheduleSaveConfig();
            updateBossBar();
            if (currentNPC != null && !currentNPC.isDead() && npcSpawnLoc != null) {
                moveNPCSmoothly(npcSpawnLoc);
            }
            plugin.getGameManager().celebrateVictory("Crewmate menang! Jembatan selesai!");
        }
    }

    private int countOakPlanks(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.OAK_PLANKS) count += item.getAmount();
        }
        return count;
    }

    private void removeOakPlanks(Player player, int amount) {
        int toRemove = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.OAK_PLANKS) {
                int amt = item.getAmount();
                if (amt <= toRemove) {
                    toRemove -= amt;
                    player.getInventory().removeItem(item);
                } else {
                    item.setAmount(amt - toRemove);
                    toRemove = 0;
                    break;
                }
                if (toRemove <= 0) break;
            }
        }
        player.updateInventory();
    }

    private void animateBlockPlace(Location target) {
        World world = target.getWorld();
        Location spawnLoc = target.clone().add(0, 10, 0);
        FallingBlock falling = world.spawnFallingBlock(spawnLoc, Material.ORANGE_CONCRETE_POWDER.createBlockData());
        falling.setDropItem(false);
        falling.setVelocity(new Vector(0, -0.5, 0));
        falling.setGravity(true);
        BukkitRunnable timeout = new BukkitRunnable() {
            @Override
            public void run() {
                if (!falling.isDead()) {
                    falling.remove();
                    if (target.getBlock().getType() == Material.AIR || target.getBlock().getType() == Material.WATER)
                        target.getBlock().setType(Material.OAK_PLANKS);
                }
            }
        };
        timeout.runTaskLater(plugin, 100L);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (falling.isDead() || falling.getLocation().getY() <= target.getY() + 0.5) {
                    falling.remove();
                    timeout.cancel();
                    if (target.getBlock().getType() == Material.AIR || target.getBlock().getType() == Material.WATER)
                        target.getBlock().setType(Material.OAK_PLANKS);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void resetBridge() {
        for (Location loc : allBlocksLinear) {
            if (loc.getBlock().getType() == Material.OAK_PLANKS) loc.getBlock().setType(Material.AIR);
        }
        blocksBuilt = 0;
        totalPlankDeposited = 0;
        isBuilt = false;
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        updateBossBar();
        scheduleSaveConfig();
        if (currentNPC != null && !currentNPC.isDead() && npcSpawnLoc != null) {
            moveNPCSmoothly(npcSpawnLoc);
        }
    }

    private void updateBossBar() {
        if (targetPlank <= 0) return;
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(ChatColor.GOLD + "Progress Jembatan", BarColor.GREEN, BarStyle.SEGMENTED_10);
            for (Player p : Bukkit.getOnlinePlayers()) bossBar.addPlayer(p);
        }
        double progress = (double) totalPlankDeposited / targetPlank;
        bossBar.setProgress(Math.min(1.0, progress));
        if (isBuilt) {
            bossBar.setTitle(ChatColor.GREEN + "Jembatan Selesai!");
        } else {
            bossBar.setTitle(ChatColor.GOLD + "Progress Jembatan (" + totalPlankDeposited + "/" + targetPlank + " plank)");
        }
    }

    public void respawnNPC() {
        if (npcSpawnLoc != null && npcSpawnLoc.getWorld() != null) {
            clearBridgeNPCs(npcSpawnLoc.getWorld());
            spawnBridgeNPC(npcSpawnLoc, null);
        }
    }

    // ===== REFLEKSI UNTUK SETCOLLIDABLE =====
    private void setEntityCollidable(Entity entity, boolean collidable) {
        try {
            Method method = Entity.class.getMethod("setCollidable", boolean.class);
            method.invoke(entity, collidable);
        } catch (NoSuchMethodException ignored) {
            // Versi server lama mungkin tidak punya method ini, abaikan
        } catch (Exception e) {
            plugin.getLogger().warning("Gagal setCollidable: " + e.getMessage());
        }
    }

    // ===== EXPORT / IMPORT =====
    public void exportToJson(JsonObject root) {
        JsonObject bridge = new JsonObject();
        bridge.addProperty("lebar_kiri", lebarKiri);
        bridge.addProperty("lebar_kanan", lebarKanan);
        bridge.addProperty("total_baris", totalBaris);
        bridge.addProperty("blocks_built", blocksBuilt);
        bridge.addProperty("total_plank_deposited", totalPlankDeposited);
        bridge.addProperty("target_plank", targetPlank);
        bridge.addProperty("is_built", isBuilt);
        bridge.addProperty("npc_name", npcName);
        bridge.addProperty("npc.uuid", npcUUID != null ? npcUUID.toString() : "");

        if (pulau1 != null) {
            JsonObject p1 = new JsonObject();
            p1.addProperty("world", pulau1.getWorld().getName());
            p1.addProperty("x", pulau1.getX());
            p1.addProperty("y", pulau1.getY());
            p1.addProperty("z", pulau1.getZ());
            bridge.add("pulau1", p1);
        }
        if (pulau2 != null) {
            JsonObject p2 = new JsonObject();
            p2.addProperty("world", pulau2.getWorld().getName());
            p2.addProperty("x", pulau2.getX());
            p2.addProperty("y", pulau2.getY());
            p2.addProperty("z", pulau2.getZ());
            bridge.add("pulau2", p2);
        }
        if (npcSpawnLoc != null) {
            JsonObject loc = new JsonObject();
            loc.addProperty("world", npcSpawnLoc.getWorld().getName());
            loc.addProperty("x", npcSpawnLoc.getX());
            loc.addProperty("y", npcSpawnLoc.getY());
            loc.addProperty("z", npcSpawnLoc.getZ());
            bridge.add("npc.location", loc);
        }
        root.add("bridge_data", bridge);
    }

    public void importFromJson(JsonObject root) {
        if (!root.has("bridge_data")) {
            plugin.getLogger().warning("BridgeManager: Tidak ada objek 'bridge_data' dalam JSON, lewati.");
            return;
        }
        JsonObject bridge = root.getAsJsonObject("bridge_data");

        this.lebarKiri = getIntOrDefault(bridge, "lebar_kiri", 3);
        this.lebarKanan = getIntOrDefault(bridge, "lebar_kanan", 3);
        this.totalBaris = getIntOrDefault(bridge, "total_baris", 0);
        this.blocksBuilt = getIntOrDefault(bridge, "blocks_built", 0);
        this.totalPlankDeposited = getIntOrDefault(bridge, "total_plank_deposited", 0);
        this.targetPlank = getIntOrDefault(bridge, "target_plank", 0);
        this.isBuilt = getBooleanOrDefault(bridge, "is_built", false);
        this.npcName = getStringOrDefault(bridge, "npc_name", "&b&l[NPC] &e&lPembangun Jembatan");

        if (bridge.has("npc.uuid")) {
            String uuidStr = bridge.get("npc.uuid").getAsString();
            if (uuidStr != null && !uuidStr.isEmpty()) {
                try {
                    this.npcUUID = UUID.fromString(uuidStr);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        JsonObject p1 = bridge.getAsJsonObject("pulau1");
        if (p1 != null) {
            World w = Bukkit.getWorld(p1.get("world").getAsString());
            if (w != null) {
                this.pulau1 = new Location(w, p1.get("x").getAsDouble(), p1.get("y").getAsDouble(), p1.get("z").getAsDouble());
            }
        }
        JsonObject p2 = bridge.getAsJsonObject("pulau2");
        if (p2 != null) {
            World w = Bukkit.getWorld(p2.get("world").getAsString());
            if (w != null) {
                this.pulau2 = new Location(w, p2.get("x").getAsDouble(), p2.get("y").getAsDouble(), p2.get("z").getAsDouble());
            }
        }
        JsonObject loc = bridge.getAsJsonObject("npc.location");
        if (loc != null) {
            World w = Bukkit.getWorld(loc.get("world").getAsString());
            if (w != null) {
                this.npcSpawnLoc = new Location(w, loc.get("x").getAsDouble(), loc.get("y").getAsDouble(), loc.get("z").getAsDouble());
            }
        }

        recalculateBridge();
        respawnNPC();
        scheduleSaveConfig();
        updateBossBar();
    }

    private int getIntOrDefault(JsonObject obj, String key, int def) {
        return obj.has(key) ? obj.get(key).getAsInt() : def;
    }
    private boolean getBooleanOrDefault(JsonObject obj, String key, boolean def) {
        return obj.has(key) ? obj.get(key).getAsBoolean() : def;
    }
    private String getStringOrDefault(JsonObject obj, String key, String def) {
        return obj.has(key) ? obj.get(key).getAsString() : def;
    }

    // Legacy & Getter
    public void setTargetPlanks(int crewmateCount) { setTargetByCrewmateCount(crewmateCount); }
    public void addBridgeProgressOnCrewmateDeath() {}
    public void addBridgeProgressFromTask(int amount) {}
    public void buildBridge() {}
    public boolean isBridgeComplete() { return isBuilt; }
    public int getBarisBuilt() { return blocksBuilt; }
    public int getTotalBaris() { return totalBlocks; }
    public int getTargetPlank() { return targetPlank; }
    public int getTotalPlankDeposited() { return totalPlankDeposited; }
    public IslandBridgeAmongUs getPlugin() { return plugin; }
    public int getBlocksBuilt() { return blocksBuilt; }
    public int getTotalBlocks() { return totalBlocks; }
}