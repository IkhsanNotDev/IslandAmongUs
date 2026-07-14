package com.islandbridge;

import org.bukkit.ChatColor;

public class RoleRegistry {
    public enum Role {
        CREWMATE("Crewmate", ChatColor.BLUE),
        IMPOSTOR("Impostor", ChatColor.RED),
        JOKER("Joker", ChatColor.LIGHT_PURPLE);

        private final String display;
        private final ChatColor color;
        Role(String display, ChatColor color) { this.display = display; this.color = color; }
        public String getDisplay() { return color + display; }
    }
}