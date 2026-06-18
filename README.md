# JoustingPlugin

A Minecraft Paper 1.21.11 plugin that adds a complete jousting system with lances, momentum, and knockoff mechanics.

## Features

✨ **Lance System** - Players on horses holding a lance (End Rod by default) deal jousting damage

⚡ **Momentum Tracking** - Distance traveled builds momentum, more distance = more damage

🐴 **Speed Tiers** - Horses are classified by speed (BPS threshold configurable)
  - Low tier: Up to 3 hearts damage
  - High tier: Up to 6 hearts damage

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
# Lance item type
lance-item: END_ROD

# Speed threshold for high-tier horses (blocks per second)
high-tier-speed-threshold: 1.5

# Maximum damage values
low-tier-max-damage: 3.0
high-tier-max-damage: 6.0

# Minimum distance traveled before damage applies
minimum-momentum-distance: 5.0

# Knockoff probabilities
knockoff-chance-zero-momentum: 5
knockoff-chance-full-momentum: 70

# Sound effects
sounds:
  hit: "entity.player.attack.strong"
  knockoff: "entity.item.break"
```

## Commands

- `/jousting` - Shows available commands
- `/jousting reload` - Reloads the plugin configuration

## Permissions

- `jousting.admin` - Permission to use admin commands (default: op)

## How It Works

1. **Mount a horse** and hold an End Rod (or configured lance item)
2. **Ride the horse** to build momentum (more distance = more momentum)
3. **Hit another player** on a horse with your lance
4. **Damage scales** with momentum and horse speed tier
5. **Chance to knockoff** the target, scaling from 5% to 70%
6. **Momentum resets** after each hit

## Requirements

- Paper 1.21.8 or later
- Java 21 or later

## Author

k4nefr-cmd

## Forked by 

GorrGodSlayer (AKA Alex)

## Horse Combat System (added 2026-06-18)

Implemented by GorrGodSlayer

- Three lance tiers as distinct items — Iron (`END_ROD`), Gold (`BLAZE_ROD`), Diamond (`BREEZE_ROD`)
- Lance durability + breaking (lance becomes decorative when spent)
- Post-hit / post-block lance cooldown
- Shield interaction (shield takes durability, knockback, cooldown; no health damage on a block)
- Three horse speed tiers (slow / medium / fast → 3 / 5 / 6 caps)
- Momentum reset on stop, hit, or dismount
- Momentum visual **BossBar**
- Strength potion + Sharpness enchant handling, clamped so no one-hit kills
- PvP mode (foot target) vs Jousting mode (mounted target, dismount chance)
- `/jousting give <iron|gold|diamond>`

Build & test locally with `mvn clean verify`. 
test report, and review.

## Known minor bugs

- The momentum bar sometimes hallucinates 
- Need to test the lances against shields and against players 
- Need to see if knockback/unmounting works
- Need to see if i coded the lances properly
