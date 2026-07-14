// ============================================================
// FILE: ScoreboardManager.java (LENGKAP - dengan Fake Task)
// ============================================================
package com.islandbridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
    private final IslandBridgeAmongUs plugin;
    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private int updateTaskId = -1;

    public ScoreboardManager(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
    }

    public void startUpdater() {
        if (updateTaskId != -1) return;
        updateTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateScoreboard(p);
            }
        }, 0L, 20L).getTaskId();
    }

    public void stopUpdater() {
        if (updateTaskId != -1) {
            Bukkit.getScheduler().cancelTask(updateTaskId);
            updateTaskId = -1;
        }
    }

    public void updateAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            updateScoreboard(p);
        }
    }

    public void updateScoreboard(Player p) {
        Scoreboard board = playerScoreboards.computeIfAbsent(p.getUniqueId(), id -> Bukkit.getScoreboardManager().getNewScoreboard());

        SabotageManager sm = plugin.getSabotageManager();
        Team hideTeam = board.getTeam("hideNametag");
        if (hideTeam == null) {
            hideTeam = board.registerNewTeam("hideNametag");
            hideTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
        if (sm.isSkinWalkerActive()) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (plugin.getGameManager().isPlayerAlive(online)) {
                    if (!hideTeam.hasEntry(online.getName())) {
                        hideTeam.addEntry(online.getName());
                    }
                } else {
                    hideTeam.removeEntry(online.getName());
                }
            }
        } else {
            for (String entry : hideTeam.getEntries()) {
                hideTeam.removeEntry(entry);
            }
        }

        Objective obj = board.getObjective("gameinfo");
        if (obj == null) {
            obj = board.registerNewObjective("gameinfo", "dummy", ChatColor.GOLD + "═══ ISLAND BRIDGE ═══");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            for (String entry : board.getEntries()) {
                if (!entry.equals("hideNametag")) {
                    board.resetScores(entry);
                }
            }
        }

        GameManager gm = plugin.getGameManager();
        BridgeManager bm = plugin.getBridgeManager();
        com.islandbridge.tasksystem.TaskManager tm = plugin.getTaskManager();
        VotingSystem vs = plugin.getVotingSystem();
        RoleRegistry.Role role = gm.getRole(p);

        int score = 0;

        // Status (semua role)
        String status = "§eStatus: ";
        switch (gm.getState()) {
            case WAITING: status += "§aMenunggu"; break;
            case STARTING: status += "§6Memulai..."; break;
            case GAME_RUNNING: status += "§aBerjalan"; break;
            case MEETING: status += "§cMeeting"; break;
            case GAME_END: status += "§cSelesai"; break;
            default: status += "§7?";
        }
        obj.getScore(status).setScore(score);

        // Role (semua role)
        String roleDisplay = "§fRole: ";
        if (role == RoleRegistry.Role.CREWMATE) roleDisplay += "§aCrewmate";
        else if (role == RoleRegistry.Role.IMPOSTOR) roleDisplay += "§cImpostor";
        else if (role == RoleRegistry.Role.JOKER) roleDisplay += "§dJoker";
        else roleDisplay += "§7?";
        obj.getScore(roleDisplay).setScore(score);

        // === TASK UNTUK SEMUA ROLE ===
        String taskDisplay = "§fTask: ";
        if (tm.hasTask(p)) {
            if (tm.isTaskNPC(p)) {
                taskDisplay += "§eCari " + tm.getTargetNPC(p);
            } else {
                String ttype = tm.getTaskType(p);
                if (ttype.equals("TEBANG")) {
                    int wood = tm.getWoodCount().getOrDefault(p.getUniqueId(), 0);
                    taskDisplay += "§eTebang " + wood + "/2 Log";
                } else if (ttype.equals("MANCING")) {
                    int caught = tm.getFishCaught().getOrDefault(p.getUniqueId(), 0);
                    int required = tm.getFishRequired().getOrDefault(p.getUniqueId(), 5);
                    taskDisplay += "§eMancing " + caught + "/" + required + " Ikan";
                } else if (ttype.equals("SWIM")) {
                    int elapsed = tm.getSwimElapsed(p);
                    int required = tm.getSwimRequired(p);
                    taskDisplay += "§eBerenang " + elapsed + "/" + required + "s";
                } else if (ttype.equals("FARM")) {
                    int count = tm.getFarmCount(p);
                    int required = tm.getFarmRequired(p);
                    taskDisplay += "§ePanen " + count + "/" + required;
                } else {
                    taskDisplay += "§e" + ttype;
                }
            }
        } else {
            taskDisplay += "§7Tidak ada";
        }

        // === INFORMASI PER ROLE ===

        // CREWMATE
        if (role == RoleRegistry.Role.CREWMATE) {
            obj.getScore(taskDisplay).setScore(score);

            long emRemaining = gm.getEmergencyRemaining();
            String emCd = "§fEmergency: ";
            if (emRemaining <= 0) emCd += "§aSiap";
            else emCd += "§c" + emRemaining + "s";
            obj.getScore(emCd).setScore(score);
        }

        // IMPOSTOR
        if (role == RoleRegistry.Role.IMPOSTOR) {
            long killRemaining = gm.getKillRemaining(p);
            String killCd = "§fKill: ";
            if (killRemaining <= 0) killCd += "§aAktif";
            else killCd += "§c" + killRemaining + "s";
            obj.getScore(killCd).setScore(score);

            long sabRemaining = sm.getSabotageRemaining(p);
            String sabCd = "§fSabotase: ";
            if (sabRemaining <= 0) sabCd += "§aSiap";
            else sabCd += "§c" + sabRemaining + "s";
            obj.getScore(sabCd).setScore(score);

            long swRemaining = sm.getSkinWalkerRemaining(p);
            String swCd = "§fSkin Walker: ";
            if (swRemaining <= 0) swCd += "§aSiap";
            else swCd += "§c" + swRemaining + "s";
            obj.getScore(swCd).setScore(score);

            long emRemaining = gm.getEmergencyRemaining();
            String emCd = "§fEmergency: ";
            if (emRemaining <= 0) emCd += "§aSiap";
            else emCd += "§c" + emRemaining + "s";
            obj.getScore(emCd).setScore(score);

            // ★ FAKE TASK untuk impostor
            int fakeProgress = plugin.getFakeTaskBook().getProgress(p);
            int maxFake = 20; // Bisa diambil dari config nanti
            obj.getScore("§fFake Task: §e" + fakeProgress + "/" + maxFake).setScore(score);

            // Task nyata (sama seperti Crewmate)
            obj.getScore(taskDisplay).setScore(score);
        }

        // JOKER
        if (role == RoleRegistry.Role.JOKER) {
            int corpseCount = plugin.getCorpseGlowManager().getCorpseCount();
            obj.getScore("§fMayat Terdeteksi: §e" + corpseCount).setScore(score);

            long emRemaining = gm.getEmergencyRemaining();
            String emCd = "§fEmergency: ";
            if (emRemaining <= 0) emCd += "§aSiap";
            else emCd += "§c" + emRemaining + "s";
            obj.getScore(emCd).setScore(score);

            // ★ FAKE TASK untuk joker
            int fakeProgress = plugin.getFakeTaskBook().getProgress(p);
            int maxFake = 20;
            obj.getScore("§fFake Task: §e" + fakeProgress + "/" + maxFake).setScore(score);

            // Task nyata (sama seperti Crewmate)
            obj.getScore(taskDisplay).setScore(score);
        }

        // Informasi umum (semua role)
        int builtBlocks = bm.getBlocksBuilt();
        int totalBlocks = bm.getTotalBlocks();
        obj.getScore("§fProgress: §e" + builtBlocks + "/" + totalBlocks + " blok").setScore(score);

        if (sm.isSabotaging()) {
            obj.getScore("§c⚡ LAMPU MATI!").setScore(score);
        } else if (sm.isSkinWalkerActive()) {
            obj.getScore("§d✨ Skin Walker Aktif").setScore(score);
        } else {
            obj.getScore("§7Tidak ada sabotase").setScore(score);
        }

        if (gm.getState() == GameManager.GameState.MEETING) {
            obj.getScore("§c⏰ MEETING BERLANGSUNG").setScore(score);
        } else if (vs.isVotingActive()) {
            obj.getScore("§e🗳️ VOTING BERLANGSUNG").setScore(score);
        }

        obj.getScore("§m-----------------").setScore(score);

        p.setScoreboard(board);
    }

    public void removePlayer(Player p) {
        playerScoreboards.remove(p.getUniqueId());
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}