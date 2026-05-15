package com.jousting;

import org.bukkit.plugin.java.JavaPlugin;

public class JoustingPlugin extends JavaPlugin {
    private JoustingConfig config;
    private JoustingListener listener;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Load configuration
        config = new JoustingConfig(this);
        config.reload();
        
        // Register listener
        listener = new JoustingListener(this, config);
        getServer().getPluginManager().registerEvents(listener, this);
        
        // Register command
        getCommand("jousting").setExecutor(new JoustingCommand(this, config));
        
        getLogger().info("JoustingPlugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("JoustingPlugin disabled!");
    }

    public JoustingConfig getJoustingConfig() {
        return config;
    }
}