// ============================================================
// FILE: CorpseGlowManager.java (LENGKAP + getCorpseLocations)
// ============================================================
package com.islandbridge;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CorpseGlowManager {
    private final IslandBridgeAmongUs plugin;
    private final Set<Location> corpseLocations = new HashSet<>();
    private int taskId = -1;

    public CorpseGlowManager(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
    }

    public void addCorpse(Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        corpseLocations.add(loc.clone());
        startGlowTask();
    }

    public void removeCorpse(Location loc) {
        corpseLocations.remove(loc);
        if (corpseLocations.isEmpty() && taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    public void clearAllCorpses() {
        corpseLocations.clear();
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    public int getCorpseCount() {
        return corpseLocations.size();
    }

    /**
     * Mengembalikan daftar lokasi mayat dalam format string yang rapi
     */
    public List<String> getCorpseLocations() {
        List<String> list = new ArrayList<>();
        for (Location loc : corpseLocations) {
            if (loc.getWorld() == null) continue;
            list.add("World: " + loc.getWorld().getName() +
                    " X:" + loc.getBlockX() +
                    " Y:" + loc.getBlockY() +
                    " Z:" + loc.getBlockZ());
        }
        return list;
    }

    private void startGlowTask() {
        if (taskId != -1) return;
        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (corpseLocations.isEmpty()) {
                    clearAllCorpses();
                    return;
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    GameManager gm = plugin.getGameManager();
                    if (gm.getRole(p) != RoleRegistry.Role.JOKER) continue;
                    if (gm.getState() != GameManager.GameState.GAME_RUNNING) continue;
                    if (!gm.isPlayerAlive(p)) continue;

                    for (Location loc : corpseLocations) {
                        boolean stillExists = false;
                        World world = loc.getWorld();
                        if (world != null) {
                            for (org.bukkit.entity.Entity e : world.getNearbyEntities(loc, 1.0, 1.0, 1.0)) {
                                if (e instanceof ArmorStand as && as.getCustomName() != null && as.getCustomName().endsWith("Corpse")) {
                                    stillExists = true;
                                    break;
                                }
                            }
                        }
                        if (!stillExists) {
                            removeCorpse(loc);
                            continue;
                        }

                        p.spawnParticle(Particle.END_ROD, loc.clone().add(0, 0.3, 0), 8, 0.5, 0.5, 0.5, 0);
                        p.spawnParticle(Particle.ENCHANT, loc.clone().add(0, 0.6, 0), 15, 0.6, 0.6, 0.6, 0);
                        p.spawnParticle(Particle.WITCH, loc.clone().add(0, 0.8, 0), 5, 0.4, 0.4, 0.4, 0);
                        p.spawnParticle(Particle.FIREWORK, loc.clone().add(0, 1.0, 0), 3, 0.3, 0.3, 0.3, 0.1);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 8L).getTaskId();
    }

    public Location findNearestCorpse(Player player, double radius) {
        Location closest = null;
        double closestDist = radius;

        for (Location loc : corpseLocations) {
            if (loc.getWorld() == null || !loc.getWorld().equals(player.getWorld())) continue;
            double dist = loc.distance(player.getLocation());
            if (dist < closestDist) {
                closestDist = dist;
                closest = loc;
            }
        }
        return closest;
    }

    public Set<Location> getCorpseLocationsRaw() {
        return Collections.unmodifiableSet(corpseLocations);
    }
}