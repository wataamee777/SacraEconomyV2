package si.f5.wata777.sacraeconomyv2.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PlayerShopManager {
    
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<Integer, PlayerShop> shops = new HashMap<>();

    public PlayerShopManager(JavaPlugin plugin, DatabaseManager databaseManager) {
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
                shops.put(shopId, shop);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load shops: " + e.getMessage());
        }
    }

    public int createShop(UUID ownerUuid, String shopName, String location) {
        try {
            databaseManager.executeUpdate(
                "INSERT INTO player_shops (owner_uuid, shop_name, location) VALUES (?, ?, ?)",
                ownerUuid.toString(), shopName, location
            );
            
            // Get the inserted shop ID
            ResultSet rs = databaseManager.executeQuery(
                "SELECT shop_id FROM player_shops WHERE owner_uuid = ? AND shop_name = ? ORDER BY shop_id DESC LIMIT 1",
                ownerUuid.toString(), shopName
            );
            
            if (rs.next()) {
                int shopId = rs.getInt("shop_id");
                PlayerShop shop = new PlayerShop(shopId, ownerUuid, shopName, location);
                shops.put(shopId, shop);
                return shopId;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create shop: " + e.getMessage());
        }
        return -1;
    }

    public void addItemToShop(int shopId, String itemName, double price, int quantity, boolean forSale) {
        try {
            databaseManager.executeUpdate(
                "INSERT INTO shop_items (shop_id, item_name, price, quantity, for_sale) VALUES (?, ?, ?, ?, ?)",
                shopId, itemName, price, quantity, forSale ? 1 : 0
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add item to shop: " + e.getMessage());
        }
    }

    public void removeItemFromShop(int itemId) {
        try {
            databaseManager.executeUpdate(
                "DELETE FROM shop_items WHERE item_id = ?",
                itemId
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove item from shop: " + e.getMessage());
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

    public boolean processSignTrade(Sign sign, UUID buyerUuid) {
        String[] lines = sign.getLines();
        
        // Check for [sc:sell] or [sc:buy] format
        if (!lines[0].contains("[sc:")) {
            return false;
        }
        
        String type = lines[0].toLowerCase();
        String itemName = lines[1];
        String priceStr = lines[2];
        String shopIdStr = lines[3];
        
        try {
            double price = Double.parseDouble(priceStr);
            int shopId = Integer.parseInt(shopIdStr);
            
            PlayerShop shop = getShop(shopId);
            if (shop == null) return false;
            
            if (type.contains("sell")) {
                // Buyer wants to sell to shop owner
                return sellToShop(buyerUuid, shop.getOwnerUuid(), price);
            } else if (type.contains("buy")) {
                // Buyer wants to buy from shop owner
                return buyFromShop(buyerUuid, shop.getOwnerUuid(), price);
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid shop sign format: " + e.getMessage());
        }
        
        return false;
    }

    private boolean sellToShop(UUID sellerUuid, UUID buyerUuid, double price) {
        // Implementation for selling to shop
        return true;
    }

    private boolean buyFromShop(UUID buyerUuid, UUID sellerUuid, double price) {
        // Implementation for buying from shop
        return true;
    }

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
        public boolean isForSale() { return forSale; }
    }
}
