package com.jousting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Random;

public class JoustingListener implements Listener {
    private final JoustingPlugin plugin;
    private final JoustingConfig config;
    private final Random random = new Random();
    private final NamespacedKey lanceUsesKey;

    public JoustingListener(JoustingPlugin plugin, JoustingConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.lanceUsesKey = new NamespacedKey(plugin, "lance_uses");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!(player.getVehicle() instanceof Horse horse)) {
            return;
        }

        // If horse is too slow, bleed momentum back to zero and stop tracking
        double speed = horse.getVelocity().length();
        if (speed < config.getMinimumRunSpeed()) {
            MomentumTracker.resetMomentum(player.getUniqueId());
            return;
        }

        // Track momentum while riding at speed
        MomentumTracker.trackLocation(
            player.getUniqueId(),
            player.getLocation().getX(),
            player.getLocation().getY(),
            player.getLocation().getZ()
        );
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) {
            return;
        }
        if (!(damager.getVehicle() instanceof Horse horse)) {
            return;
        }

        ItemStack mainHand = damager.getInventory().getItemInMainHand();
        if (!config.isLanceItem(mainHand.getType())) {
            return;
        }

        // This is a jousting attack — cancel vanilla damage
        event.setCancelled(true);

        // Check lance durability
        ItemMeta meta = mainHand.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int uses = pdc.getOrDefault(lanceUsesKey, PersistentDataType.INTEGER, 0);

        if (uses >= config.getLanceMaxUses()) {
            // Lance is broken — no damage, no sound
            return;
        }

        // Get momentum and calculate damage (lance tier adds a flat bonus to the horse-speed cap)
        double momentum = MomentumTracker.getMomentumDistance(damager.getUniqueId());
        double baseMaxDamage = HorseSpeedTier.getMaxDamage(horse, config);
        double lanceBonus = config.getLanceDamageBonus(mainHand.getType());
        double damage = HorseSpeedTier.calculateDamage(
            momentum,
            config.getMinimumMomentumDistance(),
            baseMaxDamage + lanceBonus
        );

        if (damage > 0) {
            event.getEntity().damage(damage, damager);

            // Play hit sound
            playSound(damager, config.getHitSound());

            // Check for knockoff (mounted targets only)
            if (event.getEntity() instanceof Player target) {
                if (target.getVehicle() instanceof Horse) {
                    if (shouldKnockoff(momentum, config.getMinimumMomentumDistance())) {
                        target.eject();
                        playSound(damager, config.getKnockoffSound());
                    }
                }
            }

            // Apply knockback
            Vector direction = damager.getLocation().getDirection();
            event.getEntity().setVelocity(event.getEntity().getVelocity().add(direction.multiply(0.5)));

            // Consume one lance use
            uses++;
            pdc.set(lanceUsesKey, PersistentDataType.INTEGER, uses);
            if (uses >= config.getLanceMaxUses()) {
                meta.displayName(Component.text("Broken Lance").color(NamedTextColor.DARK_GRAY));
            }
            mainHand.setItemMeta(meta);
        }

        // Reset momentum after every hit attempt
        MomentumTracker.resetMomentum(damager.getUniqueId());
    }

    private boolean shouldKnockoff(double momentum, double minDistance) {
        int chance;
        if (momentum < minDistance) {
            chance = config.getKnockoffChanceZeroMomentum();
        } else {
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
