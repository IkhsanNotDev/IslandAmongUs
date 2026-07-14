// ============================================================
// FILE: GameManager.java (LENGKAP - DENGAN VOICE CHAT INTEGRATION + TASK CANCEL + MEETING FIX)
// ============================================================
package com.islandbridge;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {
    public enum GameState { WAITING, STARTING, GAME_RUNNING, MEETING, GAME_END }
    private GameState state = GameState.WAITING;
    private final IslandBridgeAmongUs plugin;
    private final Map<UUID, RoleRegistry.Role> playerRoles = new HashMap<>();
    private final Set<UUID> livingPlayers = new HashSet<>();
    private final Set<UUID> deadPlayers = new HashSet<>();
    private Location lobbyLocation, mapSpawn, meetingCenter;
    private int countdownTaskId = -1;
    private boolean testingMode = false;

    private long emergencyLastUsed = 0;
    private static final long EMERGENCY_COOLDOWN_SECONDS = 90;
    private final Map<UUID, Long> killCooldown = new HashMap<>();
    // ★ Cooldown kill dinamis (bisa diubah via command)
    private long killCooldownSeconds = 60; // default

    // ===== VOID EXEMPT =====
    private final Set<UUID> voidExemptPlayers = new HashSet<>();

    public GameManager(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
        loadLocationsFromConfig();
        loadConfigValues();
    }

    // ★ Muat semua nilai dari config yang bisa diubah via command setting
    public void loadConfigValues() {
        FileConfiguration config = plugin.getConfig();
        killCooldownSeconds = config.getLong("game.kill-cooldown-seconds", 60);
        // Nilai lain (min-players, countdown, dll) dibaca langsung di tempat masing-masing
    }

    // ★ Setter untuk cooldown kill (dipanggil dari command)
    public void setKillCooldownSeconds(long seconds) {
        this.killCooldownSeconds = seconds;
        plugin.getConfig().set("game.kill-cooldown-seconds", seconds);
        plugin.saveConfig();
    }

    public long getKillCooldownSeconds() { return killCooldownSeconds; }

    public void loadLocationsFromConfig() {
        FileConfiguration config = plugin.getConfig();
        this.lobbyLocation = parseLocation(config.getString("lobby.location"));
        this.mapSpawn = parseLocation(config.getString("map-spawn.location"));
        this.meetingCenter = parseLocation(config.getString("meeting-center.location"));
    }

    private void saveLocationAsync(String path, Location loc) {
        if (loc == null) {
            plugin.getConfig().set(path, null);
        } else {
            plugin.getConfig().set(path, loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.saveConfig();
            } catch (Exception e) {
                plugin.getLogger().warning("Gagal menyimpan config.yml: " + e.getMessage());
            }
        });
    }

    private Location parseLocation(String str) {
        if (str == null || str.isEmpty()) return null;
        String[] parts = str.split(",");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public boolean isTestingMode() { return testingMode; }
    public void setTestingMode(boolean mode) { this.testingMode = mode; }

    public boolean isMeetingRunning() { return this.state == GameState.MEETING; }

    // ===== VOID EXEMPT =====
    public void addVoidExempt(Player player) {
        voidExemptPlayers.add(player.getUniqueId());
    }
    public void removeVoidExempt(Player player) {
        voidExemptPlayers.remove(player.getUniqueId());
    }
    public boolean isVoidExempt(UUID uuid) {
        return voidExemptPlayers.contains(uuid);
    }
    public void clearVoidExempt() {
        voidExemptPlayers.clear();
    }

    public void handleMeetingQuit(Player player) {
        if (player == null) return;
        handleMeetingQuitByUUID(player.getUniqueId());
    }

    public void handleMeetingQuitByUUID(UUID uid) {
        if (uid == null) return;
        if (livingPlayers.contains(uid)) {
            String name = Bukkit.getOfflinePlayer(uid).getName();
            if (name == null) name = "Pemain Terputus";

            // ★ PERUBAHAN: Hapus broadcast publik dan pembagian plank saat disconnect saat meeting
            // broadcast(ChatColor.YELLOW + name + " telah keluar dari server (Disconnect) saat meeting!");

            RoleRegistry.Role role = playerRoles.get(uid);
            livingPlayers.remove(uid);
            deadPlayers.add(uid);

            // ★ PERUBAHAN: Hapus pembagian plank
            // if (role == RoleRegistry.Role.CREWMATE) {
            //     distributePlanksToCrewmates(uid, 8);
            // }

            playSoundToAll(Sound.ENTITY_PLAYER_DEATH, 1.0f, 0.8f);
            plugin.getScoreboardManager().updateAll();
            checkGameOver();
        }
    }

    private void distributePlanksToCrewmates(UUID excludedUid, int totalPlanks) {
        List<Player> aliveCrewmates = new ArrayList<>();
        for (UUID uid : livingPlayers) {
            if (excludedUid != null && uid.equals(excludedUid)) continue;
            Player p = Bukkit.getPlayer(uid);
            if (p != null && p.isOnline() && getRole(p) == RoleRegistry.Role.CREWMATE) {
                aliveCrewmates.add(p);
            }
        }
        if (aliveCrewmates.isEmpty()) {
            broadcast(ChatColor.GRAY + "Tidak ada crewmate hidup yang tersedia untuk menerima plank.");
            return;
        }

        int each = totalPlanks / aliveCrewmates.size();
        int remainder = totalPlanks % aliveCrewmates.size();

        for (Player p : aliveCrewmates) {
            int give = each;
            if (remainder > 0) {
                give++;
                remainder--;
            }
            if (give > 0) {
                ItemStack planks = new ItemStack(Material.OAK_PLANKS, give);
                var leftover = p.getInventory().addItem(planks);
                if (!leftover.isEmpty()) {
                    p.getWorld().dropItemNaturally(p.getLocation(), planks);
                    p.sendMessage(ChatColor.YELLOW + "Inventory penuh! " + give + " Oak Planks dijatuhkan di tanah.");
                } else {
                    p.sendMessage(ChatColor.GREEN + "Kamu menerima " + give + " Oak Planks dari kematian rekan crewmate.");
                }
            }
        }
        broadcast(ChatColor.GOLD + String.valueOf(totalPlanks) + " Oak Planks telah dibagikan ke crewmate yang masih hidup!");
    }

    private void distributePlanksToCrewmates(Player excluded, int totalPlanks) {
        distributePlanksToCrewmates(excluded != null ? excluded.getUniqueId() : null, totalPlanks);
    }

    // ★ Method cooldown kill menggunakan variabel dinamis
    public boolean canKill(Player impostor) {
        Long lastKill = killCooldown.get(impostor.getUniqueId());
        if (lastKill == null) return true;
        long now = System.currentTimeMillis() / 1000;
        return (now - lastKill) >= killCooldownSeconds;
    }

    public void setKillCooldown(Player impostor) {
        killCooldown.put(impostor.getUniqueId(), System.currentTimeMillis() / 1000);
    }

    public long getKillRemaining(Player impostor) {
        Long lastKill = killCooldown.get(impostor.getUniqueId());
        if (lastKill == null) return 0;
        long now = System.currentTimeMillis() / 1000;
        return Math.max(0, killCooldownSeconds - (now - lastKill));
    }

    public boolean canUseEmergency() {
        long now = System.currentTimeMillis() / 1000;
        return (now - emergencyLastUsed) >= EMERGENCY_COOLDOWN_SECONDS;
    }
    public void setEmergencyCooldown() {
        emergencyLastUsed = System.currentTimeMillis() / 1000;
    }
    public long getEmergencyRemaining() {
        long now = System.currentTimeMillis() / 1000;
        return Math.max(0, EMERGENCY_COOLDOWN_SECONDS - (now - emergencyLastUsed));
    }

    public void playSoundToAll(Sound sound, float volume, float pitch) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }

    public void applySaturation(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 255, false, false));
    }

    public void teleportToLobbyCircle() {
        if (lobbyLocation == null) {
            plugin.getLogger().warning("Lobby location not set! Cannot teleport players.");
            return;
        }
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) return;
        int radius = 3;
        for (int i = 0; i < players.size(); i++) {
            double angle = 2 * Math.PI * i / players.size();
            double x = lobbyLocation.getX() + radius * Math.cos(angle);
            double z = lobbyLocation.getZ() + radius * Math.sin(angle);
            double y = lobbyLocation.getY();
            players.get(i).teleport(new Location(lobbyLocation.getWorld(), x, y, z));
        }
        plugin.getLogger().info("Teleported " + players.size() + " players to lobby.");
    }

    private void teleportToMapCircle(List<Player> players) {
        if (mapSpawn == null) return;
        int radius = 3;
        for (int i = 0; i < players.size(); i++) {
            double angle = 2 * Math.PI * i / players.size();
            double x = mapSpawn.getX() + radius * Math.cos(angle);
            double z = mapSpawn.getZ() + radius * Math.sin(angle);
            double y = mapSpawn.getY();
            players.get(i).teleport(new Location(mapSpawn.getWorld(), x, y, z));
        }
    }

    public void startGame() {
        if (state != GameState.WAITING && state != GameState.GAME_END) return;
        int playerCount = Bukkit.getOnlinePlayers().size();
        if (!testingMode && playerCount < plugin.getConfig().getInt("game.min-players", 2)) {
            broadcast(ChatColor.RED + "Minimal " + plugin.getConfig().getInt("game.min-players") + " player untuk mulai!");
            return;
        }
        state = GameState.STARTING;
        broadcast(ChatColor.YELLOW + "Game akan dimulai dalam " + plugin.getConfig().getInt("game.countdown", 10) + " detik!");
        playSoundToAll(Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 0.5f);
        countdownTaskId = new BukkitRunnable() {
            int count = plugin.getConfig().getInt("game.countdown", 10);
            @Override
            public void run() {
                if (count <= 0) {
                    cancel();
                    startGameNow();
                } else {
                    broadcast(ChatColor.GREEN + "Memulai dalam " + count + "...");
                    playSoundToAll(Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.0f + (count * 0.05f));
                    count--;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    private void startGameNow() {
        if (countdownTaskId != -1) Bukkit.getScheduler().cancelTask(countdownTaskId);
        state = GameState.GAME_RUNNING;
        livingPlayers.clear();
        deadPlayers.clear();
        playerRoles.clear();
        emergencyLastUsed = 0;
        killCooldown.clear();

        plugin.getBridgeManager().loadConfig();
        plugin.getSabotageManager().loadConfig();
        loadLocationsFromConfig();
        plugin.getTaskManager().reloadAllNPCs();
        plugin.getBridgeManager().respawnNPC();

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player p : players) livingPlayers.add(p.getUniqueId());

        for (Player p : players) {
            plugin.getSkinWalker().autoDetectAndSaveSkin(p);
        }

        assignRoles(players);
        int crewmateCount = (int) playerRoles.values().stream().filter(r -> r == RoleRegistry.Role.CREWMATE).count();
        plugin.getBridgeManager().setTargetByCrewmateCount(crewmateCount);

        startGachaAnimation(players);
    }

    private void startGachaAnimation(List<Player> players) {
        for (Player p : players) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 2, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 255, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 255, false, false));
        }

        playSoundToAll(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f);
        playSoundToAll(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f);
        playSoundToAll(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);

        for (Player p : players) {
            p.sendTitle(ChatColor.GOLD + "⚡ MEMILIH ROLE ⚡", ChatColor.GRAY + "Sedang mengocok...", 10, 30, 10);
        }

        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 2) {
                    cancel();
                    for (Player p : players) {
                        RoleRegistry.Role role = getRole(p);
                        String roleName = role.getDisplay();
                        if (role == RoleRegistry.Role.CREWMATE) {
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            p.sendTitle(ChatColor.GREEN + "Kamu adalah " + roleName, ChatColor.AQUA + "Bantu bangun jembatan!", 10, 60, 20);
                        } else if (role == RoleRegistry.Role.IMPOSTOR) {
                            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
                            p.sendTitle(ChatColor.RED + "Kamu adalah " + roleName, ChatColor.DARK_RED + "Bunuh semua crewmate!", 10, 60, 20);
                        } else if (role == RoleRegistry.Role.JOKER) {
                            p.playSound(p.getLocation(), Sound.ENTITY_VEX_AMBIENT, 1.0f, 1.2f);
                            p.sendTitle(ChatColor.LIGHT_PURPLE + "Kamu adalah " + roleName, ChatColor.GRAY + "Menang jika di-vote!", 10, 60, 20);
                        }
                    }
                    playSoundToAll(Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.7f);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        for (Player p : players) {
                            p.removePotionEffect(PotionEffectType.BLINDNESS);
                            p.removePotionEffect(PotionEffectType.SLOWNESS);
                            p.removePotionEffect(PotionEffectType.WEAKNESS);
                        }
                        teleportToMapCircle(players);
                        plugin.getBridgeManager().resetBridge();
                        plugin.getTaskManager().initializeTasksForGame();
                        for (Player p : players) {
                            p.getInventory().clear();
                            p.setHealth(20);
                            p.setFoodLevel(20);
                            for (PotionEffect effect : p.getActivePotionEffects()) p.removePotionEffect(effect.getType());
                            applySaturation(p);
                            for (Player other : players) p.showPlayer(plugin, other);
                            p.setGameMode(GameMode.SURVIVAL);
                            p.setWalkSpeed(0.2f);
                            p.sendMessage(ChatColor.GREEN + "Game dimulai! Role kamu: " + playerRoles.get(p.getUniqueId()).getDisplay());
                            if (playerRoles.get(p.getUniqueId()) == RoleRegistry.Role.CREWMATE) {
                                p.sendMessage(ChatColor.AQUA + "Selesaikan task untuk mendapatkan kayu oak, lalu setorkan ke NPC jembatan!");
                                plugin.getTaskManager().assignInitialTask(p);
                            } else if (playerRoles.get(p.getUniqueId()) == RoleRegistry.Role.IMPOSTOR) {
                                p.sendMessage(ChatColor.RED + "Bunuh semua crewmate! Gunakan pedang untuk membunuh.");
                                p.getInventory().setItem(8, new ItemStack(Material.IRON_SWORD));
                                p.getInventory().setItem(6, new ItemStack(Material.NETHER_STAR));
                                // Fake task berjalan otomatis, tidak perlu buku
                                p.getInventory().setHeldItemSlot(0);
                                // Beri task nyata juga untuk impostor
                                plugin.getTaskManager().assignInitialTask(p);
                            } else if (playerRoles.get(p.getUniqueId()) == RoleRegistry.Role.JOKER) {
                                p.sendMessage(ChatColor.LIGHT_PURPLE + "Kamu adalah Joker! Tujuanmu: di-vote keluar! Gunakan map untuk melihat mayat.");
                                giveJokerItems(p);
                                p.getInventory().setHeldItemSlot(0);
                                // Beri task nyata juga untuk joker
                                plugin.getTaskManager().assignInitialTask(p);
                            }
                        }
                        broadcast(ChatColor.GOLD + "Game dimulai! Selamat bermain!");
                        plugin.getScoreboardManager().updateAll();

                        // ★ VOICE CHAT: Pastikan semua pemain hidup di-unmute saat game mulai
                        plugin.getSabotageManager().unmuteAllAlive();
                    }, 20L);
                    return;
                }
                playSoundToAll(Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 0.7f + (count * 0.1f));
                playSoundToAll(Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                count++;
            }
        }.runTaskTimer(plugin, 5L, 10L);
    }

    // ==================== PEMBERIAN ITEM JOKER ====================
    private void giveJokerItems(Player player) {
        // Map di slot 7 (index 7 = slot ke-8)
        ItemStack map = new ItemStack(Material.MAP);
        ItemMeta mapMeta = map.getItemMeta();
        mapMeta.setDisplayName(ChatColor.GOLD + "Peta Mayat");
        mapMeta.setLore(Arrays.asList(ChatColor.GRAY + "Klik kanan untuk melihat lokasi mayat"));
        map.setItemMeta(mapMeta);
        player.getInventory().setItem(7, map);

        // Tidak perlu buku fake task karena otomatis
    }

    // ==================== SISA METHOD (tidak berubah) ====================
    private void assignRoles(List<Player> players) {
        int impostorCount = plugin.getConfig().getInt("roles.impostor-count", 1);
        boolean jokerEnabled = plugin.getConfig().getBoolean("roles.joker-enabled", true);
        List<UUID> ids = new ArrayList<>();
        players.forEach(p -> ids.add(p.getUniqueId()));
        Collections.shuffle(ids);
        for (int i = 0; i < impostorCount && i < ids.size(); i++) {
            playerRoles.put(ids.get(i), RoleRegistry.Role.IMPOSTOR);
        }
        if (jokerEnabled && ids.size() > impostorCount + 1) {
            playerRoles.put(ids.get(impostorCount), RoleRegistry.Role.JOKER);
        }
        for (UUID id : ids) {
            if (!playerRoles.containsKey(id)) playerRoles.put(id, RoleRegistry.Role.CREWMATE);
        }
    }

    public void celebrateVictory(String winnerMessage) {
        if (state == GameState.GAME_END) return;
        state = GameState.GAME_END;

        // ★ VOICE CHAT: Unmute semua pemain sebelum game berakhir
        plugin.getSabotageManager().unmuteAllAlive();

        plugin.getTaskManager().cancelAllActiveTasks();
        // Reset semua fake task progress
        plugin.getFakeTaskBook().resetAll();

        plugin.getSabotageManager().resetSkinWalkerIfActive();
        plugin.getSabotageManager().resetCooldowns();
        if (plugin.getSabotageManager().isSabotaging()) {
            plugin.getSabotageManager().fixSabotage(null);
        }

        removeAllCorpses();

        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getSkinWalker().restoreOriginalSkin(p);
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (!target.getUniqueId().equals(p.getUniqueId())) {
                    target.hidePlayer(plugin, p);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (p.isOnline() && target.isOnline()) {
                            target.showPlayer(plugin, p);
                        }
                    }, 1L);
                }
            }
            Location currentLoc = p.getLocation();
            p.teleport(currentLoc.add(0, 0.1, 0));
        }

        broadcast(ChatColor.DARK_RED + winnerMessage);
        playSoundToAll(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        playSoundToAll(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

        for (Player p : Bukkit.getOnlinePlayers()) {
            spawnFirework(p.getLocation());
        }

        Bukkit.getScheduler().runTaskLater(plugin, this::cleanupAfterGame, 20L);
    }

    private void spawnFirework(Location loc) {
        try {
            Firework fw = loc.getWorld().spawn(loc, Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .withColor(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .build());
            meta.setPower(0);
            fw.setFireworkMeta(meta);
            Bukkit.getScheduler().runTaskLater(plugin, fw::remove, 40L);
        } catch (Exception e) {
            if (loc.getWorld() != null) {
                loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1);
            }
        }
    }

    private void cleanupAfterGame() {
        removeAllCorpses();
        plugin.getBridgeManager().resetBridge();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().clear();
            p.getInventory().setHelmet(null);
            p.getInventory().setChestplate(null);
            p.getInventory().setLeggings(null);
            p.getInventory().setBoots(null);

            p.setGameMode(GameMode.ADVENTURE);
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setWalkSpeed(0.2f);
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            applySaturation(p);

            for (Player other : Bukkit.getOnlinePlayers()) {
                p.showPlayer(plugin, other);
            }
        }
        teleportToLobbyCircle();
        plugin.getScoreboardManager().updateAll();

        state = GameState.WAITING;
        livingPlayers.clear();
        deadPlayers.clear();
        playerRoles.clear();
        emergencyLastUsed = 0;
        killCooldown.clear();
        plugin.getTaskManager().resetTasks();
        plugin.getCorpseGlowManager().clearAllCorpses();

        // ★ VOICE CHAT: Unmute semua pemain setelah cleanup
        plugin.getSabotageManager().unmuteAllAlive();
    }

    public void handlePlayerJoin(Player player) {
        if (state == GameState.WAITING || state == GameState.STARTING) {
            player.sendMessage(ChatColor.GRAY + "Memverifikasi integritas GameTag skin premium Anda...");
            plugin.getSkinWalker().autoDetectAndSaveSkin(player);

            player.setGameMode(GameMode.ADVENTURE);
            if (lobbyLocation != null) {
                player.teleport(lobbyLocation);
            }
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            if (meetingCenter != null) {
                player.teleport(meetingCenter);
                player.sendMessage(ChatColor.RED + "Game sedang berjalan! Kamu otomatis menjadi Spectator di area Meeting Center.");
            } else if (mapSpawn != null) {
                player.teleport(mapSpawn);
            }
        }
        plugin.getScoreboardManager().updateAll();
    }

    public void killPlayer(Player victim, Player killer, boolean createCorpse) {
        if (state != GameState.GAME_RUNNING && state != GameState.MEETING) return;
        if (!livingPlayers.contains(victim.getUniqueId())) return;

        Location deathLoc = victim.getLocation().clone();

        RoleRegistry.Role role = getRole(victim);
        livingPlayers.remove(victim.getUniqueId());
        deadPlayers.add(victim.getUniqueId());

        if (role == RoleRegistry.Role.CREWMATE) {
            victim.setGameMode(GameMode.SPECTATOR);
            // ★ PERUBAHAN: Hapus pembagian Oak Planks saat mati
            // distributePlanksToCrewmates(victim, 8);
        } else {
            victim.setGameMode(GameMode.SPECTATOR);
        }
        victim.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));

        // ★ PERUBAHAN: Hapus teleport ke meeting center – biarkan mayat di tempat
        // if (meetingCenter != null) {
        //     victim.teleport(meetingCenter);
        // }

        if (createCorpse) {
            ArmorStand corpse = deathLoc.getWorld().spawn(deathLoc, ArmorStand.class);
            corpse.setHelmet(victim.getInventory().getHelmet());
            corpse.setCustomName(ChatColor.RED + victim.getName() + "'s Corpse");
            corpse.setCustomNameVisible(true);
            corpse.setMarker(false);
            corpse.setGravity(false);
            corpse.setInvulnerable(true);
            plugin.getCorpseGlowManager().addCorpse(deathLoc);
        }

        // ★ PERUBAHAN: Hapus broadcast publik "X telah mati!"
        // broadcast(ChatColor.RED + victim.getName() + " telah mati!");

        // ★ Notifikasi ke Joker (TETAP ADA) ★
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (getRole(p) == RoleRegistry.Role.JOKER && isPlayerAlive(p)) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + "☠ " + victim.getName() + " telah mati di koordinat: "
                        + deathLoc.getBlockX() + ", " + deathLoc.getBlockY() + ", " + deathLoc.getBlockZ());
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_DEATH, 0.5f, 0.8f);
            }
        }

        playSoundToAll(Sound.ENTITY_PLAYER_DEATH, 1.0f, 0.8f);
        plugin.getScoreboardManager().updateAll();
        checkGameOver();
    }

    public void killPlayer(Player victim, Player killer) {
        killPlayer(victim, killer, true);
    }

    public void handleDisconnect(Player player) {
        if (state != GameState.GAME_RUNNING && state != GameState.MEETING) return;
        if (!livingPlayers.contains(player.getUniqueId())) return;
        killPlayer(player, null, false);
    }

    public void checkGameOver() {
        long aliveCrew = livingPlayers.stream().filter(uid -> {
            RoleRegistry.Role r = playerRoles.get(uid);
            return r != null && r == RoleRegistry.Role.CREWMATE;
        }).count();
        long aliveImpostor = livingPlayers.stream().filter(uid -> {
            RoleRegistry.Role r = playerRoles.get(uid);
            return r != null && r == RoleRegistry.Role.IMPOSTOR;
        }).count();
        if (aliveCrew == 0 && aliveImpostor > 0) {
            celebrateVictory("Impostor menang! Semua crewmate mati!");
        } else if (aliveImpostor == 0 && aliveCrew > 0) {
            celebrateVictory("Crewmate menang! Semua impostor mati!");
        }
    }

    public void removeAllCorpses() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (e instanceof ArmorStand as && as.getCustomName() != null && as.getCustomName().endsWith("Corpse")) {
                    e.remove();
                }
            }
        }
        plugin.getCorpseGlowManager().clearAllCorpses();
    }

    public void startMeeting(Player caller, String reason) {
        if (state != GameState.GAME_RUNNING) return;
        if (plugin.getSabotageManager().isSabotaging()) {
            caller.sendMessage(ChatColor.RED + "Tidak bisa meeting saat lampu mati! Perbaiki sabotase dulu.");
            return;
        }

        if (plugin.getSabotageManager().isSkinWalkerActive()) {
            plugin.getSabotageManager().pauseSkinWalker();
        }

        state = GameState.MEETING;

        // ★ Batalkan semua task saat meeting dimulai
        plugin.getTaskManager().cancelAllTasksForAllPlayers();

        // ★ HAPUS SEMUA CORPSE AGAR TIDAK MENGANGGU MEETING
        removeAllCorpses();

        broadcast(ChatColor.AQUA + "Meeting dimulai! " + reason);
        playSoundToAll(Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 0.5f);

        // ★ TELEPORT SEMUA PLAYER KE MEETING CENTER
        plugin.getVotingSystem().teleportToMeetingCircle();

        // ★ MULAI SESSI VOTING
        plugin.getVotingSystem().startVotingSession();
        plugin.getScoreboardManager().updateAll();
    }

    public void endMeeting() {
        if (state != GameState.MEETING) return;
        state = GameState.GAME_RUNNING;
        if (plugin.getSabotageManager().isSkinWalkerPaused()) {
            plugin.getSabotageManager().resumeSkinWalker();
        }
        removeAllCorpses();
        plugin.getScoreboardManager().updateAll();

        // ★ Berikan task baru untuk semua player yang masih hidup setelah meeting
        for (UUID uid : livingPlayers) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null && p.isOnline()) {
                plugin.getTaskManager().assignInitialTask(p);
            }
        }
    }

    public RoleRegistry.Role getRole(Player p) { return playerRoles.get(p.getUniqueId()); }
    public boolean isPlayerAlive(Player p) { return livingPlayers.contains(p.getUniqueId()); }
    public Set<UUID> getLivingPlayers() { return Collections.unmodifiableSet(livingPlayers); }

    public void setLobbyLocation(Location loc) {
        this.lobbyLocation = loc;
        saveLocationAsync("lobby.location", loc);
        plugin.spawnCreditsHologram();
        plugin.getScoreboardManager().updateAll();
    }
    public void setMapSpawn(Location loc) {
        this.mapSpawn = loc;
        saveLocationAsync("map-spawn.location", loc);
        plugin.getScoreboardManager().updateAll();
    }
    public void setMeetingCenter(Location loc) {
        this.meetingCenter = loc;
        saveLocationAsync("meeting-center.location", loc);
        plugin.getScoreboardManager().updateAll();
    }
    public Location getLobbyLocation() { return lobbyLocation; }
    public Location getMeetingCenter() { return meetingCenter; }
    public Location getMapSpawn() { return mapSpawn; }

    private void broadcast(String msg) { Bukkit.broadcastMessage(msg); }
}