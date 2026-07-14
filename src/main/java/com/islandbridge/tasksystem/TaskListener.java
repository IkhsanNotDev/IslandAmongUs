package com.islandbridge.tasksystem;

import com.islandbridge.FakeTaskBook;
import com.islandbridge.GameManager;
import com.islandbridge.IslandBridgeAmongUs;
import com.islandbridge.RoleRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TaskListener implements Listener {
    private final IslandBridgeAmongUs plugin;
    private final Random random = new Random();
    private final Map<UUID, BukkitRunnable> activeTaskRunnables = new HashMap<>();
    private final Map<UUID, Location> taskStartLocations = new HashMap<>();
    private final FakeTaskBook fakeTaskBook;

    public TaskListener(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
        this.fakeTaskBook = plugin.getFakeTaskBook();
    }

    public void cancelAllActiveTasks() {
        for (Map.Entry<UUID, BukkitRunnable> entry : activeTaskRunnables.entrySet()) {
            entry.getValue().cancel();
        }
        activeTaskRunnables.clear();
        taskStartLocations.clear();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        var taskManager = plugin.getTaskManager();

        // NPC task distance check
        if (activeTaskRunnables.containsKey(uuid)) {
            Location start = taskStartLocations.get(uuid);
            if (start != null && e.getTo().distance(start) > 5.0) {
                p.sendMessage("§c§l[Gagal] §cTask dibatalkan karena kamu terlalu jauh!");
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                cancelTask(p);
            }
        }
        // SWIM task ditangani oleh scheduler di TaskManager
    }

    private void cancelTask(Player p) {
        UUID uuid = p.getUniqueId();
        BukkitRunnable run = activeTaskRunnables.remove(uuid);
        if (run != null) run.cancel();
        taskStartLocations.remove(uuid);
        plugin.getTaskManager().getIsInteractingWithNPC().remove(uuid);
    }

    @EventHandler
    public void onNPCHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        Entity entity = event.getEntity();
        if (!entity.hasMetadata("TaskNPC")) return;
        event.setCancelled(true);
        handleNPCInteraction(player, entity);
    }

    @EventHandler
    public void onNPCInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!entity.hasMetadata("TaskNPC")) return;
        event.setCancelled(true);
        handleNPCInteraction(event.getPlayer(), entity);
    }

    private void handleNPCInteraction(Player player, Entity npc) {
        final var taskManager = plugin.getTaskManager();
        RoleRegistry.Role role = plugin.getGameManager().getRole(player);
        UUID uuid = player.getUniqueId();

        if (plugin.getGameManager().getState() != GameManager.GameState.GAME_RUNNING) {
            player.sendMessage("§cGame sedang tidak berjalan!");
            return;
        }

        // Cek apakah player memiliki task NPC
        if (!taskManager.hasTask(player)) {
            player.sendMessage("§cKamu tidak memiliki task aktif! Tunggu task baru diberikan.");
            return;
        }

        if (!taskManager.isTaskNPC(player)) {
            player.sendMessage("§cKamu sedang memiliki task random (tebang/mancing/renang/farm), bukan task NPC! Selesaikan task randommu dulu.");
            return;
        }

        String expected = taskManager.getTargetNPC(player);
        if (expected == null) {
            player.sendMessage("§cTerjadi kesalahan pada task NPCmu. Silakan tunggu task baru.");
            taskManager.resetAllPlayerTasks();
            taskManager.startNPCTask(player);
            return;
        }

        String actual = npc.getCustomName();
        if (actual == null) {
            player.sendMessage("§cNPC tidak memiliki nama!");
            return;
        }

        String cleanActual = ChatColor.stripColor(actual).trim();
        String cleanExpected = ChatColor.stripColor(expected).trim();

        if (!cleanActual.equalsIgnoreCase(cleanExpected)) {
            player.sendMessage("§cBukan NPC yang kamu cari! Kamu harus mencari NPC dengan nama " + cleanExpected);
            return;
        }

        if (taskManager.getIsInteractingWithNPC().contains(uuid)) {
            player.sendMessage("§c[!] Kamu sedang dalam proses interaksi task dengan NPC ini!");
            return;
        }

        final long duration = npc.getMetadata("NPCDurasi").get(0).asLong();
        final String npcName = cleanExpected;
        final boolean isImpostorOrJoker = (role == RoleRegistry.Role.IMPOSTOR || role == RoleRegistry.Role.JOKER);

        taskManager.getIsInteractingWithNPC().add(uuid);
        player.sendMessage("§a[NPC " + npcName + "] Interaksi dimulai! Jangan pergi menjauh...");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 0.8f);
        taskStartLocations.put(uuid, player.getLocation());

        BukkitRunnable runnable = new BukkitRunnable() {
            long timeLeft = duration;
            @Override
            public void run() {
                if (plugin.getGameManager().getState() != GameManager.GameState.GAME_RUNNING) {
                    cancelTask(player);
                    cancel();
                    return;
                }
                if (!player.isOnline() || player.getLocation().distance(npc.getLocation()) > 5.0) {
                    player.sendMessage("§c§l[Gagal] §cInteraksi dibatalkan karena terlalu jauh!");
                    cancelTask(player);
                    cancel();
                    return;
                }
                if (timeLeft <= 0) {
                    // ★ Selesaikan task
                    if (isImpostorOrJoker) {
                        // Impostor/Joker: dapat fake reward (4 plank) dan selesaikan task nyata tanpa reward
                        fakeTaskBook.completeFakeTask(player, FakeTaskBook.FakeTaskType.NPC);
                        taskManager.completeTaskNoReward(player);
                        player.sendMessage("§a§l[Fake Task] Selesai! +1 progress, task nyata diganti.");
                    } else {
                        // Crewmate: selesaikan task dengan reward
                        taskManager.completeNPCTask(player);
                        player.sendMessage("§a§l[NPC " + npcName + "] Selesai! Kamu menyelesaikan task.");
                    }
                    taskManager.getIsInteractingWithNPC().remove(uuid);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    cancelTask(player);
                    cancel();
                    return;
                }
                if (timeLeft <= 5 && timeLeft > 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.7f, 1.0f);
                }
                player.sendActionBar(ChatColor.YELLOW + "NPC Task: " + timeLeft + " detik tersisa...");
                timeLeft--;
            }
        };
        runnable.runTaskTimer(plugin, 0L, 20L);
        activeTaskRunnables.put(uuid, runnable);
    }

    // ======================= TEBANG POHON =======================
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        var taskManager = plugin.getTaskManager();
        RoleRegistry.Role role = plugin.getGameManager().getRole(player);

        // ===== FAKE TASK untuk impostor/joker =====
        if (role == RoleRegistry.Role.IMPOSTOR || role == RoleRegistry.Role.JOKER) {
            // Tree
            boolean isTree = block.getType() == Material.OAK_LOG && taskManager.getCustomTrees().contains(block.getLocation());
            if (isTree) {
                fakeTaskBook.completeFakeTask(player, FakeTaskBook.FakeTaskType.TREE);
                // Selesaikan task nyata tanpa reward, agar berganti task
                taskManager.completeTaskNoReward(player);
                event.setCancelled(true);
                return;
            }

            // Farm
            Block below = block.getRelative(0, -1, 0);
            if (below.getType() == Material.FARMLAND && taskManager.isFarmland(below.getLocation())) {
                Material crop = block.getType();
                if (crop == Material.CARROTS || crop == Material.POTATOES || crop == Material.WHEAT || crop == Material.BEETROOTS) {
                    if (block.getBlockData() instanceof Ageable ageable && ageable.getAge() == ageable.getMaximumAge()) {
                        fakeTaskBook.completeFakeTask(player, FakeTaskBook.FakeTaskType.FARMING);
                        taskManager.completeTaskNoReward(player);
                        event.setCancelled(true);
                        player.sendMessage("§eFake task farming selesai! Task nyata diganti.");
                        return;
                    }
                }
            }
            // Untuk block lain, biarkan event berjalan (tapi impostor/joker tidak punya task nyata)
        }

        // ===== LOGIKA TASK NYATA =====
        if (plugin.getGameManager().getState() != GameManager.GameState.GAME_RUNNING) {
            event.setCancelled(true);
            return;
        }

        // === TEBANG ===
        if (block.getType() == Material.OAK_LOG && taskManager.getCustomTrees().contains(block.getLocation())) {
            if (!taskManager.hasTask(player) || taskManager.isTaskNPC(player)) {
                event.setCancelled(true);
                player.sendMessage("§cKamu tidak memiliki task menebang pohon!");
                return;
            }
            if (!taskManager.getTaskType(player).equals("TEBANG")) {
                event.setCancelled(true);
                player.sendMessage("§cTask kamu bukan menebang pohon!");
                return;
            }
            event.setDropItems(false);
            UUID uuid = player.getUniqueId();
            int currentWood = taskManager.getWoodCount().getOrDefault(uuid, 0) + 1;
            taskManager.getWoodCount().put(uuid, currentWood);
            player.sendMessage("§7Kayu ditebang (" + currentWood + "/2)");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);

            Location loc = block.getLocation();
            int delay = random.nextInt(6) + 10;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (loc.getBlock().getType() == Material.AIR) {
                    loc.getBlock().setType(Material.OAK_LOG);
                }
            }, delay * 20L);

            if (currentWood >= 2) {
                taskManager.completeTask(player);
            }
            return;
        }

        // === FARM (Crewmate) ===
        if (role == RoleRegistry.Role.CREWMATE && taskManager.isFarmTask(player)) {
            Material crop = block.getType();
            Material expected = taskManager.getFarmCropType(player);
            boolean match = false;
            if (expected == Material.WHEAT && crop == Material.WHEAT) match = true;
            else if (expected == Material.CARROTS && crop == Material.CARROTS) match = true;
            else if (expected == Material.POTATOES && crop == Material.POTATOES) match = true;
            else if (expected == Material.BEETROOTS && crop == Material.BEETROOTS) match = true;
            if (!match) {
                event.setCancelled(true);
                player.sendMessage("§cKamu harus memanen " + expected.name().toLowerCase() + "!");
                return;
            }

            Block below = block.getRelative(0, -1, 0);
            if (below.getType() != Material.FARMLAND || !taskManager.isFarmland(below.getLocation())) {
                event.setCancelled(true);
                player.sendMessage("§cIni bukan ladang task!");
                return;
            }

            if (!(block.getBlockData() instanceof Ageable ageable)) {
                event.setCancelled(true);
                return;
            }
            if (ageable.getAge() != ageable.getMaximumAge()) {
                event.setCancelled(true);
                player.sendMessage("§cTanaman belum tumbuh sempurna!");
                return;
            }

            event.setDropItems(true);
            taskManager.incrementFarmCount(player);
            int current = taskManager.getFarmCount(player);
            int required = taskManager.getFarmRequired(player);
            player.sendMessage("§ePanen " + crop.name().toLowerCase() + " (" + current + "/" + required + ")");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);

            Location cropLoc = block.getLocation();
            Material cropType = crop;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (cropLoc.getBlock().getType() == Material.AIR) {
                    cropLoc.getBlock().setType(cropType);
                    if (cropLoc.getBlock().getBlockData() instanceof Ageable newAge) {
                        newAge.setAge(newAge.getMaximumAge());
                        cropLoc.getBlock().setBlockData(newAge);
                    }
                }
            }, 20 * 20L);

            if (current >= required) {
                taskManager.completeTask(player);
                player.sendMessage("§a§l[Task] §aSelesai panen!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
        }
    }

    // ======================= MANCING =======================
    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        var taskManager = plugin.getTaskManager();
        RoleRegistry.Role role = plugin.getGameManager().getRole(player);

        if (plugin.getGameManager().getState() != GameManager.GameState.GAME_RUNNING) return;

        // ===== FAKE TASK untuk impostor/joker =====
        if (role == RoleRegistry.Role.IMPOSTOR || role == RoleRegistry.Role.JOKER) {
            if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
                fakeTaskBook.completeFakeTask(player, FakeTaskBook.FakeTaskType.FISHING);
                taskManager.completeTaskNoReward(player);
                return;
            }
        }

        // ===== LOGIKA TASK NYATA (Crewmate) =====
        if (!taskManager.hasTask(player) || taskManager.isTaskNPC(player)) return;
        if (!taskManager.getTaskType(player).equals("MANCING")) return;

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item caughtItem) {
            Material fish = caughtItem.getItemStack().getType();
            if (fish == Material.COD || fish == Material.SALMON || fish == Material.PUFFERFISH || fish == Material.TROPICAL_FISH) {
                UUID uuid = player.getUniqueId();
                int caught = taskManager.getFishCaught().getOrDefault(uuid, 0) + 1;
                int required = taskManager.getFishRequired().getOrDefault(uuid, 5);
                taskManager.getFishCaught().put(uuid, caught);
                player.sendMessage("§bBerhasil memancing! (" + caught + "/" + required + ")");
                player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 1.0f);
                if (caught >= required) {
                    taskManager.completeTask(player);
                }
            }
        }
    }

    // ======================= CHEST TASK =======================
    @EventHandler
    public void onChestOpen(InventoryOpenEvent event) {
        if (!(event.getInventory().getHolder() instanceof Chest chest)) return;
        var taskManager = plugin.getTaskManager();

        if (taskManager.getTaskChests().contains(chest.getLocation())) {
            Player player = (Player) event.getPlayer();
            RoleRegistry.Role role = plugin.getGameManager().getRole(player);

            if (plugin.getGameManager().getState() != GameManager.GameState.GAME_RUNNING) {
                event.setCancelled(true);
                return;
            }

            // ===== Impostor/Joker: trigger fake task dan selesaikan task nyata tanpa reward =====
            if (role == RoleRegistry.Role.IMPOSTOR || role == RoleRegistry.Role.JOKER) {
                if (!taskManager.hasTask(player) || taskManager.isTaskNPC(player)) {
                    event.setCancelled(true);
                    player.sendMessage("§cChest terkunci! Kamu tidak memiliki task aktif.");
                    return;
                }
                fakeTaskBook.completeFakeTask(player, FakeTaskBook.FakeTaskType.CHEST);
                taskManager.completeTaskNoReward(player);
                event.setCancelled(true);
                player.sendMessage("§eFake task chest selesai! Task nyata diganti.");
                return;
            }

            // ===== Crewmate: beri tool dari chest =====
            if (!taskManager.hasTask(player) || taskManager.isTaskNPC(player)) {
                event.setCancelled(true);
                player.sendMessage("§cChest terkunci! Kamu tidak memiliki task aktif.");
                return;
            }
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
            Inventory inv = chest.getInventory();
            inv.clear();
            ItemStack tool = taskManager.getToolForTask(player);
            inv.setItem(random.nextInt(inv.getSize()), tool);
        }
    }
}