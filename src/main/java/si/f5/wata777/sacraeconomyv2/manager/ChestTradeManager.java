package si.f5.wata777.sacraeconomyv2.manager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ChestTradeManager {
    
    private final JavaPlugin plugin;
    private final Map<UUID, TradeSession> activeTrades = new HashMap<>();

    public ChestTradeManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startTradeSession(Player buyer, Player seller) {
        UUID tradeId = UUID.randomUUID();
        TradeSession session = new TradeSession(tradeId, buyer.getUniqueId(), seller.getUniqueId(), System.currentTimeMillis());
        
        activeTrades.put(buyer.getUniqueId(), session);
        activeTrades.put(seller.getUniqueId(), session);
        
        openTradeGui(buyer, seller);
    }

    private void openTradeGui(Player buyer, Player seller) {
        Inventory tradeGui = Bukkit.createInventory(null, 27, "§6Trade with " + seller.getName());
        
        // Left side - seller's items (slots 0-8)
        ItemStack sellerTitle = createCustomItem(Material.BARRIER, "§6Seller Items");
        
        // Middle - trade options (slots 9-17)
        ItemStack confirmButton = createCustomItem(Material.GREEN_WOOL, "§aConfirm Trade");
        ItemStack cancelButton = createCustomItem(Material.RED_WOOL, "§cCancel Trade");
        
        // Right side - buyer's items (slots 18-26)
        ItemStack buyerTitle = createCustomItem(Material.BARRIER, "§6Buyer Items");
        
        tradeGui.setItem(0, sellerTitle);
        tradeGui.setItem(13, confirmButton);
        tradeGui.setItem(18, buyerTitle);
        
        buyer.openInventory(tradeGui);
        seller.openInventory(tradeGui);
    }

    private ItemStack createCustomItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void addItemToTrade(UUID playerId, ItemStack item, boolean isSeller) {
        TradeSession session = activeTrades.get(playerId);
        if (session != null) {
            if (isSeller) {
                session.addSellerItem(item);
            } else {
                session.addBuyerItem(item);
            }
        }
    }

    public void confirmTrade(UUID playerId) {
        TradeSession session = activeTrades.get(playerId);
        if (session != null && session.isConfirmedBy(playerId)) {
            // Execute trade
            executeTrade(session);
            
            // Remove session
            activeTrades.remove(session.getBuyerId());
            activeTrades.remove(session.getSellerId());
        }
    }

    private void executeTrade(TradeSession session) {
        Player buyer = Bukkit.getPlayer(session.getBuyerId());
        Player seller = Bukkit.getPlayer(session.getSellerId());
        
        if (buyer == null || seller == null) return;
        
        // Transfer items
        for (ItemStack item : session.getSellerItems()) {
            buyer.getInventory().addItem(item);
        }
        
        for (ItemStack item : session.getBuyerItems()) {
            seller.getInventory().addItem(item);
        }
        
        buyer.sendMessage("§aTradeが完了しました！");
        seller.sendMessage("§aTradeが完了しました！");
    }

    public void cancelTrade(UUID playerId) {
        TradeSession session = activeTrades.get(playerId);
        if (session != null) {
            Player buyer = Bukkit.getPlayer(session.getBuyerId());
            Player seller = Bukkit.getPlayer(session.getSellerId());
            
            if (buyer != null) buyer.sendMessage("§cTradeがキャンセルされました");
            if (seller != null) seller.sendMessage("§cTradeがキャンセルされました");
            
            activeTrades.remove(session.getBuyerId());
            activeTrades.remove(session.getSellerId());
        }
    }

    // Inner class for trade session
    public static class TradeSession {
        private final UUID tradeId;
        private final UUID buyerId;
        private final UUID sellerId;
        private final long createdAt;
        private final List<ItemStack> sellerItems = new ArrayList<>();
        private final List<ItemStack> buyerItems = new ArrayList<>();
        private final Set<UUID> confirmed = new HashSet<>();

        public TradeSession(UUID tradeId, UUID buyerId, UUID sellerId, long createdAt) {
            this.tradeId = tradeId;
            this.buyerId = buyerId;
            this.sellerId = sellerId;
            this.createdAt = createdAt;
        }

        public void addSellerItem(ItemStack item) {
            sellerItems.add(item);
        }

        public void addBuyerItem(ItemStack item) {
            buyerItems.add(item);
        }

        public boolean isConfirmedBy(UUID playerId) {
            confirmed.add(playerId);
            return confirmed.size() == 2; // Both players confirmed
        }

        public UUID getTradeId() { return tradeId; }
        public UUID getBuyerId() { return buyerId; }
        public UUID getSellerId() { return sellerId; }
        public List<ItemStack> getSellerItems() { return sellerItems; }
        public List<ItemStack> getBuyerItems() { return buyerItems; }
        public boolean isExpired(long timeout) { return System.currentTimeMillis() - createdAt > timeout; }
    }
}
