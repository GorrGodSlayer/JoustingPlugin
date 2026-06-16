package com.jousting;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class JoustingConfig {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    private Map<Material, Double> lanceTiers;
    private int lanceMaxUses;
    private double highTierSpeedThreshold;
    private double lowTierMaxDamage;
    private double highTierMaxDamage;
    private double minimumMomentumDistance;
    private double minimumRunSpeed;
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

        // Load lance tiers
        this.lanceTiers = new HashMap<>();
        ConfigurationSection tierSection = config.getConfigurationSection("lance-tiers");
        if (tierSection != null) {
            for (String key : tierSection.getKeys(false)) {
                try {
                    Material mat = Material.valueOf(key);
                    double bonus = tierSection.getDouble(key + ".damage-bonus", 0.0);
                    lanceTiers.put(mat, bonus);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid lance tier material: " + key + ", skipping");
                }
            }
        }
        if (lanceTiers.isEmpty()) {
            plugin.getLogger().warning("No valid lance tiers configured, falling back to IRON_SPEAR");
            lanceTiers.put(Material.IRON_SPEAR, 0.0);
        }

        // Load lance max uses
        this.lanceMaxUses = config.getInt("lance-max-uses", 10);

        // Load speed tier threshold
        this.highTierSpeedThreshold = config.getDouble("high-tier-speed-threshold", 1.5);

        // Load damage values
        this.lowTierMaxDamage = config.getDouble("low-tier-max-damage", 3.0);
        this.highTierMaxDamage = config.getDouble("high-tier-max-damage", 6.0);

        // Load momentum values
        this.minimumMomentumDistance = config.getDouble("minimum-momentum-distance", 5.0);
        this.minimumRunSpeed = config.getDouble("minimum-run-speed", 0.15);

        // Load knockoff chances
        this.knockoffChanceZeroMomentum = config.getInt("knockoff-chance-zero-momentum", 5);
        this.knockoffChanceFullMomentum = config.getInt("knockoff-chance-full-momentum", 70);

        // Load sounds
        this.hitSound = config.getString("sounds.hit", "entity.player.attack.strong");
        this.knockoffSound = config.getString("sounds.knockoff", "entity.item.break");
    }

    public boolean isLanceItem(Material material) { return lanceTiers.containsKey(material); }
    public double getLanceDamageBonus(Material material) { return lanceTiers.getOrDefault(material, 0.0); }
    public int getLanceMaxUses() { return lanceMaxUses; }
    public double getHighTierSpeedThreshold() { return highTierSpeedThreshold; }
    public double getLowTierMaxDamage() { return lowTierMaxDamage; }
    public double getHighTierMaxDamage() { return highTierMaxDamage; }
    public double getMinimumMomentumDistance() { return minimumMomentumDistance; }
    public double getMinimumRunSpeed() { return minimumRunSpeed; }
    public int getKnockoffChanceZeroMomentum() { return knockoffChanceZeroMomentum; }
    public int getKnockoffChanceFullMomentum() { return knockoffChanceFullMomentum; }
    public String getHitSound() { return hitSound; }
    public String getKnockoffSound() { return knockoffSound; }
}
