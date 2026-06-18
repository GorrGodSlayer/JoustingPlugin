package com.jousting;

import org.bukkit.plugin.java.JavaPlugin;

public class JoustingPlugin extends JavaPlugin {
    private JoustingConfig config;
    private JoustingListener listener;
    private MomentumBarManager barManager;
    private CooldownManager cooldowns;
    private LanceItems lances;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        config = new JoustingConfig(this);
        config.reload();

        barManager = new MomentumBarManager();
        cooldowns = new CooldownManager();
        lances = new LanceItems(this);

        listener = new JoustingListener(this, config, barManager, cooldowns, lances);
        getServer().getPluginManager().registerEvents(listener, this);

        // Per-tick momentum poll (drives the momentum bar reliably for mounted players).
        new MomentumTask(this, config, barManager).runTaskTimer(this, 1L, 1L);

        getCommand("jousting").setExecutor(new JoustingCommand(this, config, lances));

        getLogger().info("JoustingPlugin enabled!");
    }

    @Override
    public void onDisable() {
        if (barManager != null) barManager.clearAll();
        if (cooldowns != null) cooldowns.clearAll();
        MomentumTracker.clearAll();
        getLogger().info("JoustingPlugin disabled!");
    }

    public JoustingConfig getJoustingConfig() {
        return config;
    }
}
