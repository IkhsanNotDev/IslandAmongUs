// ============================================================
// FILE: CommandHandler.java (LENGKAP + semua setting + autofarmland)
// ============================================================
package com.islandbridge.commands;

import com.islandbridge.*;
import com.islandbridge.tasksystem.TaskManager;
import com.islandbridge.utils.ConfigExporter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

public class CommandHandler implements CommandExecutor {
    private final IslandBridgeAmongUs plugin;

    public CommandHandler(IslandBridgeAmongUs plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player p = (Player) sender;
        if (!p.isOp()) {
            p.sendMessage(ChatColor.RED + "You need OP to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(p);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setlobby":
                plugin.getGameManager().setLobbyLocation(p.getLocation());
                p.sendMessage(ChatColor.GREEN + "Lobby location set.");
                break;
            case "setmap":
                plugin.getGameManager().setMapSpawn(p.getLocation());
                p.sendMessage(ChatColor.GREEN + "Map spawn set.");
                break;
            case "setmeeting":
                plugin.getGameManager().setMeetingCenter(p.getLocation());
                p.sendMessage(ChatColor.GREEN + "Meeting center set.");
                break;
            case "setpulau1":
                plugin.getBridgeManager().setPulau1(p.getLocation());
                p.sendMessage(ChatColor.GREEN + "Titik Pulau 1 (start jembatan) diset!");
                break;
            case "setpulau2":
                plugin.getBridgeManager().setPulau2(p.getLocation());
                p.sendMessage(ChatColor.GREEN + "Titik Pulau 2 (end jembatan) diset!");
                break;
            case "setjembatankiri":
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Gunakan: /ib setjembatankiri <jumlah block>");
                    return true;
                }
                try {
                    int lebar = Integer.parseInt(args[1]);
                    plugin.getBridgeManager().setLebarKiri(lebar);
                    p.sendMessage(ChatColor.GREEN + "Lebar kiri jembatan diset ke " + lebar);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + "Angka tidak valid!");
                }
                break;
            case "setjembatankanan":
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Gunakan: /ib setjembatankanan <jumlah block>");
                    return true;
                }
                try {
                    int lebar = Integer.parseInt(args[1]);
                    plugin.getBridgeManager().setLebarKanan(lebar);
                    p.sendMessage(ChatColor.GREEN + "Lebar kanan jembatan diset ke " + lebar);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + "Angka tidak valid!");
                }
                break;
            case "setnpcjembatan":
                String npcDisplayName = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : null;
                if (npcDisplayName == null) {
                    npcDisplayName = plugin.getConfig().getString("bridge.npc_name", "&b&l[NPC] &e&lPembangun Jembatan");
                }
                plugin.getBridgeManager().spawnBridgeNPC(p.getLocation(), npcDisplayName);
                p.sendMessage(ChatColor.GREEN + "NPC Jembatan ditempatkan dengan nama: " + ChatColor.translateAlternateColorCodes('&', npcDisplayName));
                break;
            case "setnpcname":
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Gunakan: /ib setnpcname <nama dengan & untuk warna>");
                    return true;
                }
                String name = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                plugin.getBridgeManager().setNpcName(name);
                p.sendMessage(ChatColor.GREEN + "Nama NPC diubah menjadi " + ChatColor.translateAlternateColorCodes('&', name));
                break;
            case "resetbridge":
                plugin.getBridgeManager().resetBridge();
                p.sendMessage(ChatColor.YELLOW + "Jembatan telah direset.");
                break;
            case "setlamp":
                plugin.getSabotageManager().addLamp(p.getLocation());
                p.sendMessage(ChatColor.GREEN + "Lampu redstone ditambahkan di lokasi anda.");
                break;
            case "autolamp":
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Gunakan: /ib autolamp <radius>");
                    return true;
                }
                try {
                    int radius = Integer.parseInt(args[1]);
                    if (radius <= 0) {
                        p.sendMessage(ChatColor.RED + "Radius harus lebih besar dari 0.");
                        return true;
                    }
                    int maxRadius = plugin.getConfig().getInt("sabotage.max-autodetect-radius", 200);
                    if (radius > maxRadius) {
                        p.sendMessage(ChatColor.RED + "Radius terlalu besar! Maksimal " + maxRadius + " block.");
                        p.sendMessage(ChatColor.YELLOW + "Kamu bisa mengubah batas di config.yml: sabotage.max-autodetect-radius");
                        return true;
                    }
                    int found = plugin.getSabotageManager().autoDetectLamps(p, radius);
                    p.sendMessage(ChatColor.GREEN + "Scan selesai. Ditemukan " + found + " lampu redstone baru.");
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + "Radius harus angka!");
                }
                break;
            case "autochest":
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Gunakan: /ib autochest <radius>");
                    return true;
                }
                try {
                    int radius = Integer.parseInt(args[1]);
                    if (radius <= 0) {
                        p.sendMessage(ChatColor.RED + "Radius harus lebih besar dari 0.");
                        return true;
                    }
                    int maxRadius = plugin.getConfig().getInt("task.max-autodetect-radius", 200);
                    if (radius > maxRadius) {
                        p.sendMessage(ChatColor.RED + "Radius terlalu besar! Maksimal " + maxRadius + " block.");
                        p.sendMessage(ChatColor.YELLOW + "Kamu bisa mengubah batas di config.yml: task.max-autodetect-radius");
                        return true;
                    }
                    plugin.getTaskManager().autoDetectChests(p, radius);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + "Radius harus angka!");
                }
                break;
            case "autooak":
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Gunakan: /ib autooak <radius>");
                    return true;
                }
                try {
                    int radius = Integer.parseInt(args[1]);
                    if (radius <= 0) {
                        p.sendMessage(ChatColor.RED + "Radius harus lebih besar dari 0.");
                        return true;
                    }
                    int maxRadius = plugin.getConfig().getInt("task.max-autodetect-radius", 200);
                    if (radius > maxRadius) {
                        p.sendMessage(ChatColor.RED + "Radius terlalu besar! Maksimal " + maxRadius + " block.");
                        p.sendMessage(ChatColor.YELLOW + "Kamu bisa mengubah batas di config.yml: task.max-autodetect-radius");
                        return true;
                    }
                    plugin.getTaskManager().autoDetectOakLogs(p, radius);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + "Radius harus angka!");
                }
                break;
            case "autofarmland":
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Gunakan: /ib autofarmland <radius>");
                    return true;
                }
                try {
                    int radius = Integer.parseInt(args[1]);
                    if (radius <= 0) {
                        p.sendMessage(ChatColor.RED + "Radius harus > 0.");
                        return true;
                    }
                    int maxRadius = plugin.getConfig().getInt("task.max-autodetect-radius", 200);
                    if (radius > maxRadius) {
                        p.sendMessage(ChatColor.RED + "Radius terlalu besar! Maksimal " + maxRadius + " block.");
                        p.sendMessage(ChatColor.YELLOW + "Kamu bisa mengubah batas di config.yml: task.max-autodetect-radius");
                        return true;
                    }
                    plugin.getTaskManager().autoDetectFarmland(p, radius);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + "Radius harus angka!");
                }
                break;
            case "setgenerator":
                Location genLoc = findNearestLever(p, 2);
                if (genLoc == null) {
                    p.sendMessage(ChatColor.RED + "Tidak ada lever dalam radius 2 block! Tempatkan lever terlebih dahulu.");
                    return true;
                }
                plugin.getSabotageManager().setGenerator(genLoc);
                p.sendMessage(ChatColor.GREEN + "Generator (lever) ditemukan dan disimpan di koordinat " +
                        genLoc.getBlockX() + ", " + genLoc.getBlockY() + ", " + genLoc.getBlockZ());
                break;
            case "setvoid":
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Gunakan: /ib setvoid <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    p.sendMessage(ChatColor.RED + "Player tidak online!");
                    return true;
                }
                GameManager gm = plugin.getGameManager();
                if (gm.isVoidExempt(target.getUniqueId())) {
                    gm.removeVoidExempt(target);
                    p.sendMessage(ChatColor.YELLOW + "Void exemption dihapus untuk " + target.getName());
                } else {
                    gm.addVoidExempt(target);
                    p.sendMessage(ChatColor.GREEN + "Void exemption diberikan ke " + target.getName() + " (tidak akan terkena void di lobby)");
                }
                break;
            case "setskin":
                if (args.length > 1) {
                    plugin.getSkinWalker().setSkin(p, args[1]);
                } else {
                    p.sendMessage(ChatColor.RED + "Gunakan: /ib setskin <namaSkin>");
                }
                break;
            case "testing":
                if (args.length > 1) {
                    boolean mode = Boolean.parseBoolean(args[1]);
                    plugin.getGameManager().setTestingMode(mode);
                    p.sendMessage("Testing mode: " + mode);
                }
                break;
            case "role":
                if (plugin.getGameManager().isTestingMode() && args.length > 1) {
                    try {
                        RoleRegistry.Role role = RoleRegistry.Role.valueOf(args[1].toUpperCase());
                        p.sendMessage("Role forced to " + role.name() + " (restart game to apply)");
                    } catch (IllegalArgumentException e) {
                        p.sendMessage("Invalid role.");
                    }
                } else {
                    p.sendMessage("Mode testing harus aktif!");
                }
                break;
            case "start":
                plugin.getGameManager().startGame();
                break;
            case "sabotage":
                if (plugin.getGameManager().getState() != GameManager.GameState.GAME_RUNNING) {
                    p.sendMessage(ChatColor.RED + "Game belum berjalan!");
                    return true;
                }
                if (plugin.getGameManager().getRole(p) != RoleRegistry.Role.IMPOSTOR) {
                    p.sendMessage(ChatColor.RED + "Hanya Impostor yang bisa menggunakan sabotase!");
                    return true;
                }
                plugin.getSabotageManager().sabotageLights(p);
                break;
            // ─── Perintah /ib faketask dihapus karena fake task otomatis ───
            case "reload":
                plugin.reloadPluginConfig();
                plugin.getTaskManager().clearAllNPCs();
                plugin.getTaskManager().loadData();
                plugin.getBridgeManager().respawnNPC();
                p.sendMessage(ChatColor.GREEN + "Config reloaded and all NPCs respawned with random armor!");
                break;
            case "ceknpc":
                TaskManager tm = plugin.getTaskManager();
                List<String> npcList = tm.listNPCsWithCoordinates();
                if (npcList.isEmpty()) {
                    p.sendMessage(ChatColor.YELLOW + "Belum ada NPC Task yang terdaftar.");
                } else {
                    p.sendMessage(ChatColor.GOLD + "===== DAFTAR NPC TASK =====");
                    for (String line : npcList) {
                        p.sendMessage(line);
                    }
                    p.sendMessage(ChatColor.GRAY + "Total: " + npcList.size() + " NPC");
                }
                break;
            case "export":
                if (ConfigExporter.exportConfig(plugin, p)) {
                    p.sendMessage(ChatColor.GREEN + "Config berhasil diekspor ke folder exports/");
                } else {
                    p.sendMessage(ChatColor.RED + "Gagal ekspor config!");
                }
                break;
            case "import":
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Gunakan: /ib import <nama_file>");
                    p.sendMessage(ChatColor.YELLOW + "File harus berada di folder exports/ (contoh: config_2025-06-23.json)");
                    return true;
                }
                String fileName = args[1];
                if (!fileName.endsWith(".json")) fileName += ".json";
                File importFile = new File(plugin.getDataFolder(), "exports/" + fileName);
                if (ConfigExporter.importConfig(plugin, importFile, p)) {
                    plugin.reloadPluginConfig();
                    p.sendMessage(ChatColor.GREEN + "Config berhasil di-import dari " + fileName);
                } else {
                    p.sendMessage(ChatColor.RED + "Gagal import config! Pastikan file ada dan format JSON valid.");
                }
                break;
            case "setting":
                if (args.length < 3) {
                    p.sendMessage(ChatColor.RED + "Usage: /ib setting <key> <value>");
                    p.sendMessage(ChatColor.YELLOW + "Keys: cdkill, minplayers, countdown, meetingtime, joker, impostorcount, skinwalkerblind, autodetectlamp, autodetecttask");
                    p.sendMessage(ChatColor.GRAY + "Contoh: /ib setting cdkill 45s, /ib setting minplayers 4, /ib setting joker false");
                    return true;
                }
                String key = args[1].toLowerCase();
                String value = args[2];
                handleSetting(p, key, value);
                break;
            default:
                sendHelp(p);
        }
        return true;
    }

    private void handleSetting(Player p, String key, String value) {
        var config = plugin.getConfig();
        GameManager gm = plugin.getGameManager();
        SabotageManager sm = plugin.getSabotageManager();

        try {
            switch (key) {
                case "cdkill": {
                    long seconds = parseTimeToSeconds(value);
                    if (seconds <= 0) throw new IllegalArgumentException("Waktu tidak valid!");
                    gm.setKillCooldownSeconds(seconds);
                    p.sendMessage(ChatColor.GREEN + "Kill cooldown diubah menjadi " + seconds + " detik.");
                    break;
                }
                case "minplayers": {
                    int val = Integer.parseInt(value);
                    if (val < 1) throw new IllegalArgumentException("Minimal 1 player.");
                    config.set("game.min-players", val);
                    plugin.saveConfig();
                    p.sendMessage(ChatColor.GREEN + "Minimal players diubah menjadi " + val);
                    break;
                }
                case "countdown": {
                    int val = Integer.parseInt(value);
                    if (val < 1) throw new IllegalArgumentException("Harus lebih dari 0.");
                    config.set("game.countdown", val);
                    plugin.saveConfig();
                    p.sendMessage(ChatColor.GREEN + "Countdown diubah menjadi " + val + " detik.");
                    break;
                }
                case "meetingtime": {
                    int val = Integer.parseInt(value);
                    if (val < 5) throw new IllegalArgumentException("Minimal 5 detik.");
                    config.set("game.meeting-time", val);
                    plugin.saveConfig();
                    p.sendMessage(ChatColor.GREEN + "Waktu meeting diubah menjadi " + val + " detik.");
                    break;
                }
                case "joker": {
                    boolean val = Boolean.parseBoolean(value);
                    config.set("roles.joker-enabled", val);
                    plugin.saveConfig();
                    p.sendMessage(ChatColor.GREEN + "Joker enabled = " + val);
                    break;
                }
                case "impostorcount": {
                    int val = Integer.parseInt(value);
                    if (val < 0) throw new IllegalArgumentException("Tidak boleh negatif.");
                    config.set("roles.impostor-count", val);
                    plugin.saveConfig();
                    p.sendMessage(ChatColor.GREEN + "Jumlah impostor diubah menjadi " + val);
                    break;
                }
                case "skinwalkerblind": {
                    long seconds = parseTimeToSeconds(value);
                    if (seconds <= 0) throw new IllegalArgumentException("Waktu tidak valid!");
                    int ticks = (int) (seconds * 20);
                    if (ticks > 600) ticks = 600;
                    config.set("skinwalker-blind-duration-ticks", ticks);
                    plugin.saveConfig();
                    sm.setBlindDurationTicks(ticks);
                    p.sendMessage(ChatColor.GREEN + "Durasi buta Skin Walker diubah menjadi " + seconds + " detik (" + ticks + " ticks).");
                    break;
                }
                case "autodetectlamp": {
                    int radius = Integer.parseInt(value);
                    if (radius <= 0) throw new IllegalArgumentException("Radius harus > 0.");
                    config.set("sabotage.max-autodetect-radius", radius);
                    plugin.saveConfig();
                    p.sendMessage(ChatColor.GREEN + "Radius auto-detect lamp diubah menjadi " + radius);
                    break;
                }
                case "autodetecttask": {
                    int radius = Integer.parseInt(value);
                    if (radius <= 0) throw new IllegalArgumentException("Radius harus > 0.");
                    config.set("task.max-autodetect-radius", radius);
                    plugin.saveConfig();
                    p.sendMessage(ChatColor.GREEN + "Radius auto-detect task (chest/oak/farmland) diubah menjadi " + radius);
                    break;
                }
                default:
                    p.sendMessage(ChatColor.RED + "Key tidak dikenal! Gunakan: cdkill, minplayers, countdown, meetingtime, joker, impostorcount, skinwalkerblind, autodetectlamp, autodetecttask");
            }
        } catch (Exception e) {
            p.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
        }
    }

    private Location findNearestLever(Player p, int radius) {
        Location center = p.getLocation();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getWorld().getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);
                    if (block.getType() == Material.LEVER) {
                        return block.getLocation();
                    }
                }
            }
        }
        return null;
    }

    private long parseTimeToSeconds(String input) {
        if (input == null || input.isEmpty()) return -1;
        input = input.toLowerCase();
        long multiplier = 1;
        if (input.endsWith("s")) {
            multiplier = 1;
            input = input.substring(0, input.length() - 1);
        } else if (input.endsWith("m")) {
            multiplier = 60;
            input = input.substring(0, input.length() - 1);
        } else if (input.endsWith("h")) {
            multiplier = 3600;
            input = input.substring(0, input.length() - 1);
        }
        try {
            long value = Long.parseLong(input);
            return value * multiplier;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void sendHelp(Player p) {
        int maxLampRadius = plugin.getConfig().getInt("sabotage.max-autodetect-radius", 200);
        int maxTaskRadius = plugin.getConfig().getInt("task.max-autodetect-radius", 200);
        p.sendMessage(ChatColor.YELLOW + "===== IslandBridgeAmongUs Help =====");
        p.sendMessage("/ib setlobby - Set lobby spawn");
        p.sendMessage("/ib setmap - Set game map spawn");
        p.sendMessage("/ib setmeeting - Set meeting center");
        p.sendMessage("/ib setpulau1 - Set bridge start point (Pulau 1)");
        p.sendMessage("/ib setpulau2 - Set bridge end point (Pulau 2)");
        p.sendMessage("/ib setjembatankiri <lebar> - Set left bridge width");
        p.sendMessage("/ib setjembatankanan <lebar> - Set right bridge width");
        p.sendMessage("/ib setnpcjembatan [nama] - Place deposit NPC");
        p.sendMessage("/ib setnpcname <nama> - Change NPC name");
        p.sendMessage("/ib resetbridge - Reset bridge progress");
        p.sendMessage("/ib setlamp - Add manual lamp at your position");
        p.sendMessage("/ib autolamp <radius> - Auto detect REDSTONE_LAMP (max " + maxLampRadius + ")");
        p.sendMessage("/ib autochest <radius> - Auto detect CHEST/TRAPPED_CHEST (max " + maxTaskRadius + ")");
        p.sendMessage("/ib autooak <radius> - Auto detect OAK_LOG for task trees (max " + maxTaskRadius + ")");
        p.sendMessage("/ib autofarmland <radius> - Auto detect FARMLAND for farming task (max " + maxTaskRadius + ")");
        p.sendMessage("/ib setgenerator - Auto find lever within 2 blocks and set as generator");
        p.sendMessage("/ib setvoid <player> - Toggle void exemption for player");
        p.sendMessage("/ib setskin <nama> - Change skin (default IkhsanNotDev)");
        p.sendMessage("/ib testing true/false - Enable solo testing mode");
        p.sendMessage("/ib start - Start the game");
        p.sendMessage("/ib sabotage - Lights sabotage (Impostor only, no item needed)");
        p.sendMessage("/ib reload - Reload config and respawn all NPCs with random armor");
        p.sendMessage("/ib ceknpc - List all task NPCs with coordinates");
        p.sendMessage("/ib export - Export all config to JSON (folder exports/)");
        p.sendMessage("/ib import <file> - Import config from JSON file");
        p.sendMessage(ChatColor.AQUA + "Settings:");
        p.sendMessage("/ib setting cdkill <waktu> - Set kill cooldown (30s, 5m, 2h)");
        p.sendMessage("/ib setting minplayers <angka> - Set minimal pemain untuk mulai");
        p.sendMessage("/ib setting countdown <detik> - Set waktu countdown awal");
        p.sendMessage("/ib setting meetingtime <detik> - Set durasi meeting");
        p.sendMessage("/ib setting joker <true/false> - Aktif/nonaktifkan role Joker");
        p.sendMessage("/ib setting impostorcount <angka> - Set jumlah impostor");
        p.sendMessage("/ib setting skinwalkerblind <waktu> - Set durasi buta Skin Walker (contoh: 4s)");
        p.sendMessage("/ib setting autodetectlamp <radius> - Set radius auto-detect lamp");
        p.sendMessage("/ib setting autodetecttask <radius> - Set radius auto-detect task (chest/oak/farmland)");
        p.sendMessage(" ");
        p.sendMessage(ChatColor.AQUA + "Task System Commands:");
        p.sendMessage("/setnpc <name> <time> - Create task NPC");
        p.sendMessage("/setpohon - Create oak tree (3 logs)");
        p.sendMessage("/setchest - Place task chest");
        p.sendMessage("/task <player> - Give random task");
    }
}