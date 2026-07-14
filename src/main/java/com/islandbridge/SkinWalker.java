package com.islandbridge;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class SkinWalker {
    private final IslandBridgeAmongUs plugin;
    public static final String DEFAULT_SKIN = "IkhsanNotDev";
    private final Map<UUID, String> originalSkins = new HashMap<>();
    private final Map<UUID, String> pendingSkinSaves = new HashMap<>();
    private boolean saveTaskScheduled = false;

    private static final String[] FALLBACK_SKINS = {
            "Dream", "Technoblade", "GeorgeNotFound", "Sapnap", "KarlJacobs",
            "BadBoyHalo", "Quackity", "TommyInnit", "Tubbo", "WilburSoot",
            "Philza", "Fundy", "Nihachu", "Ranboo", "Skeppy"
    };
    private final Random random = new Random();

    private static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    private final File skinFile;
    private YamlConfiguration skinConfig;

    public SkinWalker(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
        this.skinFile = new File(plugin.getDataFolder(), "skins.yml");
        loadSkinConfig();
    }

    private void loadSkinConfig() {
        if (!skinFile.exists()) {
            skinConfig = new YamlConfiguration();
            try {
                skinConfig.save(skinFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            skinConfig = YamlConfiguration.loadConfiguration(skinFile);
        }
    }

    // ======================= SIMPAN SKIN KE FILE TERPISAH (ASYNC) =======================
    private void saveSkinToConfig(UUID uuid, String skinName) {
        pendingSkinSaves.put(uuid, skinName);
        if (!saveTaskScheduled) {
            saveTaskScheduled = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    flushSkinSaves();
                }
            }.runTaskLaterAsynchronously(plugin, 40L); // 2 detik delay
        }
    }

    private void flushSkinSaves() {
        if (pendingSkinSaves.isEmpty()) {
            saveTaskScheduled = false;
            return;
        }
        Map<UUID, String> toSave = new HashMap<>(pendingSkinSaves);
        pendingSkinSaves.clear();
        saveTaskScheduled = false;

        for (Map.Entry<UUID, String> entry : toSave.entrySet()) {
            skinConfig.set(entry.getKey().toString(), entry.getValue());
        }
        // Simpan secara async menggunakan thread terpisah (tidak memblock main thread)
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                skinConfig.save(skinFile);
                plugin.getLogger().info("SkinWalker: Tersimpan " + toSave.size() + " perubahan skin.");
            } catch (IOException e) {
                plugin.getLogger().warning("Gagal menyimpan skin: " + e.getMessage());
            }
        });
    }

    // UTAMAKAN GAMETAG: Jika di config tidak ada atau bernilai default, pakai nama asli player (GameTag)
    public String getSavedSkinName(Player player) {
        String configSkin = skinConfig.getString(player.getUniqueId().toString());
        if (configSkin == null || configSkin.equalsIgnoreCase(DEFAULT_SKIN)) {
            return player.getName(); // Mengutamakan GameTag saat ini
        }
        return configSkin;
    }

    // ======================= DETEKSI & SIMPAN SKIN (ASYNC) =======================
    public void autoDetectAndSaveSkin(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String name = player.getName(); // GameTag Utama
                if (isPremium(name)) {
                    // Update & simpan langsung menggunakan nama aslinya (GameTag) ke database config
                    setSkinAndSave(player, name);
                    player.sendMessage(ChatColor.GREEN + "Akun premium terdeteksi! Skin GameTag disimpan ke database.");
                } else {
                    // Jika non-premium, cek apakah ada skin kustom yang pernah diset sebelumnya di config
                    String currentSkin = skinConfig.getString(player.getUniqueId().toString());
                    if (currentSkin == null) {
                        // Jika tidak ada data sama sekali, set default menggunakan GameTag-nya sendiri sebagai fallback awal
                        setSkinAndSave(player, name);
                    }
                    player.sendMessage(ChatColor.YELLOW + "Akun tidak premium, gunakan /skin <nama> jika ingin mengubah skin.");
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private boolean isPremium(String playerName) {
        try {
            String response = makeRequest(PROFILE_URL + playerName);
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ======================= ORIGINAL SKIN =======================
    public void saveOriginalSkin(Player player) {
        // Ambil data skin saat ini (Mengutamakan GameTag/Data tersimpan terbaru)
        String current = getSavedSkinName(player);
        originalSkins.put(player.getUniqueId(), current);
    }

    public void restoreOriginalSkin(Player player) {
        String original = originalSkins.remove(player.getUniqueId());
        if (original != null) {
            setSkin(player, original);
        } else {
            applySavedSkin(player);
        }
    }

    // ======================= RANDOM SKIN SWAP =======================
    public void applyRandomSkinSwap() {
        List<Player> alivePlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getGameManager().isPlayerAlive(p)) {
                alivePlayers.add(p);
            }
        }
        if (alivePlayers.size() < 2) return;

        List<String> skinNames = new ArrayList<>();
        for (Player p : alivePlayers) {
            skinNames.add(getSavedSkinName(p));
        }

        boolean valid = false;
        while (!valid) {
            Collections.shuffle(skinNames, random);
            valid = true;
            for (int i = 0; i < alivePlayers.size(); i++) {
                if (skinNames.get(i).equals(getSavedSkinName(alivePlayers.get(i)))) {
                    valid = false;
                    break;
                }
            }
        }

        for (int i = 0; i < alivePlayers.size(); i++) {
            Player p = alivePlayers.get(i);
            String newSkin = skinNames.get(i);
            setSkin(p, newSkin); // Menggunakan setSkin biasa agar tidak merusak config skins.yml
        }
    }

    // ======================= SET SKIN & SAVE (UNTUK COMMAND USER) =======================
    public void setSkinAndSave(Player target, String premiumTarget) {
        saveSkinToConfig(target.getUniqueId(), premiumTarget);
        setSkin(target, premiumTarget);
    }

    // ======================= SET SKIN (ASYNC) =======================
    public void setSkin(Player target, String premiumTarget) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String resolvedSkin = premiumTarget;
            try {
                String profileResponse = makeRequest(PROFILE_URL + premiumTarget);
                if (profileResponse == null || profileResponse.isEmpty()) {
                    resolvedSkin = FALLBACK_SKINS[random.nextInt(FALLBACK_SKINS.length)];
                    target.sendMessage(ChatColor.YELLOW + "Skin " + premiumTarget + " tidak ditemukan, menggunakan " + resolvedSkin);
                }
            } catch (Exception e) {
                resolvedSkin = FALLBACK_SKINS[random.nextInt(FALLBACK_SKINS.length)];
                target.sendMessage(ChatColor.YELLOW + "Gagal mengambil skin, menggunakan " + resolvedSkin);
            }

            final String finalSkin = resolvedSkin;
            try {
                String profileResponse = makeRequest(PROFILE_URL + finalSkin);
                if (profileResponse == null || profileResponse.isEmpty()) return;
                JsonObject profileObject = JsonParser.parseString(profileResponse).getAsJsonObject();
                String uuid = profileObject.get("id").getAsString();

                String skinResponse = makeRequest(String.format(SKIN_URL, uuid));
                if (skinResponse == null || skinResponse.isEmpty()) return;

                JsonObject skinObject = JsonParser.parseString(skinResponse).getAsJsonObject();
                JsonArray properties = skinObject.getAsJsonArray("properties");
                JsonObject textureProp = properties.get(0).getAsJsonObject();

                String value = textureProp.get("value").getAsString();
                String signature = textureProp.get("signature").getAsString();

                Bukkit.getScheduler().runTask(plugin, () -> {
                    PlayerProfile targetProfile = target.getPlayerProfile();
                    targetProfile.clearProperties();
                    targetProfile.setProperty(new ProfileProperty("textures", value, signature));
                    target.setPlayerProfile(targetProfile);
                    target.sendMessage(ChatColor.GREEN + "Skin berubah menjadi " + finalSkin);
                });

            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        target.sendMessage(ChatColor.RED + "Gagal mengambil skin untuk " + finalSkin)
                );
                e.printStackTrace();
            }
        });
    }

    public void applySavedSkin(Player player) {
        String skinName = getSavedSkinName(player);
        setSkin(player, skinName);
    }

    // ======================= HTTP REQUEST (DENGAN TIMEOUT) =======================
    private String makeRequest(String url) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            return null;
        }
    }
}