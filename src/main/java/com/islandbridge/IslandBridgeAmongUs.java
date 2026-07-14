// ============================================================
// FILE: IslandBridgeAmongUs.java (LENGKAP - hologram dihapus)
// ============================================================
package com.islandbridge;

import com.islandbridge.commands.*;
import com.islandbridge.listeners.GameListener;
import com.islandbridge.listeners.MeetingListener;
import com.islandbridge.listeners.SkinListener;
import com.islandbridge.tasksystem.*;
import com.islandbridge.utils.ConfigUtil;
import com.islandbridge.utils.VoiceChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;

public class IslandBridgeAmongUs extends JavaPlugin {
    private static IslandBridgeAmongUs instance;
    private GameManager gameManager;
    private BridgeManager bridgeManager;
    private com.islandbridge.tasksystem.TaskManager taskManager;
    private VotingSystem votingSystem;
    private SabotageManager sabotageManager;
    private ModeratorManager moderatorManager;
    private SkinWalker skinWalker;
    private HeartbeatRadar heartbeatRadar;
    private FakeTaskBook fakeTaskBook;
    private SabotageGUIListener sabotageGUIListener;
    private ScoreboardManager scoreboardManager;
    private ArmorStand creditsHologram = null; // tidak digunakan lagi
    private CorpseGlowManager corpseGlowManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        ConfigUtil.init(this);

        gameManager = new GameManager(this);
        bridgeManager = new BridgeManager(this);
        taskManager = new com.islandbridge.tasksystem.TaskManager(this);
        votingSystem = new VotingSystem(this);
        sabotageManager = new SabotageManager(this);
        moderatorManager = new ModeratorManager(this);
        skinWalker = new SkinWalker(this);
        heartbeatRadar = new HeartbeatRadar(this);
        fakeTaskBook = new FakeTaskBook(this);
        sabotageGUIListener = new SabotageGUIListener(this);
        scoreboardManager = new ScoreboardManager(this);
        corpseGlowManager = new CorpseGlowManager(this);

        taskManager.loadData();

        getCommand("ib").setExecutor(new CommandHandler(this));
        getCommand("vote").setExecutor(new VoteCommand(this));
        getCommand("setnpc").setExecutor(new SetNPCCommand(this));
        getCommand("delnpc").setExecutor(new DeleteNPCCommand(this));
        getCommand("listnpc").setExecutor(new ListNPCCommand(this));
        getCommand("setpohon").setExecutor(new SetPohonCommand(this));
        getCommand("setchest").setExecutor(new SetChestCommand(this));
        getCommand("task").setExecutor(new TaskCommand(this));
        getCommand("skin").setExecutor(new SkinCommand(this));

        getCommand("ib").setTabCompleter(new IBCommandTabCompleter(this));

        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new MeetingListener(this), this);
        getServer().getPluginManager().registerEvents(new BridgeInteractionListener(this, bridgeManager), this);
        getServer().getPluginManager().registerEvents(new TaskListener(this), this);
        getServer().getPluginManager().registerEvents(new WeaponListener(), this);
        getServer().getPluginManager().registerEvents(sabotageGUIListener, this);
        getServer().getPluginManager().registerEvents(new SkinListener(this), this);
        getServer().getPluginManager().registerEvents(new JokerMapListener(this), this);

        VoiceChatUtil.init(this);
        scoreboardManager.startUpdater();

        // ★ HAPUS PEMANGGILAN spawnCreditsHologram() ★
        // spawnCreditsHologram();

        getLogger().info("IslandBridgeAmongUs enabled!");
    }

    @Override
    public void onDisable() {
        if (taskManager != null) taskManager.saveData();
        if (scoreboardManager != null) scoreboardManager.stopUpdater();
        if (creditsHologram != null) creditsHologram.remove();
        if (corpseGlowManager != null) corpseGlowManager.clearAllCorpses();
        getLogger().info("IslandBridgeAmongUs disabled!");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        taskManager.loadData();
        gameManager.loadLocationsFromConfig();
        sabotageManager.loadConfig();
        bridgeManager.loadConfig();
        taskManager.reloadAllNPCs();
        bridgeManager.respawnNPC();
        // ★ hologram tidak dipanggil lagi
        // spawnCreditsHologram();
        getLogger().info("Configuration reloaded and NPCs respawned!");
    }

    // ★ METHOD INI DIKOSONGKAN ATAU DIHAPUS ★
    public void spawnCreditsHologram() {
        // Tidak melakukan apa-apa (hologram dihapus)
        // Jika ingin benar-benar dihapus, bisa hapus method ini.
    }

    public static IslandBridgeAmongUs getInstance() { return instance; }
    public GameManager getGameManager() { return gameManager; }
    public BridgeManager getBridgeManager() { return bridgeManager; }
    public com.islandbridge.tasksystem.TaskManager getTaskManager() { return taskManager; }
    public VotingSystem getVotingSystem() { return votingSystem; }
    public SabotageManager getSabotageManager() { return sabotageManager; }
    public ModeratorManager getModeratorManager() { return moderatorManager; }
    public SkinWalker getSkinWalker() { return skinWalker; }
    public HeartbeatRadar getHeartbeatRadar() { return heartbeatRadar; }
    public FakeTaskBook getFakeTaskBook() { return fakeTaskBook; }
    public SabotageGUIListener getSabotageGUIListener() { return sabotageGUIListener; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public CorpseGlowManager getCorpseGlowManager() { return corpseGlowManager; }
}