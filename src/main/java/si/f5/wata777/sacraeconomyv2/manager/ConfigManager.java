package si.f5.wata777.sacraeconomyv2.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

@Getter
public class ConfigManager {
    
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        // Create config directory if it doesn't exist
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        configFile = new File(dataFolder, "config.yml");

        // Create default config if it doesn't exist
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reloadConfig() {
        loadConfig();
        plugin.getLogger().info("Configuration has been reloaded!");
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Convenience getters
    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }

    public String getSQLitePath() {
        return config.getString("database.sqlite.file", "plugins/SacraEconomyV2/economy.db");
    }

    public double getStartingBalance() {
        return config.getDouble("economy.starting-balance", 1000.0);
    }

    public String getCurrencySymbol() {
        return config.getString("economy.currency-symbol", "¥");
    }

    public double getJobReward(String jobId) {
        return config.getDouble("jobs.rewards." + jobId, 100.0);
    }

    public int getJobCooldown() {
        return config.getInt("jobs.cooldown-seconds", 300);
    }

    public boolean isFeatureEnabled(String feature) {
        return config.getBoolean("features." + feature, true);
    }

    public FileConfiguration getConfig() {
        return this.config;
    }
}