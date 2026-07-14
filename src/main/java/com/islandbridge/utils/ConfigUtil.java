package com.islandbridge.utils;

import com.islandbridge.IslandBridgeAmongUs;

public class ConfigUtil {
    public static void init(IslandBridgeAmongUs plugin) {
        plugin.saveDefaultConfig();
        // Bisa memuat lokasi-lokasi yang tersimpan di config jika diperlukan
    }
}