package si.f5.wata777.sacraeconomyv2.manager;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {
    
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, Long> workCooldowns = new HashMap<>();

    public EconomyManager(JavaPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    // Account management
    public void createAccount(UUID uuid, String username) {
        try {
            double startingBalance = plugin.getConfig().getDouble("economy.starting-balance", 1000.0);
            String defaultLang = plugin.getConfig().getString("language.default", "ja");
            databaseManager.executeUpdate(
                "INSERT IGNORE INTO " + databaseManager.getTableName("player_accounts") + 
                " (uuid, username, balance, language) VALUES (?, ?, ?, ?)",
                uuid.toString(), username, startingBalance, defaultLang
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create account: " + e.getMessage());
        }
    }

    public String getUsername(UUID uuid) {
        try {
            ResultSet rs = databaseManager.executeQuery(
                "SELECT username FROM " + databaseManager.getTableName("player_accounts") + " WHERE uuid = ?",
                uuid.toString()
            );
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get username: " + e.getMessage());
        }
        return "Unknown";
    }

    public double getBalance(UUID uuid) {
        try {
            ResultSet rs = databaseManager.executeQuery(
                "SELECT balance FROM " + databaseManager.getTableName("player_accounts") + " WHERE uuid = ?",
                uuid.toString()
            );
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get balance: " + e.getMessage());
        }
        return 0;
    }

    public void setBalance(UUID uuid, double amount) {
        try {
            double maxBalance = plugin.getConfig().getDouble("economy.max-balance", 9999999999.0);
            amount = Math.min(amount, maxBalance);
            amount = Math.max(amount, 0);
            
            databaseManager.executeUpdate(
                "UPDATE " + databaseManager.getTableName("player_accounts") + " SET balance = ? WHERE uuid = ?",
                amount, uuid.toString()
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set balance: " + e.getMessage());
        }
    }

    public boolean deposit(UUID uuid, double amount) {
        if (amount <= 0) return false;
        try {
            double currentBalance = getBalance(uuid);
            double maxBalance = plugin.getConfig().getDouble("economy.max-balance", 9999999999.0);
            if (currentBalance + amount > maxBalance) {
                return false;
            }
            databaseManager.executeUpdate(
                "UPDATE " + databaseManager.getTableName("player_accounts") + " SET balance = balance + ? WHERE uuid = ?",
                amount, uuid.toString()
            );
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to deposit: " + e.getMessage());
            return false;
        }
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0) return false;
        try {
            double currentBalance = getBalance(uuid);
            if (currentBalance < amount) {
                return false;
            }
            databaseManager.executeUpdate(
                "UPDATE " + databaseManager.getTableName("player_accounts") + " SET balance = balance - ? WHERE uuid = ?",
                amount, uuid.toString()
            );
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to withdraw: " + e.getMessage());
            return false;
        }
    }

    // Job management
    public String getJob(UUID uuid) {
        try {
            ResultSet rs = databaseManager.executeQuery(
                "SELECT job FROM " + databaseManager.getTableName("player_accounts") + " WHERE uuid = ?",
                uuid.toString()
            );
            if (rs.next()) {
                return rs.getString("job");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get job: " + e.getMessage());
        }
        return null;
    }

    public void setJob(UUID uuid, String job) {
        try {
            databaseManager.executeUpdate(
                "UPDATE " + databaseManager.getTableName("player_accounts") + " SET job = ? WHERE uuid = ?",
                job, uuid.toString()
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set job: " + e.getMessage());
        }
    }

    public boolean canWork(UUID uuid) {
        int cooldown = plugin.getConfig().getInt("jobs.cooldown-seconds", 300);
        Long lastWork = workCooldowns.get(uuid);
        
        if (lastWork == null) {
            return true;
        }

        long elapsed = (System.currentTimeMillis() - lastWork) / 1000;
        return elapsed >= cooldown;
    }

    public void work(UUID uuid, String job) {
        double reward = plugin.getConfig().getDouble("jobs.rewards." + job, 100.0);
        deposit(uuid, reward);
        workCooldowns.put(uuid, System.currentTimeMillis());
    }

    public int getLevel(UUID uuid) {
        try {
            ResultSet rs = databaseManager.executeQuery(
                "SELECT level FROM " + databaseManager.getTableName("player_accounts") + " WHERE uuid = ?",
                uuid.toString()
            );
            if (rs.next()) {
                return rs.getInt("level");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get level: " + e.getMessage());
        }
        return 1;
    }

    public void setLevel(UUID uuid, int level) {
        try {
            databaseManager.executeUpdate(
                "UPDATE " + databaseManager.getTableName("player_accounts") + " SET level = ? WHERE uuid = ?",
                level, uuid.toString()
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set level: " + e.getMessage());
        }
    }

    public boolean useGiveawayCode(UUID uuid, String code) {
        try {
            ResultSet rs = databaseManager.executeQuery(
                "SELECT amount, max_uses, current_uses FROM " + databaseManager.getTableName("giveaway_codes") + " WHERE code = ?",
                code
            );

            if (!rs.next()) {
                return false;
            }

            double amount = rs.getDouble("amount");
            int maxUses = rs.getInt("max_uses");
            int currentUses = rs.getInt("current_uses");

            // Check if code is expired
            if (maxUses != -1 && currentUses >= maxUses) {
                return false;
            }

            // Give reward
            deposit(uuid, amount);

            // Update usage
            databaseManager.executeUpdate(
                "UPDATE " + databaseManager.getTableName("giveaway_codes") + " SET current_uses = current_uses + 1 WHERE code = ?",
                code
            );

            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to use giveaway code: " + e.getMessage());
            return false;
        }
    }
}
