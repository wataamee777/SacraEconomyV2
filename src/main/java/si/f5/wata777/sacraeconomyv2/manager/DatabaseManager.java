package si.f5.wata777.sacraeconomyv2.manager;

import org.bukkit.plugin.java.JavaPlugin;
import lombok.Getter;

import java.sql.*;

@Getter
public class DatabaseManager {
    
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private Connection connection;
    private final String tablePrefix;

    public DatabaseManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.tablePrefix = plugin.getConfig().getString("database.prefix", "seco_");
    }

    public void initialize() {
        try {
            initializeMySQL();
            createTables();
            plugin.getLogger().info("Database connection established successfully!");
            plugin.getLogger().info("Table prefix: " + tablePrefix);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeMySQL() throws SQLException {
        String host = plugin.getConfig().getString("database.mysql.host", "localhost");
        int port = plugin.getConfig().getInt("database.mysql.port", 3306);
        String database = plugin.getConfig().getString("database.mysql.database", "sacra_economy");
        String username = plugin.getConfig().getString("database.mysql.username", "root");
        String password = plugin.getConfig().getString("database.mysql.password", "");
        boolean ssl = plugin.getConfig().getBoolean("database.mysql.ssl", false);
        
        // Java（JDBC）ドライバに渡すエンコーディング名は「UTF-8」で大正解！
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + 
                     "?useSSL=" + ssl + "&serverTimezone=UTC&characterEncoding=UTF-8";
        connection = DriverManager.getConnection(url, username, password);
    }

    public String getTableName(String tableName) {
        return tablePrefix + tableName;
    }

    private void createTables() throws SQLException {
        // MySQLの文字セット指定は「utf8mb4」、Collationは「utf8mb4_unicode_ci」に戻します
        String[] tables = {
            // Player account table
            "CREATE TABLE IF NOT EXISTS " + getTableName("player_accounts") + " (" +
            "uuid VARCHAR(36) PRIMARY KEY," +
            "username VARCHAR(255) NOT NULL," +
            "balance DOUBLE NOT NULL DEFAULT 0," +
            "level INT NOT NULL DEFAULT 1," +
            "job VARCHAR(50)," +
            "language VARCHAR(10) DEFAULT 'ja'," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "KEY idx_username (username)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            
            // Transaction history
            "CREATE TABLE IF NOT EXISTS " + getTableName("transactions") + " (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "from_uuid VARCHAR(36)," +
            "to_uuid VARCHAR(36)," +
            "amount DOUBLE NOT NULL," +
            "type VARCHAR(50) NOT NULL," +
            "description TEXT," +
            "status VARCHAR(20) DEFAULT 'COMPLETED'," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "KEY idx_from_uuid (from_uuid)," +
            "KEY idx_to_uuid (to_uuid)," +
            "KEY idx_created_at (created_at)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            
            // Achievements
            "CREATE TABLE IF NOT EXISTS " + getTableName("achievements") + " (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "uuid VARCHAR(36) NOT NULL," +
            "achievement_id VARCHAR(100) NOT NULL," +
            "level INT NOT NULL DEFAULT 1," +
            "earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "UNIQUE KEY unique_achievement (uuid, achievement_id)," +
            "KEY idx_uuid (uuid)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            
            // Giveaway codes
            "CREATE TABLE IF NOT EXISTS " + getTableName("giveaway_codes") + " (" +
            "code VARCHAR(100) PRIMARY KEY," +
            "amount DOUBLE NOT NULL," +
            "max_uses INT DEFAULT -1," +
            "current_uses INT DEFAULT 0," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            
            // Player shops
            "CREATE TABLE IF NOT EXISTS " + getTableName("player_shops") + " (" +
            "shop_id INT AUTO_INCREMENT PRIMARY KEY," +
            "owner_uuid VARCHAR(36) NOT NULL," +
            "shop_name VARCHAR(255) NOT NULL," +
            "location VARCHAR(255)," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "KEY idx_owner_uuid (owner_uuid)," +
            "UNIQUE KEY unique_shop (owner_uuid, shop_name)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            
            // Shop items for sale
            "CREATE TABLE IF NOT EXISTS " + getTableName("shop_items") + " (" +
            "item_id INT AUTO_INCREMENT PRIMARY KEY," +
            "shop_id INT NOT NULL," +
            "item_name VARCHAR(255) NOT NULL," +
            "price DOUBLE NOT NULL," +
            "quantity INT NOT NULL," +
            "for_sale BOOLEAN DEFAULT 1," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY(shop_id) REFERENCES " + getTableName("player_shops") + "(shop_id) ON DELETE CASCADE," +
            "KEY idx_shop_id (shop_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            
            // Work cooldown tracking
            "CREATE TABLE IF NOT EXISTS " + getTableName("work_cooldown") + " (" +
            "uuid VARCHAR(36) PRIMARY KEY," +
            "job VARCHAR(50) NOT NULL," +
            "last_work_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : tables) {
                stmt.execute(sql);
            }
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Database connection closed!");
            } catch (SQLException e) {
                plugin.getLogger().severe("Error closing database: " + e.getMessage());
            }
        }
    }

    public void executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        }
    }

    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        return pstmt.executeQuery();
    }
}
