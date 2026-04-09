# NikasMiner

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.19+-green.svg)
![Java Version](https://img.shields.io/badge/Java-17-orange.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

A Spigot plugin that lets players mine entire veins of ores, logs, and crops by sneaking while breaking a block.

## Features

- Break entire ore veins, tree logs, or crop patches in one action
- Recursive vein detection with configurable max blocks (default: 50)
- Smart durability consumption (1 durability per block)
- Drops spawn at the origin block location
- Particle and sound effects
- Toggle on/off with `/vm toggle`
- Per-player cooldown system
- Permission-based access control
- Hot-reload configuration with `/vm reload`
- Persistent player preferences using PDC

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/vm toggle` | Toggle VeinMiner on/off | `veinminer.toggle` |
| `/vm reload` | Reload plugin configuration | `veinminer.reload` |

**Aliases:** `/veinminer`, `/vm`

## Permissions

- `veinminer.toggle` - Allow toggling VeinMiner (default: true)
- `veinminer.reload` - Allow reloading configuration (default: op)
- `veinminer.use.ores` - Allow vein mining ores (default: true)
- `veinminer.use.logs` - Allow vein mining logs (default: true)
- `veinminer.use.crops` - Allow vein mining crops (default: true)
- `veinminer.*` - Grants all permissions

## Configuration

```yaml
settings:
  max-blocks: 50
  cooldown: 2
  particles: true
  sounds: true

enabled-blocks:
  ores:
    - COAL_ORE
    - DEEPSLATE_COAL_ORE
    # ... other ores
  logs:
    - OAK_LOG
    - SPRUCE_LOG
    # ... other logs
  crops:
    - WHEAT
    - CARROTS
    - POTATOES

messages:
  enabled: "&aVeinMiner enabled!"
  disabled: "&cVeinMiner disabled!"
  cooldown: "&eYou must wait {time} seconds before using VeinMiner again."
  no-permission: "&cYou don't have permission to use VeinMiner on this block type."
  reload: "&aVeinMiner configuration reloaded!"
```

## Installation

1. Download the latest `VeinMiner.jar` from [Releases](../../releases)
2. Place the JAR in your server's `plugins/` folder
3. Restart or reload your server
4. Edit `plugins/VeinMiner/config.yml` as needed
5. Run `/vm reload` to apply changes without restarting

## Building from Source

Requires Java 17+ and Maven 3.6+.

```bash
git clone https://github.com/hmax07697-gif/veinminer.git
cd veinminer
mvn clean package
# Output: target/VeinMiner-1.0.0.jar
```

## Usage

1. Equip a pickaxe, axe, or hoe
2. Hold **Shift** (sneak)
3. Break an ore, log, or crop block — the entire vein breaks automatically

Use `/vm toggle` to temporarily disable VeinMiner, and `/vm reload` after editing `config.yml`.

## License

MIT — see [LICENSE](LICENSE) for details.
