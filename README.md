# JoustingPlugin

<<<<<<< HEAD
A Minecraft Paper 1.21.11+ plugin that adds a complete jousting system with lances, momentum, and knockoff mechanics.
=======
A Minecraft Paper 1.21.11 plugin that adds a complete jousting system with lances, momentum, and knockoff mechanics.
>>>>>>> d564ae0cd4924d13e444e302fcd440fe55ee6ae2

## Features

✨ **Lance System** - Players on horses holding a lance (the craftable spears) deal jousting damage

⚡ **Momentum Tracking** - Distance traveled builds momentum, more distance = more damage

🐴 **Speed Tiers** - Horses are classified by their movement-speed attribute (thresholds configurable)
  - Slow tier: up to 3 hearts damage
  - Medium tier: up to 5 hearts damage
  - Fast tier: up to 6 hearts damage

💥 **Knockoff Mechanics** - Chance to knock riders off horses
  - 5% at zero momentum
  - 70% at full momentum
  - Scales smoothly between

🔊 **Knockback** - Hit players are knocked back in the lance direction

🎵 **Configurable Sounds** - Customize hit and knockoff sounds

⚙️ **Admin Commands** - `/jousting reload` to reload configuration

## Installation

1. Build the plugin:
   ```bash
   mvn clean package
   ```

2. Copy `target/JoustingPlugin-1.0.0.jar` to your server's `plugins/` folder

3. Restart your server

4. Edit `plugins/JoustingPlugin/config.yml` to customize settings (optional)

5. Run `/jousting reload` to apply any config changes

## Configuration

Edit `config.yml` to customize:

```yaml
# Lance tiers (any material listed here counts as a lance)
lance-tiers:
  IRON_SPEAR:
    damage-bonus: 0.0
    max-uses: 10
  GOLDEN_SPEAR:
    damage-bonus: 1.0
    max-uses: 14
  DIAMOND_SPEAR:
    damage-bonus: 2.0
    max-uses: 18

# Horse movement-speed attribute thresholds
medium-tier-speed-threshold: 0.2
high-tier-speed-threshold: 0.28

# Maximum damage values (hearts)
low-tier-max-damage: 3.0
medium-tier-max-damage: 5.0
high-tier-max-damage: 6.0

# Minimum distance traveled before damage applies
minimum-momentum-distance: 5.0

# Knockoff probabilities
knockoff-chance-zero-momentum: 5
knockoff-chance-full-momentum: 70

# Sound effects (Sound enum names)
sounds:
  hit: "ENTITY_PLAYER_ATTACK_STRONG"
  knockoff: "ENTITY_ITEM_BREAK"
```

## Commands

- `/jousting` - Shows available commands
- `/jousting reload` - Reloads the plugin configuration
- `/jousting give <iron|gold|diamond>` - Gives you a lance of that tier

## Permissions

- `jousting.admin` - Permission to use admin commands (default: op)
- `jousting.use` - Permission to wield lances in combat (default: true)

## How It Works

1. **Mount a horse** and hold a spear (any configured lance material)
2. **Ride the horse** to build momentum (more distance = more momentum)
3. **Hit another player** on a horse with your lance
4. **Damage scales** with momentum and horse speed tier
5. **Chance to knockoff** the target, scaling from 5% to 70%
6. **Momentum resets** after each hit

## Requirements

- Paper 1.21.11 or later (needs the craftable spear items)
- Java 21 or later

## Author

k4nefr-cmd

## Forked by 

GorrGodSlayer (AKA Alex)

## Horse Combat System (added 2026-06-18)

Implemented by GorrGodSlayer

- Three lance tiers as distinct items — Iron (`IRON_SPEAR`), Gold (`GOLDEN_SPEAR`), Diamond (`DIAMOND_SPEAR`)
- Lance durability + breaking (lance becomes decorative when spent)
- Post-hit / post-block lance cooldown
- Shield interaction (shield takes durability, knockback, cooldown; no health damage on a block)
- Three horse speed tiers (slow / medium / fast → 3 / 5 / 6 caps)
- Momentum reset on stop, hit, or dismount
- Momentum visual **BossBar**
- Lance damage ignores Strength/Sharpness and is hard-capped so no one-hit kills
- PvP mode (foot target) vs Jousting mode (mounted target, dismount chance)
- `/jousting give <iron|gold|diamond>`

Build & test locally with `mvn clean verify`.

## Known minor bugs

- The momentum bar sometimes hallucinated — fixed (momentum now tracks the horse's position, not the rider's)
- Need to test the lances against shields and against players 
- Need to see if knockback/unmounting works
- Need to see if i coded the lances properly
