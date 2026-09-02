package si.f5.wata777.sacraeconomyv2.manager;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AchievementManager {
    
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;

    public AchievementManager(JavaPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public void unlockAchievement(UUID uuid, String achievementId) {
        try {
            // Check if already unlocked
            ResultSet rs = databaseManager.executeQuery(
                "SELECT level FROM achievements WHERE uuid = ? AND achievement_id = ?",
                uuid.toString(), achievementId
            );
            
            if (rs.next()) {
                // Already unlocked, increment level
                int currentLevel = rs.getInt("level");
                incrementLevel(uuid, achievementId, currentLevel + 1);
            } else {
                // New achievement
                databaseManager.executeUpdate(
                    "INSERT INTO achievements (uuid, achievement_id, level) VALUES (?, ?, ?)",
                    uuid.toString(), achievementId, 1
                );
                
                // Grant reward for level 1
                grantReward(uuid, 1);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to unlock achievement: " + e.getMessage());
        }
    }

    public void incrementLevel(UUID uuid, String achievementId, int newLevel) {
        try {
            databaseManager.executeUpdate(
                "UPDATE achievements SET level = ? WHERE uuid = ? AND achievement_id = ?",
                newLevel, uuid.toString(), achievementId
            );
            
            // Grant reward for new level if configured
            grantReward(uuid, newLevel);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to increment achievement level: " + e.getMessage());
        }
    }

    private void grantReward(UUID uuid, int level) {
        try {
            double reward = plugin.getConfig().getDouble("achievements.rewards-per-level." + level, 0);
            if (reward > 0) {
                EconomyManager economyManager = plugin.getServer().getServicesManager()
                    .load(EconomyManager.class);
                if (economyManager != null) {
                    economyManager.deposit(uuid, reward);
                    plugin.getLogger().info("Granted achievement reward to " + uuid + ": " + reward);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to grant achievement reward: " + e.getMessage());
        }
    }

    public int getAchievementLevel(UUID uuid, String achievementId) {
        try {
            ResultSet rs = databaseManager.executeQuery(
                "SELECT level FROM achievements WHERE uuid = ? AND achievement_id = ?",
                uuid.toString(), achievementId
            );
            if (rs.next()) {
                return rs.getInt("level");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get achievement level: " + e.getMessage());
        }
        return 0;
    }

    public boolean hasAchievement(UUID uuid, String achievementId) {
        return getAchievementLevel(uuid, achievementId) > 0;
    }
}
