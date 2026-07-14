package com.islandbridge.listeners;

import com.islandbridge.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class GameListener implements Listener {
    private final IslandBridgeAmongUs plugin;

    public GameListener(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        GameManager gm = plugin.getGameManager();
        GameManager.GameState state = gm.getState();

        p.getInventory().clear();
        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);
        p.setGameMode(GameMode.ADVENTURE);
        p.setWalkSpeed(0.2f);
        p.setFlySpeed(0.1f);
        p.setFlying(false);
        p.setAllowFlight(false);
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setSaturation(20);
        p.setExhaustion(0);
        for (PotionEffect effect : p.getActivePotionEffects()) p.removePotionEffect(effect.getType());
        p.updateInventory();
        gm.applySaturation(p);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    plugin.getSkinWalker().autoDetectAndSaveSkin(p);
                } catch (Exception ex) {
                    plugin.getLogger().warning("Gagal autoDetectAndSaveSkin untuk " + p.getName() + ": " + ex.getMessage());
                }
            }
        }.runTaskLater(plugin, 1L);

        if (state == GameManager.GameState.GAME_RUNNING || state == GameManager.GameState.MEETING) {
            if (plugin.getSabotageManager() != null) {
                try {
                    plugin.getSabotageManager().handlePlayerRejoin(p);
                } catch (Exception ex) {
                    plugin.getLogger().warning("Error handlePlayerRejoin: " + ex.getMessage());
                }
            }
            if (!gm.isPlayerAlive(p)) p.setGameMode(GameMode.SPECTATOR);
        } else {
            Location lobby = gm.getLobbyLocation();
            if (lobby != null) {
                p.teleport(lobby);
                p.setWalkSpeed(0.2f);
                p.setGameMode(GameMode.ADVENTURE);
                p.sendMessage(ChatColor.GREEN + "Kamu telah dikembalikan ke Lobby.");
            } else {
                p.sendMessage(ChatColor.RED + "Lobby location belum diset! Hubungi admin.");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (plugin.getGameManager().getState() == GameManager.GameState.GAME_RUNNING) {
            plugin.getGameManager().handleDisconnect(p);
            if (plugin.getGameManager().isMeetingRunning()) {
                plugin.getGameManager().handleMeetingQuit(p);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        ItemStack item = e.getItemDrop().getItemStack();
        Material type = item.getType();
        if (type == Material.IRON_SWORD || type == Material.NETHER_STAR || type == Material.WRITTEN_BOOK ||
                type == Material.COMPASS || type == Material.OAK_PLANKS) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "Item ini tidak bisa dijatuhkan!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.OAK_PLANKS) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "Kamu tidak bisa meletakkan Oak Planks!");
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        // Cegah Task NPC dari damage (selain sudah ditangani TaskListener, tapi untuk jaga-jaga)
        if (e.getEntity().hasMetadata("TaskNPC")) {
            e.setCancelled(true);
            return;
        }

        if (plugin.getGameManager().getState() == GameManager.GameState.GAME_END) {
            e.setCancelled(true);
            return;
        }
        if (e.getEntity() instanceof ArmorStand as) {
            if (as.hasMetadata("bridgeNPC")) {
                e.setCancelled(true);
                return;
            }
            if (as.getCustomName() != null && (as.getCustomName().contains("Pembangun Jembatan") || as.getCustomName().contains("NPC"))) {
                e.setCancelled(true);
                return;
            }
        }

        if (!(e.getDamager() instanceof Player killer && e.getEntity() instanceof Player victim)) return;
        if (plugin.getGameManager().getState() != GameManager.GameState.GAME_RUNNING) return;
        if (plugin.getGameManager().isMeetingRunning()) {
            e.setCancelled(true);
            return;
        }
        if (plugin.getGameManager().getRole(killer) == RoleRegistry.Role.IMPOSTOR && isSword(killer.getInventory().getItemInMainHand().getType())) {
            if (!plugin.getGameManager().canKill(killer)) {
                long remaining = plugin.getGameManager().getKillRemaining(killer);
                killer.sendMessage(ChatColor.RED + "Kill cooldown! Tunggu " + remaining + " detik.");
                killer.playSound(killer.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                e.setCancelled(true);
                return;
            }
            plugin.getGameManager().setKillCooldown(killer);
            plugin.getGameManager().killPlayer(victim, killer, true);
            e.setCancelled(true);
            killer.sendMessage(ChatColor.RED + "Kamu membunuh " + victim.getName() + "!");
            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
        } else {
            e.setCancelled(true);
        }
    }

    private boolean isSword(Material mat) {
        return mat.name().contains("SWORD");
    }

    // ======================= INTERAKSI DENGAN CORPSE (REPORT) =======================
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        // Cegah interaksi dengan Task NPC (selain sudah ditangani TaskListener)
        if (e.getRightClicked().hasMetadata("TaskNPC")) {
            e.setCancelled(true);
            return;
        }

        if (plugin.getGameManager().getState() == GameManager.GameState.GAME_END || plugin.getGameManager().isMeetingRunning()) {
            e.setCancelled(true);
            return;
        }
        Player p = e.getPlayer();
        if (e.getRightClicked() instanceof ArmorStand entity) {
            // NPC Jembatan
            if (entity.hasMetadata("bridgeNPC") ||
                    (entity.getCustomName() != null && (entity.getCustomName().contains("Pembangun Jembatan") || entity.getCustomName().contains("NPC")))) {
                e.setCancelled(true);
                plugin.getBridgeManager().depositPlanks(p);
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
                return;
            }
            // CORPSE - Report
            if (entity.getCustomName() != null && entity.getCustomName().endsWith("Corpse")) {
                e.setCancelled(true);
                if (plugin.getGameManager().isPlayerAlive(p)) {
                    Location corpseLoc = entity.getLocation().getBlock().getLocation();
                    entity.remove();
                    plugin.getCorpseGlowManager().removeCorpse(corpseLoc);
                    plugin.getGameManager().startMeeting(p, p.getName() + " melaporkan mayat!");
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1.0f, 1.0f);
                } else {
                    p.sendMessage(ChatColor.RED + "Kamu sudah mati, tidak bisa melapor!");
                }
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        // Cegah interaksi dengan Task NPC
        if (e.getRightClicked().hasMetadata("TaskNPC")) {
            e.setCancelled(true);
            return;
        }

        if (plugin.getGameManager().getState() == GameManager.GameState.GAME_END || plugin.getGameManager().isMeetingRunning()) {
            e.setCancelled(true);
            return;
        }
        Player p = e.getPlayer();
        if (e.getRightClicked() instanceof ArmorStand entity) {
            if (entity.hasMetadata("bridgeNPC") ||
                    (entity.getCustomName() != null && (entity.getCustomName().contains("Pembangun Jembatan") || entity.getCustomName().contains("NPC")))) {
                e.setCancelled(true);
                plugin.getBridgeManager().depositPlanks(p);
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
                return;
            }
            if (entity.getCustomName() != null && entity.getCustomName().endsWith("Corpse")) {
                e.setCancelled(true);
                if (plugin.getGameManager().isPlayerAlive(p)) {
                    Location corpseLoc = entity.getLocation().getBlock().getLocation();
                    entity.remove();
                    plugin.getCorpseGlowManager().removeCorpse(corpseLoc);
                    plugin.getGameManager().startMeeting(p, p.getName() + " melaporkan mayat!");
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1.0f, 1.0f);
                } else {
                    p.sendMessage(ChatColor.RED + "Kamu sudah mati, tidak bisa melapor!");
                }
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerInteractBlock(PlayerInteractEvent e) {
        if (plugin.getGameManager().getState() == GameManager.GameState.GAME_END || plugin.getGameManager().isMeetingRunning()) {
            e.setCancelled(true);
            return;
        }
        Player p = e.getPlayer();
        Block b = e.getClickedBlock();
        if (b != null && b.getType() == Material.STONE_BUTTON
                && plugin.getGameManager().getMeetingCenter() != null
                && b.getLocation().distance(plugin.getGameManager().getMeetingCenter()) < 10) {
            e.setCancelled(true);
            if (plugin.getSabotageManager() != null && plugin.getSabotageManager().isSabotaging()) {
                p.sendMessage(ChatColor.RED + "Kamu tidak bisa menekan tombol emergency saat sabotase lampu aktif!");
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
                return;
            }
            if (!plugin.getGameManager().canUseEmergency()) {
                long remaining = plugin.getGameManager().getEmergencyRemaining();
                p.sendMessage(ChatColor.RED + "Tombol emergency cooldown! Tunggu " + remaining + " detik.");
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
                return;
            }
            plugin.getGameManager().setEmergencyCooldown();
            plugin.getGameManager().startMeeting(p, p.getName() + " menekan tombol emergency!");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
        }
    }

    @EventHandler
    public void onLeverInteract(PlayerInteractEvent e) {
        if (plugin.getGameManager().getState() == GameManager.GameState.GAME_END || plugin.getGameManager().isMeetingRunning()) {
            e.setCancelled(true);
            return;
        }
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null || block.getType() != Material.LEVER) return;
        Location genLoc = plugin.getSabotageManager().getGeneratorLocation();
        if (genLoc != null && genLoc.equals(block.getLocation())) {
            e.setCancelled(true);
            if (plugin.getSabotageManager().isSabotaging()) {
                plugin.getSabotageManager().startFixSabotage(e.getPlayer());
            } else {
                e.getPlayer().sendMessage(ChatColor.GRAY + "Generator tidak diperlukan saat ini.");
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onNetherStarClick(PlayerInteractEvent e) {
        if (plugin.getGameManager().getState() == GameManager.GameState.GAME_END || plugin.getGameManager().isMeetingRunning()) {
            e.setCancelled(true);
            return;
        }
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if (item != null && item.getType() == Material.NETHER_STAR) {
            if (plugin.getGameManager().getState() == GameManager.GameState.GAME_RUNNING &&
                    plugin.getGameManager().getRole(p) == RoleRegistry.Role.IMPOSTOR) {
                plugin.getSabotageGUIListener().openSabotageGUI(p);
                p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (plugin.getGameManager().getState() == GameManager.GameState.GAME_RUNNING && !plugin.getGameManager().isMeetingRunning()) {
            if (!plugin.getGameManager().isPlayerAlive(p)) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.RED + "Kamu sudah mati, tidak bisa chat saat game berjalan!");
                return;
            }
        }
        if (plugin.getGameManager().getState() == GameManager.GameState.GAME_RUNNING && plugin.getGameManager().isMeetingRunning()) {
            if (!plugin.getGameManager().isPlayerAlive(p)) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.RED + "Sebagai hantu, pesanmu tidak akan terlihat oleh pemain yang masih hidup!");
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
        e.getDrops().clear();
        e.setDroppedExp(0);
    }

    @EventHandler
    public void onPlayerMoveIntoVoid(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (e.getTo() == null) return;

        double y = e.getTo().getY();
        GameManager gm = plugin.getGameManager();
        GameManager.GameState state = gm.getState();

        boolean isExempt = gm.isVoidExempt(p.getUniqueId());
        if (isExempt && (state == GameManager.GameState.WAITING || state == GameManager.GameState.STARTING || state == GameManager.GameState.GAME_END)) {
            return;
        }

        int threshold;
        Location targetLocation = null;

        if (state == GameManager.GameState.WAITING || state == GameManager.GameState.STARTING || state == GameManager.GameState.GAME_END) {
            threshold = 0;
            targetLocation = gm.getLobbyLocation();
        } else {
            threshold = -70;
            targetLocation = e.getFrom();
        }

        if (y < threshold) {
            e.setCancelled(true);
            if (targetLocation != null) {
                p.teleport(targetLocation);
                p.sendMessage(ChatColor.YELLOW + "Kamu diselamatkan dari Void!");
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            } else {
                Location spawn = p.getWorld().getSpawnLocation();
                p.teleport(spawn);
            }
        }
    }
}