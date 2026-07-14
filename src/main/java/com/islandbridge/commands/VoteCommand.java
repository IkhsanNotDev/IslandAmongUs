package com.islandbridge.commands;

import com.islandbridge.IslandBridgeAmongUs;
import com.islandbridge.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.UUID;

public class VoteCommand implements CommandExecutor {
    private final IslandBridgeAmongUs plugin;

    public VoteCommand(IslandBridgeAmongUs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player voter)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (plugin.getGameManager().getState() != GameManager.GameState.MEETING) {
            voter.sendMessage(ChatColor.RED + "Vote hanya bisa dilakukan saat meeting!");
            return true;
        }

        if (!plugin.getGameManager().isPlayerAlive(voter)) {
            voter.sendMessage(ChatColor.RED + "Kamu sudah mati, tidak bisa vote!");
            return true;
        }

        // Buka GUI voting
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Vote to Eject");
        java.util.List<UUID> targets = new ArrayList<>(plugin.getGameManager().getLivingPlayers());
        targets.remove(voter.getUniqueId());
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
        voter.openInventory(gui);
        return true;
    }
}