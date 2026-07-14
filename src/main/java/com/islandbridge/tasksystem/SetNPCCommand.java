package com.islandbridge.tasksystem;

import com.islandbridge.IslandBridgeAmongUs;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;

import java.util.Random;

public class SetNPCCommand implements CommandExecutor {
    private final IslandBridgeAmongUs plugin;
    private final Random random = new Random();

    public SetNPCCommand(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length < 2) {
            player.sendMessage("§cGunakan: /setnpc <displayname> <durasi(s/m)>");
            return true;
        }

        String displayName = ChatColor.translateAlternateColorCodes('&', args[0]);
        String timeRaw = args[1];
        long seconds = parseTimeToSeconds(timeRaw);
        if (seconds <= 0) {
            player.sendMessage("§cFormat waktu salah! Contoh: 30s atau 5m");
            return true;
        }

        Location spawnLoc = player.getLocation();
        ArmorStand npc = (ArmorStand) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        npc.setCustomName(displayName);
        npc.setCustomNameVisible(true);
        npc.setArms(true);
        npc.setBasePlate(false);
        npc.setGravity(true);
        npc.setInvulnerable(false);
        npc.setMetadata("TaskNPC", new FixedMetadataValue(plugin, true));
        npc.setMetadata("NPCDurasi", new FixedMetadataValue(plugin, seconds));

        applyRandomClothing(npc);
        applyRandomPose(npc);

        plugin.getTaskManager().registerNPC(spawnLoc, displayName, seconds);
        player.sendMessage("§a[Sukses] NPC [" + displayName + "§a] dibuat dengan durasi " + seconds + " detik!");
        return true;
    }

    private long parseTimeToSeconds(String input) {
        try {
            char unit = input.charAt(input.length() - 1);
            long value = Long.parseLong(input.substring(0, input.length() - 1));
            if (unit == 's') return value;
            if (unit == 'm') return value * 60;
        } catch (Exception ignored) {}
        return -1;
    }

    private void applyRandomClothing(ArmorStand stand) {
        Material[] helmets = {Material.LEATHER_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET};
        stand.getEquipment().setHelmet(new ItemStack(helmets[random.nextInt(helmets.length)]));
        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) chest.getItemMeta();
        if (meta != null) {
            meta.setColor(Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            chest.setItemMeta(meta);
        }
        stand.getEquipment().setChestplate(chest);
    }

    private void applyRandomPose(ArmorStand stand) {
        stand.setHeadPose(new EulerAngle(random.nextDouble() * 0.4, random.nextDouble() * 0.4, random.nextDouble() * 0.4));
        stand.setLeftArmPose(new EulerAngle(random.nextDouble() * 1.2, random.nextDouble() * 0.4, random.nextDouble() * 0.4));
        stand.setRightArmPose(new EulerAngle(random.nextDouble() * 1.2, random.nextDouble() * 0.4, random.nextDouble() * 0.4));
    }
}