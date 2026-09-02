package si.f5.wata777.sacraeconomyv2.manager;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TransactionManager {
    
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final EconomyManager economyManager;

    public TransactionManager(JavaPlugin plugin, DatabaseManager databaseManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.economyManager = economyManager;
    }

    /**
     * プレイヤー間の送金処理（トランザクション付き）
     */
    public boolean transfer(UUID fromUuid, UUID toUuid, double amount, String type, String description) {
        if (amount <= 0) {
            return false;
        }

        try {
            // Check if sender has enough money
            double senderBalance = economyManager.getBalance(fromUuid);
            if (senderBalance < amount) {
                return false;
            }

            // Perform the transaction
            economyManager.withdraw(fromUuid, amount);
            economyManager.deposit(toUuid, amount);

            // Log the transaction
            logTransaction(fromUuid, toUuid, amount, type, description, "COMPLETED");

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Transaction failed: " + e.getMessage());
            // Rollback if needed - already handled by individual deposit/withdraw
            try {
                logTransaction(fromUuid, toUuid, amount, type, description, "FAILED");
            } catch (SQLException ex) {
                plugin.getLogger().severe("Failed to log failed transaction: " + ex.getMessage());
            }
            return false;
        }
    }

    /**
     * 単一プレイヤーへの入金（トランザクション付き）
     */
    public boolean deposit(UUID toUuid, double amount, String type, String description) {
        if (amount <= 0) {
            return false;
        }

        try {
            economyManager.deposit(toUuid, amount);
            logTransaction(null, toUuid, amount, type, description, "COMPLETED");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Deposit failed: " + e.getMessage());
            try {
                logTransaction(null, toUuid, amount, type, description, "FAILED");
            } catch (SQLException ex) {
                plugin.getLogger().severe("Failed to log failed deposit: " + ex.getMessage());
            }
            return false;
        }
    }

    /**
     * 単一プレイヤーからの出金（トランザクション付き）
     */
    public boolean withdraw(UUID fromUuid, double amount, String type, String description) {
        if (amount <= 0) {
            return false;
        }

        try {
            double balance = economyManager.getBalance(fromUuid);
            if (balance < amount) {
                return false;
            }

            economyManager.withdraw(fromUuid, amount);
            logTransaction(fromUuid, null, amount, type, description, "COMPLETED");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Withdrawal failed: " + e.getMessage());
            try {
                logTransaction(fromUuid, null, amount, type, description, "FAILED");
            } catch (SQLException ex) {
                plugin.getLogger().severe("Failed to log failed withdrawal: " + ex.getMessage());
            }
            return false;
        }
    }

    /**
     * トランザクションログ記録
     */
    private void logTransaction(UUID fromUuid, UUID toUuid, double amount, String type, String description, String status) throws SQLException {
        databaseManager.executeUpdate(
            "INSERT INTO " + databaseManager.getTableName("transactions") +
            " (from_uuid, to_uuid, amount, type, description, status) VALUES (?, ?, ?, ?, ?, ?)",
            fromUuid != null ? fromUuid.toString() : null,
            toUuid != null ? toUuid.toString() : null,
            amount,
            type,
            description,
            status
        );
    }

    /**
     * トランザクション履歴取得
     */
    public double getTotalTransactionAmount(UUID playerUuid, String type) {
        try {
            ResultSet rs = databaseManager.executeQuery(
                "SELECT SUM(amount) as total FROM " + databaseManager.getTableName("transactions") +
                " WHERE (from_uuid = ? OR to_uuid = ?) AND type = ? AND status = 'COMPLETED'",
                playerUuid.toString(), playerUuid.toString(), type
            );

            if (rs.next()) {
                Double total = rs.getDouble("total");
                return total != null ? total : 0.0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get transaction amount: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * ショップ取引（トランザクション付き）
     */
    public boolean shopTransaction(UUID buyerUuid, UUID sellerUuid, double price, String itemName) {
        String description = "Bought/Sold: " + itemName;
        return transfer(buyerUuid, sellerUuid, price, "SHOP", description);
    }

    /**
     * 仕事報酬（トランザクション付き）
     */
    public boolean workReward(UUID playerUuid, double amount, String job) {
        String description = "Work reward: " + job;
        return deposit(playerUuid, amount, "WORK", description);
    }

    /**
     * 実績報酬（トランザクション付き）
     */
    public boolean achievementReward(UUID playerUuid, double amount, String achievementId) {
        String description = "Achievement reward: " + achievementId;
        return deposit(playerUuid, amount, "ACHIEVEMENT", description);
    }

    /**
     * ギフトコード報酬（トランザクション付き）
     */
    public boolean giveawayReward(UUID playerUuid, double amount, String code) {
        String description = "Giveaway code: " + code;
        return deposit(playerUuid, amount, "GIVEAWAY", description);
    }
}
