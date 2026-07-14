package com.islandbridge.utils;

import com.islandbridge.IslandBridgeAmongUs;
import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConfigExporter {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static boolean exportConfig(IslandBridgeAmongUs plugin, Player player) {
        File exportsDir = new File(plugin.getDataFolder(), "exports");
        if (!exportsDir.exists()) exportsDir.mkdirs();

        String fileName = "config_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".json";
        File exportFile = new File(exportsDir, fileName);

        JsonObject root = new JsonObject();

        // 1. Export config.yml
        JsonObject configObj = convertConfigToJson(plugin.getConfig());
        root.add("config", configObj);

        // 2. Export bridge_data.yml (via BridgeManager)
        JsonObject bridgeObj = new JsonObject();
        plugin.getBridgeManager().exportToJson(bridgeObj);
        root.add("bridge_data", bridgeObj);

        // 3. Export task data (via TaskManager)
        JsonObject taskObj = new JsonObject();
        plugin.getTaskManager().exportToJson(taskObj);
        root.add("task_data", taskObj);

        try (FileWriter writer = new FileWriter(exportFile)) {
            writer.write(gson.toJson(root));
            player.sendMessage("§aConfig berhasil diekspor ke: " + exportFile.getName());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage("§cGagal ekspor config: " + e.getMessage());
            return false;
        }
    }

    public static boolean importConfig(IslandBridgeAmongUs plugin, File importFile, Player player) {
        if (!importFile.exists()) {
            player.sendMessage("§cFile tidak ditemukan!");
            return false;
        }

        try (FileReader reader = new FileReader(importFile)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            // 1. Import config.yml
            if (root.has("config")) {
                JsonObject configObj = root.getAsJsonObject("config");
                importConfigToYaml(plugin, configObj);
                player.sendMessage("§aConfig.yml berhasil di-import.");
            } else {
                player.sendMessage("§eTidak ada objek 'config', lewati.");
            }

            // 2. Import bridge_data.yml (via BridgeManager)
            if (root.has("bridge_data")) {
                JsonObject bridgeObj = root.getAsJsonObject("bridge_data");
                plugin.getBridgeManager().importFromJson(bridgeObj);
                player.sendMessage("§aBridge data berhasil di-import.");
            } else {
                player.sendMessage("§eTidak ada objek 'bridge_data', lewati.");
            }

            // 3. Import task data (via TaskManager)
            if (root.has("task_data")) {
                JsonObject taskObj = root.getAsJsonObject("task_data");
                plugin.getTaskManager().importFromJson(taskObj);
                player.sendMessage("§aTask data berhasil di-import.");
            } else {
                player.sendMessage("§eTidak ada objek 'task_data', lewati.");
            }

            // Reload semua data
            plugin.reloadPluginConfig();
            plugin.getTaskManager().reloadAllNPCs();
            plugin.getBridgeManager().respawnNPC();

            player.sendMessage("§aImport selesai dari: " + importFile.getName());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("§cGagal import config: " + e.getMessage());
            return false;
        }
    }

    private static JsonObject convertConfigToJson(Configuration config) {
        JsonObject result = new JsonObject();
        for (String key : config.getKeys(false)) {
            result.add(key, convertObjectToJson(config.get(key)));
        }
        return result;
    }

    private static JsonElement convertObjectToJson(Object obj) {
        if (obj == null) return JsonNull.INSTANCE;
        if (obj instanceof String) return new JsonPrimitive((String) obj);
        if (obj instanceof Number) return new JsonPrimitive((Number) obj);
        if (obj instanceof Boolean) return new JsonPrimitive((Boolean) obj);
        if (obj instanceof List) {
            JsonArray arr = new JsonArray();
            for (Object o : (List<?>) obj) arr.add(convertObjectToJson(o));
            return arr;
        }
        if (obj instanceof Location) {
            Location loc = (Location) obj;
            JsonObject locObj = new JsonObject();
            locObj.addProperty("world", loc.getWorld().getName());
            locObj.addProperty("x", loc.getX());
            locObj.addProperty("y", loc.getY());
            locObj.addProperty("z", loc.getZ());
            locObj.addProperty("yaw", loc.getYaw());
            locObj.addProperty("pitch", loc.getPitch());
            return locObj;
        }
        if (obj instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) obj;
            JsonObject sectionObj = new JsonObject();
            for (String key : section.getKeys(false)) {
                sectionObj.add(key, convertObjectToJson(section.get(key)));
            }
            return sectionObj;
        }
        return new JsonPrimitive(obj.toString());
    }

    private static void importConfigToYaml(IslandBridgeAmongUs plugin, JsonObject configObj) {
        FileConfiguration config = plugin.getConfig();
        for (String key : configObj.keySet()) {
            config.set(key, convertJsonToObject(configObj.get(key)));
        }
        plugin.saveConfig();
    }

    private static Object convertJsonToObject(JsonElement element) {
        if (element == null || element.isJsonNull()) return null;
        if (element.isJsonPrimitive()) {
            JsonPrimitive prim = element.getAsJsonPrimitive();
            if (prim.isBoolean()) return prim.getAsBoolean();
            if (prim.isNumber()) return prim.getAsNumber();
            if (prim.isString()) return prim.getAsString();
        }
        if (element.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement e : element.getAsJsonArray()) {
                list.add(convertJsonToObject(e));
            }
            return list;
        }
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            // Cek apakah objek adalah Location
            if (obj.has("world") && obj.has("x") && obj.has("y") && obj.has("z")) {
                World w = Bukkit.getWorld(obj.get("world").getAsString());
                if (w != null) {
                    Location loc = new Location(
                            w,
                            obj.get("x").getAsDouble(),
                            obj.get("y").getAsDouble(),
                            obj.get("z").getAsDouble()
                    );
                    if (obj.has("yaw")) loc.setYaw(obj.get("yaw").getAsFloat());
                    if (obj.has("pitch")) loc.setPitch(obj.get("pitch").getAsFloat());
                    return loc;
                }
            }
            // Map biasa
            Map<String, Object> map = new HashMap<>();
            for (String key : obj.keySet()) {
                map.put(key, convertJsonToObject(obj.get(key)));
            }
            return map;
        }
        return null;
    }
}