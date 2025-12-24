# DeliveryCore

A professional delivery event system for Minecraft servers. Schedule automated delivery events where players compete to deliver the most items!

## Features

- 🕐 **Scheduled Events** - Natural language scheduling ("every day at 18:00")
- 📦 **17 Categories** - Farm, Ore, Block, Food, Wood, Rare, Nether, End, Mob, and more
- 🎨 **Beautiful GUI** - Custom head textures and smooth interface
- 🏆 **Leaderboard** - Real-time ranking system
- 💬 **Discord Webhooks** - Event notifications with winner announcements
- 🌍 **Multi-language** - Turkish & English support
- 🎁 **Rewards** - Items + commands with offline player support
- 🔒 **Protection Compatible** - Works with WorldGuard, GriefPrevention, etc.

## Commands

| Command | Description |
|---------|-------------|
| `/teslimat` | Open delivery GUI |
| `/teslim` | Quick deliver from inventory |
| `/dc reload` | Reload configurations |
| `/dc start <name>` | Start event manually |
| `/dc stop <name>` | Stop active event |
| `/dc status` | View event status |
| `/dc list` | List all deliveries |
| `/dc top` | View leaderboard |

## Permissions

| Permission | Description |
|------------|-------------|
| `deliverycore.use` | Use delivery GUI |
| `deliverycore.admin` | Admin commands |
| `deliverycore.reload` | Reload config |

## Supported Versions

- **Minecraft:** 1.16.5 - 1.20.4
- **Servers:** Spigot, Paper, Purpur, Bukkit

## Installation

1. Download the latest JAR from [Releases](../../releases)
2. Place in your `plugins` folder
3. Restart server
4. Edit configs in `plugins/DeliveryCore/`

## Configuration

- `config.yml` - General settings, webhook, display names
- `categories.yml` - Item categories
- `deliveries.yml` - Delivery event definitions
- `items.yml` - Item display names and prices
- `lang/tr.yml` & `lang/en.yml` - Language files

## Building

```bash
mvn clean package
```

JAR will be at `target/deliverycore-1.0.0-shaded.jar`

## License

MIT License

## Authors

- Maolide
- 3Mustafa5
