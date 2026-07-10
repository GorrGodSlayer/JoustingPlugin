package com.jousting;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Random;

public class JoustingListener implements Listener {
    private final JoustingPlugin plugin;
    private final JoustingConfig config;
    private final MomentumBarManager barManager;
    private final CooldownManager cooldowns;
    private final LanceItems lances;
    private final Random random = new Random();

    public JoustingListener(JoustingPlugin plugin, JoustingConfig config,
                            MomentumBarManager barManager, CooldownManager cooldowns,
                            LanceItems lances) {
        this.plugin = plugin;
        this.config = config;
        this.barManager = barManager;
        this.cooldowns = cooldowns;
        this.lances = lances;
    }

    // ---------------------------------------------------------------- movement / momentum

    // Momentum tracking + the momentum bar are handled by MomentumTask (per-tick poll).

    // ---------------------------------------------------------------- combat

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Only direct melee strikes count as lance hits. Thorns and sweep damage also
        // arrive here with the rider as damager and must not trigger a charge.
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(damager.getVehicle() instanceof Horse horse)) return;

        ItemStack mainHand = damager.getInventory().getItemInMainHand();
        if (!config.isLanceItem(mainHand.getType())) return;
        // Without jousting.use (default true) a lance is just an ordinary melee weapon.
        if (!damager.hasPermission("jousting.use")) return;

        // Non-living targets (item frames, paintings, boats, minecarts, end crystals) are not
        // jousting targets. Neither are armour stands, which are LivingEntity but would
        // otherwise burn a lance use and start a cooldown when tapped. Leave them to vanilla.
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (victim instanceof ArmorStand) return;

        // Below this point every early exit returns WITHOUT cancelling. Cancelling would strip
        // the vanilla melee damage too, leaving the spear inert against everything for the
        // whole cooldown; only the lance charge is suppressed, not the ordinary swing.

        // Cooldown gate: no lance damage during cooldown.
        if (cooldowns.isOnCooldown(damager.getUniqueId())) return;

        LanceTier tier = LanceTier.fromMaterial(mainHand.getType());
        int maxUses = config.getLanceMaxUses(mainHand.getType());
        int uses = lances.getUses(mainHand);

        // Broken lance is decorative only: no charge damage, no cooldown.
        if (uses >= maxUses) return;

        // --- damage calculation -------------------------------------------------
        double momentum = MomentumTracker.getMomentumDistance(damager.getUniqueId());
        double speedCap = HorseSpeedTier.getMaxDamage(horse, config);
        double base = HorseSpeedTier.calculateDamage(
                momentum,
                config.getMinimumMomentumDistance(),
                config.getFullMomentumDistance(),
                speedCap + config.getLanceDamageBonus(mainHand.getType()));

        if (base <= 0) {
            // Not enough momentum to land a charge; fall through to vanilla melee.
            // No use consumed, no cooldown.
            return;
        }

        // Lance damage depends ONLY on momentum, horse tier and lance tier.
        // Sharpness and Strength are deliberately ignored so buffs can't inflate a charge.
        double damage = DamageCalculator.clamp(base, config.getFinalDamageHardCap());

        // --- shield interaction -------------------------------------------------
        boolean blocked = victim instanceof Player vp && vp.isBlocking();
        if (blocked) {
            event.setCancelled(true); // shield absorbs; no health damage
            Player target = (Player) victim;
            EquipmentSlot hand = shieldHand(target);
            if (hand != null) {
                damageShield(target, hand);
            }
            if (config.isShieldKnockbackOnBlock()) {
                applyKnockback(damager, target);
            }
            playSound(target, config.getShieldSound());
            consumeUse(mainHand, uses, tier, maxUses);
            if (config.isShieldCooldownOnBlock()) {
                cooldowns.start(damager.getUniqueId(), config.getLanceCooldownMs());
            }
            MomentumTracker.resetMomentum(damager.getUniqueId());
            barManager.update(damager, 0.0, tier);
            return; // blocked hit deals no health damage
        }

        // --- normal hit ---------------------------------------------------------
        // Set the damage on THIS event instead of calling victim.damage(), which would
        // re-fire EntityDamageByEntityEvent and recurse into this handler (StackOverflow).
        // Config damage is in HEARTS; Minecraft damage is half-hearts (2 points = 1 heart).
        event.setDamage(damage * 2.0);
        playSound(damager, config.getHitSound());
        applyKnockback(damager, victim);

        if (config.isDebugDamage()) {
            double frac = HorseSpeedTier.momentumFraction(momentum,
                    config.getMinimumMomentumDistance(), config.getFullMomentumDistance());
            damager.sendMessage("§e[Joust] §f" + String.format("%.1f", damage) + " hearts §7| momentum "
                    + (int) Math.round(frac * 100) + "% | horse " + HorseSpeedTier.tierOf(horse, config)
                    + " | lance " + (tier == null ? "?" : tier.name()));
        }

        // JOUSTING mode (mounted vs mounted) only: chance to dismount.
        boolean joustMode = victim instanceof Player tp && tp.getVehicle() instanceof Horse;
        if (joustMode && shouldKnockoff(momentum)) {
            victim.leaveVehicle();
            playSound(damager, config.getKnockoffSound());
        }

        consumeUse(mainHand, uses, tier, maxUses);
        cooldowns.start(damager.getUniqueId(), config.getLanceCooldownMs());
        MomentumTracker.resetMomentum(damager.getUniqueId());
        barManager.update(damager, 0.0, tier);
    }

    // ---------------------------------------------------------------- cleanup

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (event.getExited() instanceof Player player) {
            MomentumTracker.resetMomentum(player.getUniqueId());
            barManager.remove(player.getUniqueId());
            // Deliberately NOT clearing the cooldown: dismount/remount would reset it and
            // bypass lance-cooldown-ms entirely. It expires on its own, and onQuit clears it.
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        MomentumTracker.resetMomentum(player.getUniqueId());
        barManager.remove(player.getUniqueId());
        cooldowns.clear(player.getUniqueId());
        // MomentumTask only sees online players, so it can't clean this up itself.
        plugin.getMomentumTask().forget(player.getUniqueId());
    }

    // ---------------------------------------------------------------- helpers

    private void consumeUse(ItemStack lance, int currentUses, LanceTier tier, int maxUses) {
        // LanceItems.setUses refreshes durability lore and renames to "Broken Lance" at the cap.
        lances.setUses(lance, currentUses + 1, tier, maxUses);
    }

    private EquipmentSlot shieldHand(Player target) {
        if (target.getInventory().getItemInOffHand().getType() == Material.SHIELD) {
            return EquipmentSlot.OFF_HAND;
        }
        if (target.getInventory().getItemInMainHand().getType() == Material.SHIELD) {
            return EquipmentSlot.HAND;
        }
        return null;
    }

    private void damageShield(Player target, EquipmentSlot hand) {
        ItemStack shield = hand == EquipmentSlot.OFF_HAND
                ? target.getInventory().getItemInOffHand()
                : target.getInventory().getItemInMainHand();
        if (shield == null) return;
        ItemMeta meta = shield.getItemMeta();
        if (!(meta instanceof Damageable dmg)) return;

        int max = shield.getType().getMaxDurability();
        int newDamage = dmg.getDamage() + config.getShieldDurabilityDamage();
        if (max > 0 && newDamage >= max) {
            shield.setAmount(0);
            playSound(target, config.getBreakSound());
        } else {
            dmg.setDamage(newDamage);
            shield.setItemMeta(meta);
        }
        if (hand == EquipmentSlot.OFF_HAND) {
            target.getInventory().setItemInOffHand(shield);
        } else {
            target.getInventory().setItemInMainHand(shield);
        }
    }

    private void applyKnockback(Player source, LivingEntity victim) {
        Vector dir = source.getLocation().getDirection();
        dir.setY(0);
        if (dir.lengthSquared() == 0) dir = new Vector(0, 0, 1);
        dir.normalize().multiply(config.getKnockbackStrength()).setY(0.25);

        // A passenger's position is driven by its vehicle, so velocity applied to a mounted
        // player is discarded. Push whatever is actually carrying them.
        Entity target = victim.getVehicle() != null ? victim.getVehicle() : victim;
        target.setVelocity(target.getVelocity().add(dir));
    }

    private boolean shouldKnockoff(double momentum) {
        double normalized = HorseSpeedTier.momentumFraction(momentum,
                config.getMinimumMomentumDistance(), config.getFullMomentumDistance());
        double zero = config.getKnockoffChanceZeroMomentum();
        double full = config.getKnockoffChanceFullMomentum();
        int chance = (int) Math.round(zero + (full - zero) * normalized);
        return random.nextInt(100) < chance;
    }

    private void playSound(Player player, Sound sound) {
        if (sound == null) return; // invalid key already warned about at config load
        player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }
}
