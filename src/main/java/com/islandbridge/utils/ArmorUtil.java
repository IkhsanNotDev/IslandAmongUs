package com.islandbridge.utils;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Random;

public class ArmorUtil {
    private static final Random random = new Random();

    private static final Material[] HELMETS = {
            Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET,
            Material.IRON_HELMET, Material.GOLDEN_HELMET,
            Material.DIAMOND_HELMET, Material.NETHERITE_HELMET
    };
    private static final Material[] CHESTPLATES = {
            Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE,
            Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE,
            Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE
    };
    private static final Material[] LEGGINGS = {
            Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS,
            Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS,
            Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS
    };
    private static final Material[] BOOTS = {
            Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS,
            Material.IRON_BOOTS, Material.GOLDEN_BOOTS,
            Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS
    };

    public static void applyRandomArmor(ArmorStand stand) {
        if (stand == null) return;

        ItemStack helmet = randomArmorPiece(HELMETS);
        ItemStack chestplate = randomArmorPiece(CHESTPLATES);
        ItemStack leggings = randomArmorPiece(LEGGINGS);
        ItemStack boots = randomArmorPiece(BOOTS);

        stand.setHelmet(helmet);
        stand.setChestplate(chestplate);
        stand.setLeggings(leggings);
        stand.setBoots(boots);

        colorizeIfLeather(helmet);
        colorizeIfLeather(chestplate);
        colorizeIfLeather(leggings);
        colorizeIfLeather(boots);
    }

    private static ItemStack randomArmorPiece(Material[] materials) {
        Material mat = materials[random.nextInt(materials.length)];
        return new ItemStack(mat);
    }

    private static void colorizeIfLeather(ItemStack item) {
        if (item != null && item.getType().name().contains("LEATHER")) {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            item.setItemMeta(meta);
        }
    }
}