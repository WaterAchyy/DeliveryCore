# DeliveryCore

**Professional Delivery System for Minecraft Servers**

Transform your server with engaging delivery events! Players collect and deliver items to earn rewards, compete on leaderboards, and participate in scheduled or manual events.

---

## Supported Loaders & Versions

### Loaders
- **Spigot**
- **Paper**
- **Purpur**
- **Bukkit**

### Minecraft Versions
- 1.16.5
- 1.17.1
- 1.18.2
- 1.19.4
- 1.20.1
- 1.20.2
- 1.20.4

### Requirements
- **Java:** 17+
- **Optional:** Vault (economy), PlaceholderAPI (placeholders)

---

## Features

### 📦 Delivery Events
- **Scheduled Events** - Set up automatic events with natural language scheduling ("every day at 18:00", "every monday at 20:00")
- **Manual Events** - Start events instantly with commands
- **Multiple Concurrent Events** - Run up to 5 events simultaneously
- **Random Item Selection** - Items randomly selected from configurable categories
- **Winner System** - Configurable number of winners per event

### Beautiful GUI
- **Custom Head Textures** - Unique icons for all menu elements
- **SmallCaps Typography** - Modern, stylish text formatting
- **Real-time Updates** - Live leaderboard and delivery counts
- **Intuitive Navigation** - Easy-to-use menu system

### Reward System
- **Inventory Rewards** - Give items directly to players
- **Command Rewards** - Execute any command as reward
- **Economy Integration** - Sell items for money (Vault support)
- **Pending Rewards** - Offline players receive rewards on next login

### Discord Webhook Integration
- **Event Start Notifications** - Announce when events begin
- **Event End Notifications** - Show winners with leaderboard
- **Customizable Embeds** - Full control over colors, titles, descriptions
- **Winner Medals** - 🥇🥈🥉 ranking display

### Multi-Language Support
- **Turkish (tr)** - Full Turkish translation
- **English (en)** - Full English translation
- **Easy to Add More** - Simple YAML-based language files

### Fully Configurable
- **500+ Items** - All Minecraft items with Turkish names and prices
- **17 Categories** - Farm, Ore, Block, Food, Wood, Tool, Armor, and more
- **Custom Deliveries** - Create unlimited delivery event types
- **Display Names** - Customize all category and delivery names

### Advanced Features
- **PlaceholderAPI Support** - 12+ placeholders for other plugins
- **Chest Delivery** - Deliver items directly from chests
- **Inventory Delivery** - One-click deliver all matching items
- **Data Persistence** - Events survive server restarts
- **Debug Mode** - Detailed logging for troubleshooting

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/dc` or `/teslimat` | Open delivery GUI | `deliverycore.use` |
| `/dc reload` | Reload all configurations | `deliverycore.admin` |
| `/dc start <name>` | Start an event manually | `deliverycore.admin` |
| `/dc stop <name>` | Stop an active event | `deliverycore.admin` |
| `/dc status` | View active events | `deliverycore.admin` |
| `/dc webhooktest` | Test Discord webhook | `deliverycore.admin` |

---

## Permissions

`deliverycore.use` | Access delivery GUI | true |
`deliverycore.admin` | Admin commands | op |
`deliverycore.bypass.protection` | Bypass chest protection | op |

---

## Configuration Files

- `config.yml` - Main settings, webhook config, display names
- `categories.yml` - Item categories with weights
- `deliveries.yml` - Delivery event definitions
- `items.yml` - 500+ items with Turkish names and prices
- `lang/tr.yml` - Turkish messages
- `lang/en.yml` - English messages

---

## PlaceholderAPI Placeholders

```
%deliverycore_active_count% - Number of active events
%deliverycore_active_names% - Names of active events
%deliverycore_player_total% - Player's total deliveries
%deliverycore_player_rank% - Player's current rank
%deliverycore_event_item% - Current event's required item
%deliverycore_event_category% - Current event's category
%deliverycore_event_remaining% - Time remaining
%deliverycore_leaderboard% - Top players leaderboard
%deliverycore_top_1_name% - #1 player name
%deliverycore_top_1_count% - #1 player delivery count
```

## Installation

1. Download the plugin JAR
2. Place in your `plugins` folder
3. Restart the server
4. Edit configurations in `plugins/DeliveryCore/`
5. Use `/dc reload` to apply changes

## Requirements

- **Minecraft:** 1.16.5 - 1.20.4
- **Java:** 17+
- **Optional:** Vault (for economy), PlaceholderAPI (for placeholders)

![Gui](https://cdn.modrinth.com/data/cached_images/5855a3d7348eb09f89a45cca3d0c44371a023c7d.png)

## License

This project is licensed under MIT License.

---

**Made with ❤️ for Minecraft servers**


<details>
<summary>Spoiler</summary>

developed maolide

</details>


