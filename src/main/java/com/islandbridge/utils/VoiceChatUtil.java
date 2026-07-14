package com.islandbridge.utils;

import com.islandbridge.IslandBridgeAmongUs;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * Utility untuk integrasi Simple Voice Chat.
 * Mengatur grup otomatis berdasarkan status hidup/mati pemain.
 *
 * Fitur:
 * - Pemain hidup (alive) → grup "alive" (ISOLATED) → hanya mendengar sesama hidup.
 * - Pemain mati (ghost) → grup "ghost" (NORMAL) → mendengar semua, tapi tidak terdengar oleh hidup.
 * - Mute/Unmute individual.
 */
public class VoiceChatUtil {

    private static IslandBridgeAmongUs plugin;
    private static boolean voiceChatEnabled = false;
    private static VoicechatApi voicechatApi = null;

    /**
     * Panggil di onEnable() untuk mendeteksi plugin Voice Chat.
     */
    public static void init(IslandBridgeAmongUs pluginInstance) {
        plugin = pluginInstance;
        if (Bukkit.getPluginManager().getPlugin("voicechat") != null) {
            BukkitVoicechatService service = Bukkit.getServicesManager().load(BukkitVoicechatService.class);
            if (service != null) {
                try {
                    service.registerPlugin(new VoiceChatPluginImpl());
                    voiceChatEnabled = true;
                    plugin.getLogger().info("Simple Voice Chat integration registered successfully.");
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to register Simple Voice Chat: " + e.getMessage());
                }
            } else {
                plugin.getLogger().warning("BukkitVoicechatService not found. Voice chat disabled.");
            }
        } else {
            plugin.getLogger().info("No voice chat plugin found. Using text chat only.");
        }
    }

    public static boolean isVoiceChatEnabled() {
        return voiceChatEnabled;
    }

    // ======================= GRUP MANAGEMENT =======================

    private static VoicechatServerApi getServerApi() {
        if (!voiceChatEnabled) return null;
        if (voicechatApi instanceof VoicechatServerApi) {
            return (VoicechatServerApi) voicechatApi;
        }
        if (voicechatApi == null) {
            plugin.getLogger().warning("VoicechatApi is null! Make sure VoiceChatPlugin is initialized.");
        } else {
            plugin.getLogger().warning("VoicechatApi is not a VoicechatServerApi instance.");
        }
        return null;
    }

