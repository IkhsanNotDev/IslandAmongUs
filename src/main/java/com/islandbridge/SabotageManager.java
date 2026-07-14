package com.islandbridge;

import com.islandbridge.utils.VoiceChatUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class SabotageManager {
    private final IslandBridgeAmongUs plugin;
    private final Set<Location> lampLocations = new HashSet<>();
    private Location generatorLocation;
    private boolean sabotaging = false;
    private boolean skinSabotageActive = false;
    private boolean skinSabotagePaused = false;

    private long lampCooldownSeconds = 90;
    private long skinWalkerCooldownSeconds = 150;
    private int blindDurationTicks = 100;

    private final Map<UUID, Long> sabotageCooldown = new HashMap<>();
    private final Map<UUID, Long> skinWalkerCooldown = new HashMap<>();

    private final Map<UUID, String> originalTeamNames = new HashMap<>();
    private BukkitTask skinWalkerTask = null;
    private int skinWalkerTimeLeft = 0;

    private BukkitTask fixTask = null;
    private Player fixingPlayer = null;

    private int nametagHideCounter = 0;

    public SabotageManager(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    // ======================= VOICE CHAT HELPERS =======================

    /**
     * Mute semua pemain yang masih hidup (untuk Skin Walker).
     */
    public void muteAllAlive() {
        if (!VoiceChatUtil.isVoiceChatEnabled()) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getGameManager().isPlayerAlive(p)) {
                VoiceChatUtil.mutePlayer(p);
            }
        }
    }

    /**
     * Unmute semua pemain yang masih hidup (kembalikan bicara).
     */
    public void unmuteAllAlive() {
        if (!VoiceChatUtil.isVoiceChatEnabled()) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getGameManager().isPlayerAlive(p)) {
                VoiceChatUtil.unmutePlayer(p);
            }
        }
    }

    // ======================= CONFIG / LOAD =======================

    public void loadConfig() {
        if (plugin.getConfig().contains("sabotage.lamps")) {
            for (String locStr : plugin.getConfig().getStringList("sabotage.lamps")) {
                Location loc = parseLocation(locStr);
                if (loc != null) lampLocations.add(loc);
            }
        }
        if (plugin.getConfig().contains("sabotage.generator")) {
            String genStr = plugin.getConfig().getString("sabotage.generator");
            if (genStr != null && !genStr.isEmpty()) {
                generatorLocation = parseLocation(genStr);
            }
        }

        lampCooldownSeconds = plugin.getConfig().getLong("sabotage.lamp-cooldown-seconds", 90);
        skinWalkerCooldownSeconds = plugin.getConfig().getLong("sabotage.skinwalker-cooldown-seconds", 150);
        blindDurationTicks = plugin.getConfig().getInt("skinwalker-blind-duration-ticks", 100);
        if (blindDurationTicks < 0) blindDurationTicks = 0;
    }

    private void saveConfig() {
        List<String> lampList = new ArrayList<>();
        for (Location loc : lampLocations) {
            lampList.add(locToString(loc));
        }
        plugin.getConfig().set("sabotage.lamps", lampList);
        if (generatorLocation != null) {
            plugin.getConfig().set("sabotage.generator", locToString(generatorLocation));
        } else {
            plugin.getConfig().set("sabotage.generator", null);
        }
        plugin.getConfig().set("sabotage.lamp-cooldown-seconds", lampCooldownSeconds);
        plugin.getConfig().set("sabotage.skinwalker-cooldown-seconds", skinWalkerCooldownSeconds);
        plugin.getConfig().set("skinwalker-blind-duration-ticks", blindDurationTicks);
        plugin.saveConfig();
    }

    private String locToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location parseLocation(String str) {
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

    // ======================= AUTO DETECT LAMPU =======================
    public int autoDetectLamps(Player player, int radius) {
        int maxRadius = plugin.getConfig().getInt("sabotage.max-autodetect-radius", 200);
        if (radius > maxRadius) {
            player.sendMessage(ChatColor.RED + "Radius terlalu besar! Maksimal " + maxRadius + " block.");
            player.sendMessage(ChatColor.YELLOW + "Kamu bisa mengubah batas di config.yml: sabotage.max-autodetect-radius");
            return 0;
        }
        if (radius <= 0) {
            player.sendMessage(ChatColor.RED + "Radius harus lebih besar dari 0.");
            return 0;
        }

        player.sendMessage(ChatColor.YELLOW + "Memindai lampu redstone dalam radius " + radius + "...");

        World world = player.getWorld();
        Location center = player.getLocation();
        int found = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = center.clone().add(x, y, z);
                    if (!world.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) continue;
                    Block block = loc.getBlock();
                    if (block.getType() == Material.REDSTONE_LAMP) {
                        if (!lampLocations.contains(loc)) {
                            lampLocations.add(loc);
                            if (block.getBlockData() instanceof Lightable lightable) {
                                lightable.setLit(true);
                                block.setBlockData(lightable);
                            }
                            found++;
                        }
                    }
                }
            }
        }

        if (found > 0) {
            saveConfig();
            player.sendMessage(ChatColor.GREEN + "Berhasil menambahkan " + found + " lampu redstone ke daftar sabotase.");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Tidak ditemukan REDSTONE_LAMP baru dalam radius " + radius + " block.");
        }
        return found;
    }

    public void addLamp(Location loc) {
        if (!lampLocations.contains(loc)) {
            lampLocations.add(loc);
            Block block = loc.getBlock();
            block.setType(Material.REDSTONE_LAMP);
            if (block.getBlockData() instanceof Lightable lightable) {
                lightable.setLit(true);
                block.setBlockData(lightable);
            }
            saveConfig();
        }
    }

    public void setGenerator(Location loc) {
        if (generatorLocation != null) {
            generatorLocation.getBlock().setType(Material.AIR);
        }
        generatorLocation = loc.clone();
        generatorLocation.getBlock().setType(Material.LEVER);
        saveConfig();
    }

    // Getter/Setter
    public long getLampCooldownSeconds() { return lampCooldownSeconds; }
    public void setLampCooldownSeconds(long seconds) { this.lampCooldownSeconds = seconds; saveConfig(); }
    public long getSkinWalkerCooldownSeconds() { return skinWalkerCooldownSeconds; }
    public void setSkinWalkerCooldownSeconds(long seconds) { this.skinWalkerCooldownSeconds = seconds; saveConfig(); }
    public int getBlindDurationTicks() { return blindDurationTicks; }
    public void setBlindDurationTicks(int ticks) { this.blindDurationTicks = ticks; saveConfig(); }

    // Cooldown
    public boolean canSabotage(Player player) {
        Long lastUsed = sabotageCooldown.get(player.getUniqueId());
        if (lastUsed == null) return true;
        long now = System.currentTimeMillis() / 1000;
        return (now - lastUsed) >= lampCooldownSeconds;
    }
    public long getSabotageRemaining(Player player) {
        Long lastUsed = sabotageCooldown.get(player.getUniqueId());
        if (lastUsed == null) return 0;
        long now = System.currentTimeMillis() / 1000;
        return Math.max(0, lampCooldownSeconds - (now - lastUsed));
    }
    public void setSabotageCooldown(Player player) {
        sabotageCooldown.put(player.getUniqueId(), System.currentTimeMillis() / 1000);
    }

    public boolean canSkinWalker(Player player) {
        Long lastUsed = skinWalkerCooldown.get(player.getUniqueId());
        if (lastUsed == null) return true;
        long now = System.currentTimeMillis() / 1000;
        return (now - lastUsed) >= skinWalkerCooldownSeconds;
    }
    public long getSkinWalkerRemaining(Player player) {
        Long lastUsed = skinWalkerCooldown.get(player.getUniqueId());
        if (lastUsed == null) return 0;
        long now = System.currentTimeMillis() / 1000;
        return Math.max(0, skinWalkerCooldownSeconds - (now - lastUsed));
    }
    public void setSkinWalkerCooldown(Player player) {
        skinWalkerCooldown.put(player.getUniqueId(), System.currentTimeMillis() / 1000);
    }
    public void resetCooldowns() {
        sabotageCooldown.clear();
        skinWalkerCooldown.clear();
    }

    public void handlePlayerRejoin(Player player) {
        GameManager.GameState state = plugin.getGameManager().getState();
        if (state == GameManager.GameState.GAME_RUNNING || state == GameManager.GameState.MEETING) {
            plugin.getGameManager().killPlayer(player, null, false);
            player.setGameMode(GameMode.SURVIVAL);
            if (plugin.getGameManager().getLobbyLocation() != null) {
                player.teleport(plugin.getGameManager().getLobbyLocation());
            }
            player.sendMessage(ChatColor.RED + "Kamu otomatis di-eliminasi karena re-login saat permainan berlangsung!");
        }
    }

    private void applyBlindToNonImpostors(int durationTicks) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!plugin.getGameManager().isPlayerAlive(p)) continue;
            RoleRegistry.Role role = plugin.getGameManager().getRole(p);
            if (role != RoleRegistry.Role.IMPOSTOR) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, durationTicks, 0, false, false));
            }
        }
    }

    // ======================= NAMETAG HIDE / RESTORE (Counter-based) =======================
    private void addNametagHide() {
        if (nametagHideCounter == 0) {
            originalTeamNames.clear();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (plugin.getGameManager().isPlayerAlive(p)) {
                    Team currentTeam = p.getScoreboard().getEntryTeam(p.getName());
                    if (currentTeam != null) {
                        originalTeamNames.put(p.getUniqueId(), currentTeam.getName());
                    }
                }
            }
            for (Player observer : Bukkit.getOnlinePlayers()) {
                Scoreboard board = observer.getScoreboard();
                Team hideTeam = board.getTeam("hideNametag");
                if (hideTeam == null) {
                    hideTeam = board.registerNewTeam("hideNametag");
                    hideTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (plugin.getGameManager().isPlayerAlive(p)) {
                        hideTeam.addEntry(p.getName());
                    }
                }
            }
        }
        nametagHideCounter++;
    }

    private void removeNametagHide() {
        if (nametagHideCounter <= 0) return;
        nametagHideCounter--;
        if (nametagHideCounter == 0) {
            for (Player observer : Bukkit.getOnlinePlayers()) {
                Scoreboard board = observer.getScoreboard();
                Team hideTeam = board.getTeam("hideNametag");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    String origTeamName = originalTeamNames.get(p.getUniqueId());
                    if (origTeamName != null) {
                        Team original = board.getTeam(origTeamName);
                        if (original != null) {
                            original.addEntry(p.getName());
                        }
                    } else {
                        if (hideTeam != null) {
                            hideTeam.removeEntry(p.getName());
                        }
                    }
                }
            }
            originalTeamNames.clear();
        }
    }

    // ======================= SKIN WALKER =======================
    public void sabotageSkinWalker(Player player) {
        if (skinSabotageActive || skinSabotagePaused) return;
        if (plugin.getGameManager().getState() != GameManager.GameState.GAME_RUNNING) return;
        if (!canSkinWalker(player)) {
            long remaining = getSkinWalkerRemaining(player);
            player.sendMessage(ChatColor.RED + "Skin Walker cooldown! Tunggu " + remaining + " detik.");
            return;
        }
        setSkinWalkerCooldown(player);
        startSkinWalkerEffect();
    }

    private void startSkinWalkerEffect() {
        // MUTE semua pemain hidup (Skin Walker aktif)
        muteAllAlive();

        // Simpan skin asli dan terapkan skin acak
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getSkinWalker().saveOriginalSkin(p);
        }
        plugin.getSkinWalker().applyRandomSkinSwap();
        applyBlindToNonImpostors(blindDurationTicks);

        // Sembunyikan nametag
        addNametagHide();

        skinWalkerTimeLeft = 60;
        runSkinWalkerTask();

        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "⚡ Sabotase Skin Walker aktif! Skin diacak dan nametag disembunyikan!");
        plugin.getScoreboardManager().updateAll();
    }

    private void runSkinWalkerTask() {
        skinSabotageActive = true;
        skinSabotagePaused = false;

        skinWalkerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!skinSabotageActive || skinSabotagePaused) {
                    cancel();
                    return;
                }

                if (skinWalkerTimeLeft == 5) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                        p.sendActionBar(ChatColor.YELLOW + "Skin Walker berakhir dalam " + skinWalkerTimeLeft + " detik!");
                    }
                } else if (skinWalkerTimeLeft <= 5 && skinWalkerTimeLeft > 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, 1.0f);
                    }
                }

                skinWalkerTimeLeft--;

                if (skinWalkerTimeLeft <= 0) {
                    cancel();
                    endSkinWalkerNaturally();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void pauseSkinWalker() {
        if (!skinSabotageActive || skinSabotagePaused) return;
        if (skinWalkerTask != null) {
            skinWalkerTask.cancel();
            skinWalkerTask = null;
        }

        // Kembalikan skin asli
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getSkinWalker().restoreOriginalSkin(p);
        }
        applyBlindToNonImpostors(blindDurationTicks);

        // UNMUTE semua pemain hidup (karena meeting, semua boleh bicara)
        unmuteAllAlive();

        // Jangan hapus nametag hide (tetap tersembunyi)
        skinSabotageActive = false;
        skinSabotagePaused = true;
        Bukkit.broadcastMessage(ChatColor.YELLOW + "⚡ Sabotase Skin Walker DIJEDA pada sisa " + skinWalkerTimeLeft + " detik!");
        plugin.getScoreboardManager().updateAll();
    }

    public void resumeSkinWalker() {
        if (!skinSabotagePaused) return;

        // Simpan skin dan terapkan acak
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getSkinWalker().saveOriginalSkin(p);
        }
        plugin.getSkinWalker().applyRandomSkinSwap();
        applyBlindToNonImpostors(blindDurationTicks);

        // MUTE semua pemain hidup (karena skin walker dilanjutkan)
        muteAllAlive();

        // Pastikan nametag tetap tersembunyi
        if (nametagHideCounter == 0) {
            addNametagHide();
        }

        runSkinWalkerTask();
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "⚡ Sabotase Skin Walker DILANJUTKAN! Sisa waktu: " + skinWalkerTimeLeft + " detik.");
        plugin.getScoreboardManager().updateAll();
    }

    private void endSkinWalkerNaturally() {
        // Kembalikan skin asli
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getSkinWalker().restoreOriginalSkin(p);
        }
        applyBlindToNonImpostors(blindDurationTicks);

        // UNMUTE semua pemain hidup (skin walker selesai)
        unmuteAllAlive();

        // Hapus kontribusi nametag hide
        removeNametagHide();

        Bukkit.broadcastMessage(ChatColor.GREEN + "Sabotase Skin Walker berakhir. Skin dinormalkan.");
        skinSabotageActive = false;
        skinSabotagePaused = false;
        skinWalkerTimeLeft = 0;
        skinWalkerTask = null;
        plugin.getScoreboardManager().updateAll();
    }

    public void resetSkinWalkerIfActive() {
        if (skinSabotageActive || skinSabotagePaused) {
            if (skinWalkerTask != null) {
                skinWalkerTask.cancel();
                skinWalkerTask = null;
            }
            // Kembalikan skin
            for (Player p : Bukkit.getOnlinePlayers()) {
                plugin.getSkinWalker().restoreOriginalSkin(p);
            }
            // UNMUTE semua
            unmuteAllAlive();
            // Hapus nametag hide
            removeNametagHide();

            skinSabotageActive = false;
            skinSabotagePaused = false;
            skinWalkerTimeLeft = 0;
            plugin.getScoreboardManager().updateAll();
        }
    }

    // ======================= LAMP SABOTAGE =======================
    public void sabotageLights(Player player) {
        if (sabotaging) return;
        if (plugin.getGameManager().getState() != GameManager.GameState.GAME_RUNNING) return;
        if (!canSabotage(player)) {
            long remaining = getSabotageRemaining(player);
            player.sendMessage(ChatColor.RED + "Sabotase lampu cooldown! Tunggu " + remaining + " detik.");
            return;
        }
        setSabotageCooldown(player);
        sabotaging = true;

        // Matikan semua lampu
        for (Location loc : lampLocations) {
            Block block = loc.getBlock();
            if (block.getType() == Material.REDSTONE_LAMP) {
                if (block.getBlockData() instanceof Lightable lightable) {
                    lightable.setLit(false);
                    block.setBlockData(lightable);
                }
            }
        }

        // Efek ke player
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getGameManager().isPlayerAlive(p)) {
                if (plugin.getGameManager().getRole(p) == RoleRegistry.Role.IMPOSTOR) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));
                } else {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, Integer.MAX_VALUE, 255, false, false));
                }
            } else {
                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));
            }
            p.sendMessage(ChatColor.DARK_RED + "⚡ Sabotase lampu aktif!");
            if (plugin.getGameManager().isPlayerAlive(p) && generatorLocation != null) {
                p.sendMessage(ChatColor.GOLD + "📍 Lokasi Generator: " + generatorLocation.getBlockX() + ", " + generatorLocation.getBlockY() + ", " + generatorLocation.getBlockZ());
            }
        }

        // Sembunyikan nametag (lampu juga menyembunyikan)
        addNametagHide();

        plugin.getScoreboardManager().updateAll();
    }

    public void startFixSabotage(Player player) {
        if (!sabotaging) return;
        if (fixTask != null) {
            player.sendMessage(ChatColor.RED + "Generator sedang diperbaiki oleh pemain lain, tunggu!");
            return;
        }
        fixingPlayer = player;
        player.sendMessage(ChatColor.YELLOW + "Memperbaiki generator... jangan bergerak selama 20 detik!");
        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 0.5f);

        final Location startLoc = player.getLocation().clone();
        fixTask = new BukkitRunnable() {
            int time = 20;
            @Override
            public void run() {
                if (!sabotaging) {
                    cancelFix();
                    return;
                }
                if (player.getLocation().distance(startLoc) > 1.5) {
                    player.sendMessage(ChatColor.RED + "Perbaikan gagal! Kamu bergerak terlalu jauh.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    cancelFix();
                    return;
                }
                if (time <= 0) {
                    fixSabotage(player);
                    cancelFix();
                } else {
                    player.sendActionBar(ChatColor.AQUA + "Memperbaiki generator... " + time + " detik tersisa");
                    if (time == 5 || time == 10 || time == 15) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);
                    }
                    time--;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void cancelFix() {
        if (fixTask != null) {
            fixTask.cancel();
            fixTask = null;
        }
        fixingPlayer = null;
    }

    public void fixSabotage(Player player) {
        if (!sabotaging) return;
        cancelFix();

        // Nyalakan semua lampu
        for (Location loc : lampLocations) {
            Block block = loc.getBlock();
            if (block.getType() == Material.REDSTONE_LAMP) {
                if (block.getBlockData() instanceof Lightable lightable) {
                    lightable.setLit(true);
                    block.setBlockData(lightable);
                }
            }
        }

        // Hapus efek potion
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.DARKNESS);
            p.removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.sendMessage(ChatColor.GREEN + "💡 Lampu berhasil dinyalakan kembali.");
        }

        // Hapus kontribusi nametag dari lampu
        removeNametagHide();

        sabotaging = false;
        plugin.getScoreboardManager().updateAll();
        if (player != null) {
            player.sendMessage(ChatColor.GREEN + "Generator berhasil diperbaiki!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    // ======================= GETTER =======================
    public boolean isSabotaging() { return sabotaging; }
    public boolean isSkinWalkerActive() { return skinSabotageActive || skinSabotagePaused; }
    public boolean isSkinWalkerPaused() { return skinSabotagePaused; }
    public Location getGeneratorLocation() { return generatorLocation; }
}