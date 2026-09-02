package si.f5.wata777.sacraeconomyv2;

import org.bukkit.plugin.java.JavaPlugin;
import si.f5.wata777.sacraeconomyv2.manager.ConfigManager;
import si.f5.wata777.sacraeconomyv2.manager.DatabaseManager;
import si.f5.wata777.sacraeconomyv2.manager.EconomyManager;
import si.f5.wata777.sacraeconomyv2.manager.AchievementManager;
import si.f5.wata777.sacraeconomyv2.manager.TradeRequestManager;
import si.f5.wata777.sacraeconomyv2.manager.ShopManager;
import si.f5.wata777.sacraeconomyv2.command.CommandHandler;
import si.f5.wata777.sacraeconomyv2.listener.PlayerEventListener;

public class SacraEconomyV2 extends JavaPlugin {
    
    private static SacraEconomyV2 instance;
    
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private EconomyManager economyManager;
    private AchievementManager achievementManager;
    private TradeRequestManager tradeRequestManager;
    private ShopManager shopManager;

    @Override
    public void onLoad() {
        instance = this;
        getLogger().info("SacraEconomyV2 is loading...");
        
        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Save the config
        saveConfig();
    }

    @Override
    public void onEnable() {
        getLogger().info("SacraEconomyV2 is enabling...");
        
        // Initialize database
        databaseManager = new DatabaseManager(this, configManager);
        databaseManager.initialize();
        
        // Initialize managers
        economyManager = new EconomyManager(this, databaseManager);
        achievementManager = new AchievementManager(this, databaseManager);
        tradeRequestManager = new TradeRequestManager(this);
        shopManager = new ShopManager(this, databaseManager);
        
        // Register commands
        new CommandHandler(this);
        
        // Register events
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
        
        getLogger().info("SacraEconomyV2 v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("SacraEconomyV2 has been disabled!");
    }

    // Getters
    public static SacraEconomyV2 getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    public TradeRequestManager getTradeRequestManager() {
        return tradeRequestManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }
}
