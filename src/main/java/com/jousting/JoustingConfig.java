package com.jousting;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class JoustingConfig {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    
    private Material lanceItem;
    private double highTierSpeedThreshold;
    private double lowTierMaxDamage;
    private double highTierMaxDamage;
    private double minimumMomentumDistance;
    private int knockoffChanceZeroMomentum;
    private int knockoffChanceFullMomentum;
    private String hitSound;
    private String knockoffSound;

    public JoustingConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // Load lance item
        String lanceItemStr = config.getString("lance-item", "END_ROD");
        try {
            this.lanceItem = Material.valueOf(lanceItemStr);
        } catch (IllegalArgumentException e) {
            this.lanceItem = Material.END_ROD;
            plugin.getLogger().warning("Invalid lance item: " + lanceItemStr + ", using END_ROD");
        }
        
        // Load speed tier threshold
        this.highTierSpeedThreshold = config.getDouble("high-tier-speed-threshold", 1.5);
        
        // Load damage values
        this.lowTierMaxDamage = config.getDouble("low-tier-max-damage", 3.0);
        this.highTierMaxDamage = config.getDouble("high-tier-max-damage", 6.0);
        
        // Load momentum distance
        this.minimumMomentumDistance = config.getDouble("minimum-momentum-distance", 5.0);
        
        // Load knockoff chances
        this.knockoffChanceZeroMomentum = config.getInt("knockoff-chance-zero-momentum", 5);
        this.knockoffChanceFullMomentum = config.getInt("knockoff-chance-full-momentum", 70);
        
        // Load sounds
        this.hitSound = config.getString("sounds.hit", "entity.player.attack.strong");
        this.knockoffSound = config.getString("sounds.knockoff", "entity.item.break");
    }

    public Material getLanceItem() { return lanceItem; }
    public double getHighTierSpeedThreshold() { return highTierSpeedThreshold; }
    public double getLowTierMaxDamage() { return lowTierMaxDamage; }
    public double getHighTierMaxDamage() { return highTierMaxDamage; }
    public double getMinimumMomentumDistance() { return minimumMomentumDistance; }
    public int getKnockoffChanceZeroMomentum() { return knockoffChanceZeroMomentum; }
    public int getKnockoffChanceFullMomentum() { return knockoffChanceFullMomentum; }
    public String getHitSound() { return hitSound; }
    public String getKnockoffSound() { return knockoffSound; }
}