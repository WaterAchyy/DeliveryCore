# DeliveryCore

[![Build Status](https://github.com/yourusername/DeliveryCore/workflows/Build%20and%20Test/badge.svg)](https://github.com/yourusername/DeliveryCore/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.16.5--1.20.4-green.svg)](https://www.minecraft.net/)
[![Spigot](https://img.shields.io/badge/Spigot-Paper%20%7C%20Purpur%20%7C%20Bukkit-orange.svg)](https://www.spigotmc.org/)

A professional delivery event system for Minecraft servers. Schedule automated delivery events where players compete to deliver the most items!

## Features

- **Scheduled Events** - Natural language scheduling ("every day at 18:00")
- **17 Categories** - Farm, Ore, Block, Food, Wood, Rare, Nether, End, Mob, and more
- **Beautiful GUI** - Custom head textures and smooth interface
- **Leaderboard** - Real-time ranking system
- **Discord Webhooks** - Event notifications with winner announcements
- **Multi-language** - Turkish & English support
- **Rewards** - Items + commands with offline player support
- **Protection Compatible** - Works with WorldGuard, GriefPrevention, etc.

## Commands

| Command | Description |
|---------|-------------|
| `/teslimat` / `/delivery` | Open delivery GUI |
| `/teslim` / `/deliver` | Quick deliver from inventory |
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
git clone https://github.com/yourusername/DeliveryCore.git
cd DeliveryCore
mvn clean package
```

JAR will be at `target/deliverycore-1.0.0-shaded.jar`

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Maolide** - *Lead Developer*
- **3Mustafa5** - *Developer*

## 🔗 Links

- [SpigotMC](https://www.spigotmc.org/resources/deliverycore)
- [Modrinth](https://modrinth.com/plugin/deliverycore)
- [Discord](https://discord.gg/yourserver)

## 📊 Supported Versions

- **Minecraft:** 1.16.5 - 1.20.4
- **Servers:** Spigot, Paper, Purpur, Bukkit
- **Java:** 8+ (recommended: 17+)