    /**
     * Dapatkan grup berdasarkan nama, buat jika belum ada.
     */
    private static Group getOrCreateGroup(String name, Group.Type type, String password) {
        VoicechatServerApi api = getServerApi();
        if (api == null) return null;

        try {
            // Cari grup yang sudah ada - menggunakan getGroupManager()
            Iterable<Group> groups = null;
            try {
                // Coba panggil getGroupManager() via reflection jika method tidak ada di interface
                Method getGroupManager = api.getClass().getMethod("getGroupManager");
                Object groupManager = getGroupManager.invoke(api);
                Method getGroups = groupManager.getClass().getMethod("getGroups");
                groups = (Iterable<Group>) getGroups.invoke(groupManager);
            } catch (NoSuchMethodException e) {
                // Jika getGroupManager tidak ada, coba langsung getGroups dari api
                try {
                    Method getGroups = api.getClass().getMethod("getGroups");
                    groups = (Iterable<Group>) getGroups.invoke(api);
                } catch (NoSuchMethodException ex) {
                    plugin.getLogger().warning("Method getGroups or getGroupManager not found. Trying fallback.");
                }
            }

            if (groups != null) {
                for (Group g : groups) {
                    if (g.getName().equalsIgnoreCase(name)) {
                        return g;
                    }
                }
            }

            // Buat grup baru - menggunakan groupBuilder()
            try {
                Method groupBuilder = api.getClass().getMethod("groupBuilder");
                Object builder = groupBuilder.invoke(api);
                Method setName = builder.getClass().getMethod("setName", String.class);
                Method setPassword = builder.getClass().getMethod("setPassword", String.class);
                Method setType = builder.getClass().getMethod("setType", Group.Type.class);
                Method build = builder.getClass().getMethod("build");

                setName.invoke(builder, name);
                setPassword.invoke(builder, password);
                setType.invoke(builder, type);
                return (Group) build.invoke(builder);
            } catch (NoSuchMethodException e) {
                plugin.getLogger().warning("groupBuilder method not found. Cannot create group.");
                return null;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get or create group '" + name + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Pindahkan pemain ke grup "alive" (ISOLATED) – hanya mendengar sesama alive.
     */
    public static void setPlayerAlive(Player player) {
        if (!voiceChatEnabled || player == null) return;
        VoicechatServerApi api = getServerApi();
        if (api == null) return;

        VoicechatConnection connection = api.getConnectionOf(player.getUniqueId());
        if (connection == null) {
            plugin.getLogger().fine("Voice: No connection for " + player.getName());
            return;
        }

        Group aliveGroup = getOrCreateGroup("alive", Group.Type.ISOLATED, null);
        if (aliveGroup != null) {
            try {
                connection.setGroup(aliveGroup);
                plugin.getLogger().fine("Voice: " + player.getName() + " → alive group");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to set group for " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Pindahkan pemain ke grup "ghost" (NORMAL) – mendengar semua, tapi suaranya tidak terdengar oleh alive.
     */
    public static void setPlayerDead(Player player) {
        if (!voiceChatEnabled || player == null) return;
        VoicechatServerApi api = getServerApi();
        if (api == null) return;

        VoicechatConnection connection = api.getConnectionOf(player.getUniqueId());
        if (connection == null) {
            plugin.getLogger().fine("Voice: No connection for " + player.getName());
            return;
        }

        Group ghostGroup = getOrCreateGroup("ghost", Group.Type.NORMAL, null);
        if (ghostGroup != null) {
            try {
                connection.setGroup(ghostGroup);
                plugin.getLogger().fine("Voice: " + player.getName() + " → ghost group");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to set group for " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Mute server-side (pemain tidak bisa bicara sama sekali).
     */
    public static void mutePlayer(Player player) {
        if (!voiceChatEnabled || player == null) return;
        VoicechatServerApi api = getServerApi();
        if (api == null) return;

        VoicechatConnection connection = api.getConnectionOf(player.getUniqueId());
        if (connection != null) {
            try {
                Method setMuted = connection.getClass().getMethod("setMuted", boolean.class);
                setMuted.invoke(connection, true);
                plugin.getLogger().fine("Voice: " + player.getName() + " muted");
            } catch (NoSuchMethodException e) {
                plugin.getLogger().warning("setMuted method not found. Cannot mute player.");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to mute " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Unmute server-side.
     */
    public static void unmutePlayer(Player player) {
        if (!voiceChatEnabled || player == null) return;
        VoicechatServerApi api = getServerApi();
        if (api == null) return;

        VoicechatConnection connection = api.getConnectionOf(player.getUniqueId());
        if (connection != null) {
            try {
                Method setMuted = connection.getClass().getMethod("setMuted", boolean.class);
                setMuted.invoke(connection, false);
                plugin.getLogger().fine("Voice: " + player.getName() + " unmuted");
            } catch (NoSuchMethodException e) {
                plugin.getLogger().warning("setMuted method not found. Cannot unmute player.");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to unmute " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    // ======================= PLUGIN IMPLEMENTATION =======================
    /**
     * Implementasi VoicechatPlugin yang didaftarkan ke BukkitVoicechatService.
     */
    private static class VoiceChatPluginImpl implements VoicechatPlugin {

        @Override
        public String getPluginId() {
            return "islandbridge_voicechat";
        }

        @Override
        public void initialize(VoicechatApi api) {
            voicechatApi = api;
            if (plugin != null) {
                plugin.getLogger().info("VoiceChatAddon initialized successfully.");
            }
        }

        // Tidak perlu registerEvents karena kita tidak pakai event khusus
    }
}