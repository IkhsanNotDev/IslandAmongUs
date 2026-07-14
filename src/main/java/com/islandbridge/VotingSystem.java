package com.islandbridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class VotingSystem {
    private final IslandBridgeAmongUs plugin;
    private boolean votingActive = false;
    private final Map<UUID, UUID> votes = new HashMap<>();
    private int meetingTaskId = -1;
    private final List<ArmorStand> voteIndicators = new ArrayList<>();
    private final Map<UUID, ArmorStand> voterIndicatorMap = new HashMap<>();

    private int meetingTimeLeft = 0;

    public VotingSystem(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
    }

    public boolean isVotingActive() {
        return votingActive;
    }

    public int getMeetingTimeLeft() {
        return meetingTimeLeft;
    }

    /**
     * Teleport semua pemain ke area meeting.
     * Lokasi diambil dari getMeetingCenter() yang berasal dari config "meeting-center.location".
     * Jika null, gunakan mapSpawn atau lobby sebagai fallback.
     */
    public void teleportToMeetingCircle() {
        // ★ Ambil lokasi meeting center dari GameManager (berasal dari config)
        Location center = plugin.getGameManager().getMeetingCenter();

        // ★ FALLBACK jika center null
        if (center == null) {
            center = plugin.getGameManager().getMapSpawn();
            if (center == null) {
                center = plugin.getGameManager().getLobbyLocation();
            }
            if (center == null) {
                plugin.getLogger().warning("Meeting center, map spawn, dan lobby tidak diset! Tidak bisa teleport player.");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(ChatColor.RED + "⚠ Admin belum menyetel lokasi meeting! Gunakan /ib setmeeting");
                }
                return;
            }
            plugin.getLogger().info("Meeting center null, menggunakan fallback location: " + center.getBlockX() + "," + center.getBlockY() + "," + center.getBlockZ());
        }

        // ★ Bersihkan efek sebelum teleport
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.JUMP_BOOST);
            p.removePotionEffect(PotionEffectType.RESISTANCE);
            p.removePotionEffect(PotionEffectType.SLOWNESS);
            p.setWalkSpeed(0.2f);
            p.setFlySpeed(0.1f);
        }

        List<Player> allPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        List<Player> alivePlayers = new ArrayList<>();
        List<Player> deadPlayers = new ArrayList<>();

        for (Player p : allPlayers) {
            if (plugin.getGameManager().isPlayerAlive(p)) {
                alivePlayers.add(p);
            } else {
                deadPlayers.add(p);
            }
        }

        // ★ Teleport pemain hidup dalam lingkaran
        int radius = 3;
        for (int i = 0; i < alivePlayers.size(); i++) {
            Player p = alivePlayers.get(i);
            double angle = 2 * Math.PI * i / Math.max(1, alivePlayers.size());
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            double y = center.getY();
            Location loc = new Location(center.getWorld(), x, y, z);
            p.teleport(loc);
            p.setWalkSpeed(0.01f);
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 128, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 255, false, false));
        }

        // ★ Teleport pemain mati (spectator) – di luar lingkaran, sedikit lebih tinggi
        for (int i = 0; i < deadPlayers.size(); i++) {
            Player p = deadPlayers.get(i);
            double angle = 2 * Math.PI * i / Math.max(1, deadPlayers.size());
            double x = center.getX() + (radius + 2) * Math.cos(angle);
            double z = center.getZ() + (radius + 2) * Math.sin(angle);
            double y = center.getY() + 2;
            Location loc = new Location(center.getWorld(), x, y, z);
            p.teleport(loc);
            p.setWalkSpeed(0.01f);
        }

        // ★ Efek suara dan partikel
        for (Player p : allPlayers) {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            p.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, p.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        }

        Bukkit.broadcastMessage(ChatColor.AQUA + "📍 Semua pemain diteleport ke area meeting!");
    }

    public void startVotingSession() {
        votingActive = true;
        votes.clear();
        voteIndicators.clear();
        voterIndicatorMap.clear();

        if (plugin.getSabotageManager() != null && plugin.getSabotageManager().isSkinWalkerActive()) {
            plugin.getSabotageManager().pauseSkinWalker();
        }

        final int meetingTime = plugin.getConfig().getInt("game.meeting-time", 45);
        meetingTimeLeft = meetingTime;

        // ★ BUKA GUI VOTE SETELAH 3 DETIK (agar pemain sempat teleport)
        Bukkit.getScheduler().runTaskLater(plugin, this::openVoteGUI, 60L);

        meetingTaskId = new BukkitRunnable() {
            int timeLeft = meetingTime;
            @Override
            public void run() {
                checkPlayersEverySecond();

                if (plugin.getGameManager().getState() != GameManager.GameState.MEETING) {
                    cancel();
                    return;
                }

                meetingTimeLeft = timeLeft;

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (plugin.getGameManager().isPlayerAlive(p)) {
                        p.sendActionBar(ChatColor.AQUA + "⏰ Waktu meeting: " + timeLeft + " detik tersisa ⏰");
                    } else {
                        p.sendActionBar(ChatColor.GRAY + "⏰ Meeting: " + timeLeft + "s tersisa (Spectator)");
                    }
                }

                if (timeLeft <= 5 && timeLeft > 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    }
                }

                if (timeLeft <= 0) {
                    cancel();
                    finishVoting();
                }
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    }

    private void openVoteGUI() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!plugin.getGameManager().isPlayerAlive(p)) continue;
            p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
            Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Vote to Eject");
            List<UUID> targets = new ArrayList<>(plugin.getGameManager().getLivingPlayers());
            targets.remove(p.getUniqueId());
            for (UUID id : targets) {
                Player target = Bukkit.getPlayer(id);
                if (target != null) {
                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.setOwningPlayer(target);
                    meta.setDisplayName(ChatColor.RED + target.getName());
                    head.setItemMeta(meta);
                    gui.addItem(head);
                }
            }
            ItemStack skip = new ItemStack(Material.BARRIER);
            skip.editMeta(meta -> meta.setDisplayName(ChatColor.GRAY + "SKIP VOTE"));
            gui.addItem(skip);
            p.openInventory(gui);
        }
    }

    private void removeIndicator(Player voter) {
        ArmorStand old = voterIndicatorMap.remove(voter.getUniqueId());
        if (old != null && !old.isDead()) {
            old.remove();
            voteIndicators.remove(old);
        }
    }

    public void castVote(Player voter, Player target) {
        if (!plugin.getGameManager().isPlayerAlive(voter)) {
            voter.sendMessage(ChatColor.RED + "Kamu sudah mati, tidak bisa vote!");
            return;
        }
        if (!votingActive) {
            voter.sendMessage(ChatColor.RED + "Voting tidak aktif!");
            return;
        }
        if (target == null || !plugin.getGameManager().isPlayerAlive(target)) {
            voter.sendMessage(ChatColor.RED + "Target tidak valid atau sudah mati!");
            return;
        }

        votes.remove(voter.getUniqueId());
        votes.put(voter.getUniqueId(), target.getUniqueId());
        voter.sendMessage(ChatColor.GREEN + "Kamu memilih " + target.getName() + " (Menunggu waktu habis...)");
        voter.playSound(voter.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        voter.closeInventory();
        removeIndicator(voter);
        spawnVoteIndicator(voter, target);
    }

    public void castSkip(Player voter) {
        if (!plugin.getGameManager().isPlayerAlive(voter)) {
            voter.sendMessage(ChatColor.RED + "Kamu sudah mati, tidak bisa vote!");
            return;
        }
        if (!votingActive) {
            voter.sendMessage(ChatColor.RED + "Voting tidak aktif!");
            return;
        }
        votes.remove(voter.getUniqueId());
        votes.put(voter.getUniqueId(), null);
        voter.sendMessage(ChatColor.GREEN + "Kamu memilih SKIP (Menunggu waktu habis...)");
        voter.playSound(voter.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 0.8f);
        voter.closeInventory();
        removeIndicator(voter);
        spawnSkipIndicator(voter);
    }

    private void checkPlayersEverySecond() {
        if (!votingActive) return;
        List<UUID> currentLiving = new ArrayList<>(plugin.getGameManager().getLivingPlayers());
        for (UUID uuid : currentLiving) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                plugin.getGameManager().handleMeetingQuitByUUID(uuid);
            }
        }
    }

    private void spawnVoteIndicator(Player voter, Player target) {
        Location loc = voter.getLocation().add(0, 1.5, 0);
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMarker(false);
        stand.setInvulnerable(true);
        stand.setCustomNameVisible(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(target);
        skull.setItemMeta(meta);
        stand.getEquipment().setHelmet(skull);
        voteIndicators.add(stand);
        voterIndicatorMap.put(voter.getUniqueId(), stand);
        new BukkitRunnable() {
            float angle = 0;
            @Override
            public void run() {
                if (stand.isDead() || !votingActive) {
                    cancel();
                    return;
                }
                angle += 10f;
                stand.setRotation(angle, 0);
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void spawnSkipIndicator(Player voter) {
        Location loc = voter.getLocation().add(0, 1.5, 0);
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMarker(false);
        stand.setInvulnerable(true);
        stand.setCustomNameVisible(true);
        stand.setCustomName(ChatColor.YELLOW + "✘ SKIP ✘");
        stand.setBasePlate(false);
        stand.setHelmet(null);
        voteIndicators.add(stand);
        voterIndicatorMap.put(voter.getUniqueId(), stand);
        new BukkitRunnable() {
            float angle = 0;
            @Override
            public void run() {
                if (stand.isDead() || !votingActive) {
                    cancel();
                    return;
                }
                angle += 10f;
                stand.setRotation(angle, 0);
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void finishVoting() {
        votingActive = false;
        meetingTimeLeft = 0;
        if (meetingTaskId != -1) Bukkit.getScheduler().cancelTask(meetingTaskId);

        for (ArmorStand as : voteIndicators) {
            if (as != null && !as.isDead()) as.remove();
        }
        voteIndicators.clear();
        voterIndicatorMap.clear();

        // ★ RESET semua efek dan walk speed
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setWalkSpeed(0.2f);
            p.removePotionEffect(PotionEffectType.JUMP_BOOST);
            p.removePotionEffect(PotionEffectType.RESISTANCE);
            p.removePotionEffect(PotionEffectType.SLOWNESS);
            p.sendActionBar("");
        }

        Map<UUID, Integer> voteCount = new HashMap<>();
        int skipCount = 0;

        for (Map.Entry<UUID, UUID> entry : votes.entrySet()) {
            Player voter = Bukkit.getPlayer(entry.getKey());
            if (voter == null || !plugin.getGameManager().isPlayerAlive(voter)) continue;

            if (entry.getValue() == null) {
                skipCount++;
            } else {
                UUID targetId = entry.getValue();
                if (plugin.getGameManager().getLivingPlayers().contains(targetId)) {
                    voteCount.put(targetId, voteCount.getOrDefault(targetId, 0) + 1);
                }
            }
        }

        UUID ejected = null;
        int maxVotes = 0;
        boolean tie = false;
        for (Map.Entry<UUID, Integer> e : voteCount.entrySet()) {
            if (e.getValue() > maxVotes) {
                maxVotes = e.getValue();
                ejected = e.getKey();
                tie = false;
            } else if (e.getValue() == maxVotes) {
                tie = true;
            }
        }

        if (ejected == null || maxVotes <= skipCount || tie) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Tidak ada yang di-eject (seri atau skip terbanyak).");
        } else {
            Player victim = Bukkit.getPlayer(ejected);
            if (victim != null) {
                RoleRegistry.Role role = plugin.getGameManager().getRole(victim);
                plugin.getGameManager().killPlayer(victim, null, false);
                Bukkit.broadcastMessage(ChatColor.RED + victim.getName() + " di-eject!");
                plugin.getGameManager().playSoundToAll(Sound.ENTITY_PLAYER_HURT, 0.8f, 0.5f);
                if (role == RoleRegistry.Role.IMPOSTOR) {
                    Bukkit.broadcastMessage(ChatColor.RED + victim.getName() + " adalah Impostor!");
                    plugin.getGameManager().celebrateVictory("Crewmate menang! Impostor terungkap!");
                    return;
                } else if (role == RoleRegistry.Role.JOKER) {
                    Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + victim.getName() + " adalah Joker! Joker menang!");
                    plugin.getGameManager().celebrateVictory("Joker menang!");
                    return;
                } else {
                    Bukkit.broadcastMessage(ChatColor.GRAY + victim.getName() + " bukan Impostor.");
                }
            }
        }

        // ★ AKHIRI MEETING
        plugin.getGameManager().endMeeting();
    }
}