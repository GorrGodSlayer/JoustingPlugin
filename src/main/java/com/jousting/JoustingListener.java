package com.jousting;

import org.bukkit.Sound;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public class JoustingListener implements Listener {
    private final JoustingPlugin plugin;
    private final JoustingConfig config;
    private final Random random = new Random();

    public JoustingListener(JoustingPlugin plugin, JoustingConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!(player.getVehicle() instanceof Horse)) {
            return;
        }

        // Track momentum while riding
        MomentumTracker.trackLocation(
            player.getUniqueId(),
            player.getLocation().getX(),
            player.getLocation().getY(),
            player.getLocation().getZ()
        );
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Check if damager is a player on a horse holding a lance
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player damager = (Player) event.getDamager();
        if (!(damager.getVehicle() instanceof Horse)) {
            return;
        }

        Horse horse = (Horse) damager.getVehicle();
        ItemStack mainHand = damager.getInventory().getItemInMainHand();
        
        if (mainHand.getType() != config.getLanceItem()) {
            return;
        }

        // This is a jousting attack
        event.setCancelled(true);

        // Get momentum and calculate damage
        double momentum = MomentumTracker.getMomentumDistance(damager.getUniqueId());
        double maxDamage = HorseSpeedTier.getMaxDamage(horse, config);
        double damage = HorseSpeedTier.calculateDamage(
            momentum,
            config.getMinimumMomentumDistance(),
            maxDamage
        );

        // Deal damage if momentum is sufficient
        if (damage > 0) {
            event.getEntity().damage(damage, damager);
            
            // Play hit sound
            playSound(damager, config.getHitSound());
            
            // Check for knockoff
            if (event.getEntity() instanceof Player) {
                Player target = (Player) event.getEntity();
                if (target.getVehicle() instanceof Horse) {
                    if (shouldKnockoff(momentum, config.getMinimumMomentumDistance())) {
                        target.eject();
                        playSound(damager, config.getKnockoffSound());
                    }
                }
            }
            
            // Apply knockback
            Vector direction = damager.getLocation().getDirection();
            Vector knockback = direction.multiply(0.5);
            event.getEntity().setVelocity(event.getEntity().getVelocity().add(knockback));
        }

        // Reset momentum after hit
        MomentumTracker.resetMomentum(damager.getUniqueId());
    }

    private boolean shouldKnockoff(double momentum, double minDistance) {
        int chance;
        
        if (momentum < minDistance) {
            chance = config.getKnockoffChanceZeroMomentum();
        } else {
            // Interpolate between zero and full momentum chances
            double normalizedMomentum = Math.min(momentum / (minDistance * 3), 1.0);
            double fullChance = config.getKnockoffChanceFullMomentum();
            double zeroChance = config.getKnockoffChanceZeroMomentum();
            chance = (int) (zeroChance + (fullChance - zeroChance) * normalizedMomentum);
        }
        
        return random.nextInt(100) < chance;
    }

    private void playSound(Player player, String soundKey) {
        try {
            Sound sound = Sound.valueOf(soundKey);
            player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound key: " + soundKey);
        }
    }
}