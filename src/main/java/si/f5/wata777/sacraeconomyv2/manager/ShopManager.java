package si.f5.wata777.sacraeconomyv2.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ShopManager {
    
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<Integer, PlayerShop> shops = new HashMap<>();
    private final Map<String, Integer> signToShopCache = new HashMap<>();

    public ShopManager(JavaPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        loadShops();
    }

    private void loadShops() {
        try {
            ResultSet rs = databaseManager.executeQuery("SELECT * FROM player_shops");
            while (rs.next()) {
                int shopId = rs.getInt("shop_id");
                UUID ownerUuid = UUID.fromString(rs.getString("owner_uuid"));
                String shopName = rs.getString("shop_name");
                String location = rs.getString("location");
                
                PlayerShop shop = new PlayerShop(shopId, ownerUuid, shopName, location);
                
                // Load shop items
                ResultSet items = databaseManager.executeQuery(
                    "SELECT * FROM shop_items WHERE shop_id = ?",
                    shopId
                );
                while (items.next()) {
                    ShopItem item = new ShopItem(
                        items.getInt("item_id"),
                        items.getString("item_name"),
                        items.getDouble("price"),
                        items.getInt("quantity"),
                        items.getBoolean("for_sale")
                    );
                    shop.addItem(item);
                }
                
                shops.put(shopId, shop);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load shops: " + e.getMessage());
        }
    }

    public int createShop(UUID ownerUuid, String shopName) {
        try {
            databaseManager.executeUpdate(
                "INSERT INTO player_shops (owner_uuid, shop_name, location) VALUES (?, ?, ?)",
                ownerUuid.toString(), shopName, ""
            );
            
            ResultSet rs = databaseManager.executeQuery(
                "SELECT shop_id FROM player_shops WHERE owner_uuid = ? AND shop_name = ? ORDER BY shop_id DESC LIMIT 1",
                ownerUuid.toString(), shopName
            );
            
            if (rs.next()) {
                int shopId = rs.getInt("shop_id");
                PlayerShop shop = new PlayerShop(shopId, ownerUuid, shopName, "");
                shops.put(shopId, shop);
                return shopId;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create shop: " + e.getMessage());
        }
        return -1;
    }

    public void deleteShop(int shopId) {
        try {
            databaseManager.executeUpdate("DELETE FROM shop_items WHERE shop_id = ?", shopId);
            databaseManager.executeUpdate("DELETE FROM player_shops WHERE shop_id = ?", shopId);
            shops.remove(shopId);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete shop: " + e.getMessage());
        }
    }

    public int addItemToShop(int shopId, String itemName, double price, int quantity, boolean forSale) {
        try {
            databaseManager.executeUpdate(
                "INSERT INTO shop_items (shop_id, item_name, price, quantity, for_sale) VALUES (?, ?, ?, ?, ?)",
                shopId, itemName, price, quantity, forSale ? 1 : 0
            );
            
            ResultSet rs = databaseManager.executeQuery(
                "SELECT item_id FROM shop_items WHERE shop_id = ? AND item_name = ? ORDER BY item_id DESC LIMIT 1",
                shopId, itemName
            );
            
            if (rs.next()) {
                return rs.getInt("item_id");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add item to shop: " + e.getMessage());
        }
        return -1;
    }

    public void removeItemFromShop(int itemId) {
        try {
            databaseManager.executeUpdate("DELETE FROM shop_items WHERE item_id = ?", itemId);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove item from shop: " + e.getMessage());
        }
    }

    public void updateItemQuantity(int itemId, int quantity) {
        try {
            databaseManager.executeUpdate(
                "UPDATE shop_items SET quantity = ? WHERE item_id = ?",
                quantity, itemId
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update item quantity: " + e.getMessage());
        }
    }

    public void updateItemPrice(int itemId, double price) {
        try {
            databaseManager.executeUpdate(
                "UPDATE shop_items SET price = ? WHERE item_id = ?",
                price, itemId
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update item price: " + e.getMessage());
        }
    }

    public PlayerShop getShop(int shopId) {
        return shops.get(shopId);
    }

    public List<PlayerShop> getPlayerShops(UUID ownerUuid) {
        List<PlayerShop> playerShops = new ArrayList<>();
        for (PlayerShop shop : shops.values()) {
            if (shop.getOwnerUuid().equals(ownerUuid)) {
                playerShops.add(shop);
            }
        }
        return playerShops;
    }

    public boolean createShopSign(Sign sign, int shopId, String itemName, double price) {
        try {
            PlayerShop shop = getShop(shopId);
            if (shop == null) return false;

            sign.setLine(0, "§6[sc:shop]");
            sign.setLine(1, itemName);
            sign.setLine(2, "§a" + String.format("%.2f", price));
            sign.setLine(3, "ID:" + shopId);
            sign.update();

            // Cache the sign location to shop ID
            String key = sign.getLocation().toString();
            signToShopCache.put(key, shopId);

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create shop sign: " + e.getMessage());
            return false;
        }
    }

    public boolean processShopSign(Player player, Sign sign, String action) {
        try {
            String line3 = sign.getLine(3);
            if (!line3.startsWith("ID:")) return false;

            int shopId = Integer.parseInt(line3.substring(3));
            PlayerShop shop = getShop(shopId);
            
            if (shop == null) return false;

            String itemName = sign.getLine(1);
            String priceStr = sign.getLine(2).replace("§a", "").replace("§c", "");
            double price = Double.parseDouble(priceStr);

            EconomyManager economyManager = ((si.f5.wata777.sacraeconomyv2.SacraEconomyV2) plugin).getEconomyManager();

            if ("buy".equalsIgnoreCase(action)) {
                if (!economyManager.withdraw(player.getUniqueId(), price)) {
                    player.sendMessage("§cお金が足りません！");
                    return false;
                }
                economyManager.deposit(shop.getOwnerUuid(), price);
                player.sendMessage("§a" + itemName + " を " + String.format("%.2f", price) + "で購入しました");
            } else if ("sell".equalsIgnoreCase(action)) {
                economyManager.deposit(player.getUniqueId(), price);
                economyManager.withdraw(shop.getOwnerUuid(), price);
                player.sendMessage("§a" + itemName + " を " + String.format("%.2f", price) + "で売却しました");
            }

            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to process shop sign: " + e.getMessage());
            return false;
        }
    }

    public ShopItem getShopItem(int itemId) {
        for (PlayerShop shop : shops.values()) {
            for (ShopItem item : shop.getItems()) {
                if (item.getItemId() == itemId) {
                    return item;
                }
            }
        }
        return null;
    }

    // Inner Classes
    public static class PlayerShop {
        private final int shopId;
        private final UUID ownerUuid;
        private final String shopName;
        private final String location;
        private final List<ShopItem> items = new ArrayList<>();

        public PlayerShop(int shopId, UUID ownerUuid, String shopName, String location) {
            this.shopId = shopId;
            this.ownerUuid = ownerUuid;
            this.shopName = shopName;
            this.location = location;
        }

        public void addItem(ShopItem item) {
            items.add(item);
        }

        public int getShopId() { return shopId; }
        public UUID getOwnerUuid() { return ownerUuid; }
        public String getShopName() { return shopName; }
        public String getLocation() { return location; }
        public List<ShopItem> getItems() { return items; }
    }

    public static class ShopItem {
        private final int itemId;
        private final String itemName;
        private final double price;
        private int quantity;
        private final boolean forSale;

        public ShopItem(int itemId, String itemName, double price, int quantity, boolean forSale) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.price = price;
            this.quantity = quantity;
            this.forSale = forSale;
        }

        public int getItemId() { return itemId; }
        public String getItemName() { return itemName; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int qty) { this.quantity = qty; }
        public boolean isForSale() { return forSale; }
    }
}
