package si.f5.wata777.sacraeconomyv2.listener;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import si.f5.wata777.sacraeconomyv2.SacraEconomyV2;

public class PlayerEventListener implements Listener {
    
    private final SacraEconomyV2 plugin;

    public PlayerEventListener(SacraEconomyV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Create account if not exists
        plugin.getEconomyManager().createAccount(player.getUniqueId(), player.getName());
        
        plugin.getLogger().info("Account created/loaded for " + player.getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Cancel active trades
        if (plugin.getTradeRequestManager() != null) {
            plugin.getTradeRequestManager().cancelTrade(player.getUniqueId());
        }
        
        plugin.getLogger().info("Player " + player.getName() + " has left the server");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getState() instanceof Sign) {
                Sign sign = (Sign) event.getClickedBlock().getState();
                String line1 = sign.getLine(0);
                
                // Check for shop sign
                if (line1 != null && line1.contains("[sc:shop]")) {
                    event.setCancelled(true);
                    
                    String line3 = sign.getLine(3);
                    if (line3 != null && line3.startsWith("ID:")) {
                        try {
                            int shopId = Integer.parseInt(line3.substring(3));
                            plugin.getShopManager().processShopSign(player, sign, "buy");
                        } catch (NumberFormatException e) {
                            player.sendMessage("§cショップサインが無効です");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        
        String line1 = event.getLine(0);
        if (line1 != null && line1.toLowerCase().contains("[sc:")) {
            // Prevent creating signs without proper permissions
            if (!player.hasPermission("sacraeconomy.shop.create")) {
                event.setCancelled(true);
                player.sendMessage("§cそのサインを作成する権限がありません");
            }
        }
    }
}
