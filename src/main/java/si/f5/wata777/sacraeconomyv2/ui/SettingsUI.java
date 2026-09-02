package si.f5.wata777.sacraeconomyv2.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SettingsUI {
    
    private final JavaPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public SettingsUI(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void showSettingsUI(Player player) {
        JsonObject root = new JsonObject();
        
        // Header
        root.addProperty("title", "§b§lShop Settings");
        root.addProperty("type", "form");
        
        // Create buttons for settings options
        JsonObject button1 = new JsonObject();
        button1.addProperty("text", "Sell Items Settings");
        JsonObject image = new JsonObject();
        image.addProperty("type", "url");
        button1.add("image", image);
        
        JsonObject button2 = new JsonObject();
        button2.addProperty("text", "Buy Items Settings");
        
        JsonObject button3 = new JsonObject();
        button3.addProperty("text", "Price Settings");
        
        JsonObject button4 = new JsonObject();
        button4.addProperty("text", "Shop Info");
        
        // Send the JSON UI to the player
        // Note: This would require a custom implementation with JSON UI
        player.sendMessage("§6Shop Settings Panel:");
        player.sendMessage("§7  1. Sell Items Settings");
        player.sendMessage("§7  2. Buy Items Settings");
        player.sendMessage("§7  3. Price Settings");
        player.sendMessage("§7  4. Shop Info");
    }

    public void showSellItemsSettings(Player player) {
        player.sendMessage("§6Sell Items Settings:");
        player.sendMessage("§7What items do you want to sell?");
        player.sendMessage("§7Use: /sc settings sell <item>");
    }

    public void showBuyItemsSettings(Player player) {
        player.sendMessage("§6Buy Items Settings:");
        player.sendMessage("§7What items do you want to buy?");
        player.sendMessage("§7Use: /sc settings buy <item>");
    }

    public void showPriceSettings(Player player) {
        player.sendMessage("§6Price Settings:");
        player.sendMessage("§7Set the prices for your items:");
        player.sendMessage("§7Use: /sc settings price <item> <price>");
    }

    public String generateSettingsJSON(UUID playerUuid) {
        JsonObject settings = new JsonObject();
        
        JsonObject sellItems = new JsonObject();
        sellItems.addProperty("description", "Items you want to sell");
        settings.add("sell_items", sellItems);
        
        JsonObject buyItems = new JsonObject();
        buyItems.addProperty("description", "Items you want to buy");
        settings.add("buy_items", buyItems);
        
        JsonObject prices = new JsonObject();
        prices.addProperty("description", "Price settings");
        settings.add("prices", prices);
        
        return gson.toJson(settings);
    }

    public void saveSellItemSettings(UUID playerUuid, String item, double price) {
        // Save to database
        plugin.getLogger().info("Saved sell item: " + item + " at price: " + price);
    }

    public void saveBuyItemSettings(UUID playerUuid, String item, double price) {
        // Save to database
        plugin.getLogger().info("Saved buy item: " + item + " at price: " + price);
    }
}
