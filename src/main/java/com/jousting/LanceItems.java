package com.jousting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Builds and inspects lance items. Remaining uses are stored in the item's
 * PersistentDataContainer under "lance_uses"; a tag also marks the item as a
 * plugin-issued lance so random rods aren't treated as lances unless configured.
 */
public class LanceItems {
    private final Plugin plugin;
    private final NamespacedKey usesKey;

    public LanceItems(Plugin plugin) {
        this.plugin = plugin;
        this.usesKey = new NamespacedKey(plugin, "lance_uses");
    }

    public NamespacedKey getUsesKey() { return usesKey; }

    /** Create a fresh lance for the given tier. */
    public ItemStack create(LanceTier tier, JoustingConfig config) {
        ItemStack item = new ItemStack(tier.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, 0);
            applyDisplay(meta, tier, 0, config.getLanceMaxUses(tier.getMaterial()));
            item.setItemMeta(meta);
        }
        return item;
    }

    public int getUses(ItemStack item) {
        if (item == null) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        return meta.getPersistentDataContainer().getOrDefault(usesKey, PersistentDataType.INTEGER, 0);
    }

    /** Persist new use count and refresh display; renames to "Broken Lance" when spent. */
    public void setUses(ItemStack item, int uses, LanceTier tier, int maxUses) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, uses);
        if (uses >= maxUses) {
            meta.displayName(Component.text("Broken Lance").color(NamedTextColor.DARK_GRAY));
            meta.lore(List.of(Component.text("Decorative only").color(NamedTextColor.DARK_GRAY)));
        } else {
            applyDisplay(meta, tier, uses, maxUses);
        }
        item.setItemMeta(meta);
    }

    public boolean isBroken(ItemStack item, int maxUses) {
        return getUses(item) >= maxUses;
    }

    private void applyDisplay(ItemMeta meta, LanceTier tier, int uses, int maxUses) {
        String name = tier != null ? tier.getDisplayName() : "Lance";
        NamedTextColor color = tier != null ? tier.getColor() : NamedTextColor.WHITE;
        meta.displayName(Component.text(name).color(color));
        int remaining = Math.max(0, maxUses - uses);
        meta.lore(List.of(
                Component.text("Mounted combat lance").color(NamedTextColor.GRAY),
                Component.text("Durability: " + remaining + "/" + maxUses).color(NamedTextColor.GRAY)
        ));
    }
}
