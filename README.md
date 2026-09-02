# SacraEconomyV2 - Advanced Economy Plugin

An advanced Minecraft economy plugin for Paper Servers (1.21+) with comprehensive trading, job system, achievements, and player shops.

## 🎉 Version 1.1.0 - Trade & Shop Enhancement Update

**新機能:**
- ✨ プレイヤー間トレードシステム完全実装
- 🏪 プレイヤーショップシステム完全実装  
- 📦 トレードリクエスト＆セッション管理
- 🏷️ ショップサイン統合
- ⏱️ タイムアウト管理機能

### 📊 Economy System
- **Player Accounts**: Automatic account creation on join
- **Balance Management**: Deposit, withdraw, and transfer money
- **Currency Symbol**: Customizable currency (default: ¥)
- **Balance Limits**: Configurable max balance

### 💼 Job System
- Multiple job types (Mining, Farming, Fishing, Hunting, Crafting)
- Job-based income
- Configurable cooldown between jobs
- Job selection and status tracking

### 🎁 Achievements & Rewards
- Achievement unlock system
- Progressive level-based rewards
- Reward tiers based on achievement level

### 🏪 Trading Systems

#### 1. Official Shop
- `/sc offbuy` - Buy from official shop
- `/sc offsell` - Sell to official shop
- Shop price multipliers (buy/sell rates)

#### 2. Player-to-Player Trading
- `/sc buy` - Buy from players
- `/sc sell` - Sell to players
- Direct player trading

#### 3. Chest-Based Trading
- Chest GUI interface
- Sign-based trading with formats: `[sc:buy]` and `[sc:sell]`
- Timeout-based automatic cancellation
- Item exchange between players

#### 4. Player Shops
- Create and manage personal shops
- Sign-based item listings
- Custom pricing

### 🎟️ Giveaway Codes
- Redeem giveaway codes with `/sc redeem <code>`
- Configurable rewards per code
- Usage limits

### ⚙️ Configuration
- JSON-based settings UI
- Configurable shop items and prices
- SQLite or MySQL database support

## Commands

| Command | Description |
|---------|-------------|
| `/sc` | Show help message |
| `/sc reload` | Reload configuration (admin) |
| `/sc status` | View your account status |
| `/sc work <job>` | Work to earn money |
| `/sc redeem <code>` | Redeem giveaway code |
| `/sc offbuy` | Buy from official shop |
| `/sc offsell` | Sell to official shop |
| `/sc buy` | Buy from player shops |
| `/sc sell` | Sell to player shops |
| `/sc trade <player>` | Send trade request to player |
| `/sc trade accept` | Accept trade request |
| `/sc trade decline` | Decline trade request |
| `/sc trade cancel` | Cancel active trade |
| `/sc shop create <name>` | Create a new shop |
| `/sc shop list` | List your shops |
| `/sc shop delete <id>` | Delete a shop |
| `/sc settings` | Configure shop settings (JSON UI) |

## Configuration Files

### config.yml
Main configuration file with:
- Database settings (SQLite/MySQL)
- Economy parameters
- Job rewards
- Shop multipliers
- Achievement rewards
- Giveaway codes
- Feature toggles

### plugin.yml
Plugin metadata and command definitions

## Database Structure

### Tables
- `player_accounts` - Player account data
- `transactions` - Transaction history
- `achievements` - Player achievements
- `giveaway_codes` - Giveaway code tracking
- `player_shops` - Player shop data
- `shop_items` - Items in player shops
- `work_cooldown` - Job cooldown tracking

## Installation

1. Clone or download the repository
2. Build with Maven:
   ```bash
   mvn clean package
   ```
3. Copy the JAR from `target/` to your server's `plugins/` folder
4. Start/restart your server
5. Configuration file will be generated in `plugins/SacraEconomyV2/`

## Building

### Requirements
- Java 21+
- Maven 3.6+

### Build Command
```bash
mvn clean install
```

The compiled JAR will be in `target/SacraEconomyV2-1.0.0.jar`

## Configuration Examples

### config.yml
```yaml
database:
  type: "sqlite"  # or "mysql"
  sqlite:
    file: "plugins/SacraEconomyV2/economy.db"

economy:
  currency-name: "Yen"
  currency-symbol: "¥"
  starting-balance: 1000.0

jobs:
  enabled: true
  cooldown-seconds: 300
  base-reward: 100.0
  rewards:
    mining: 150.0
    farming: 120.0
    fishing: 140.0
```

## Permissions

- `sacraeconomy.admin` - Admin commands (reload)

## API Reference

### Core Classes

#### EconomyManager
```java
double getBalance(UUID uuid)
void setBalance(UUID uuid, double amount)
boolean deposit(UUID uuid, double amount)
boolean withdraw(UUID uuid, double amount)
boolean transfer(UUID from, UUID to, double amount, String desc)
void work(UUID uuid, String job)
void setJob(UUID uuid, String job)
String getJob(UUID uuid)
```

#### ChestTradeManager
```java
void startTradeSession(Player buyer, Player seller)
void addItemToTrade(UUID playerId, ItemStack item, boolean isSeller)
void confirmTrade(UUID playerId)
void cancelTrade(UUID playerId)
```

#### PlayerShopManager
```java
int createShop(UUID owner, String name, String location)
void addItemToShop(int shopId, String item, double price, int quantity, boolean forSale)
void removeItemFromShop(int itemId)
PlayerShop getShop(int shopId)
```

## Project Structure

```
src/main/
├── java/si/f5/wata777/sacraeconomyv2/
│   ├── SacraEconomyV2.java          # Main plugin class
│   ├── command/
│   │   └── CommandHandler.java       # Command processing
│   ├── manager/
│   │   ├── ConfigManager.java        # Configuration management
│   │   ├── DatabaseManager.java      # Database operations
│   │   ├── EconomyManager.java       # Economy system
│   │   ├── AchievementManager.java   # Achievement system
│   │   ├── ChestTradeManager.java    # Chest trading system
│   │   ├── PlayerShopManager.java    # Player shop (legacy)
│   │   ├── TradeRequestManager.java  # Trade request & session mgmt
│   │   └── ShopManager.java          # Shop system
│   ├── listener/
│   │   └── PlayerEventListener.java  # Event handling (join/quit/interact)
│   └── ui/
│       └── SettingsUI.java           # JSON UI settings
└── resources/
    ├── plugin.yml                     # Plugin metadata
    └── config.yml                     # Default config
```

## Future Enhancements

- [ ] Advanced JSON UI for settings
- [ ] Market-wide price listings
- [ ] Banking system
- [ ] Investment features
- [ ] Dynamic economy balancing
- [ ] Admin shop customization
- [ ] Price history tracking
- [ ] Economy statistics

## License

Developed for Sacra Servers

## Support

For issues and feature requests, please contact the server administrators.

---

**Version**: 1.0.0  
**Target Server**: Paper 1.21+  
**API Level**: 1.21
