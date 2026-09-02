package si.f5.wata777.sacraeconomyv2.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import si.f5.wata777.sacraeconomyv2.SacraEconomyV2;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class TradeRequestManager {
    
    private final JavaPlugin plugin;
    private final Map<UUID, TradeRequest> activeRequests = new HashMap<>();
    private final Map<UUID, TradeSession> activeSessions = new HashMap<>();

    public TradeRequestManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    // Trade Request Management
    public void sendTradeRequest(UUID from, UUID to) {
        Player fromPlayer = Bukkit.getPlayer(from);
        Player toPlayer = Bukkit.getPlayer(to);

        if (fromPlayer == null || toPlayer == null) {
            if (fromPlayer != null) {
                fromPlayer.sendMessage("§c相手がオンラインではありません！");
            }
            return;
        }

        if (from.equals(to)) {
            fromPlayer.sendMessage("§c自分自身とはトレードできません！");
            return;
        }

        // Cancel existing request if any
        if (activeRequests.containsKey(to)) {
            fromPlayer.sendMessage("§c相手はすでにトレードリクエストを受け取っています");
            return;
        }

        TradeRequest request = new TradeRequest(from, to, System.currentTimeMillis());
        activeRequests.put(to, request);

        fromPlayer.sendMessage("§aトレードリクエストを送信しました: " + toPlayer.getName());
        toPlayer.sendMessage("§6" + fromPlayer.getName() + " からトレードリクエストが届きました！");
        toPlayer.sendMessage("§7/sc trade accept を入力してください");
    }

    public boolean acceptTradeRequest(UUID player) {
        TradeRequest request = activeRequests.get(player);

        if (request == null) {
            Player p = Bukkit.getPlayer(player);
            if (p != null) {
                p.sendMessage("§cトレードリクエストがありません");
            }
            return false;
        }

        Player initiator = Bukkit.getPlayer(request.getFrom());
        Player responder = Bukkit.getPlayer(request.getTo());

        if (initiator == null || responder == null) {
            activeRequests.remove(player);
            if (responder != null) {
                responder.sendMessage("§c相手がオンラインではありません");
            }
            return false;
        }

        // Create trade session
        TradeSession session = new TradeSession(
            request.getFrom(),
            request.getTo(),
            System.currentTimeMillis()
        );
        
        activeSessions.put(request.getFrom(), session);
        activeSessions.put(request.getTo(), session);
        activeRequests.remove(player);

        initiator.sendMessage("§aトレードが始まりました！");
        responder.sendMessage("§aトレードが始まりました！");

        return true;
    }

    public boolean declineTradeRequest(UUID player) {
        TradeRequest request = activeRequests.remove(player);

        if (request == null) {
            return false;
        }

        Player responder = Bukkit.getPlayer(request.getTo());
        Player initiator = Bukkit.getPlayer(request.getFrom());

        if (responder != null) {
            responder.sendMessage("§cトレードリクエストを拒否しました");
        }
        if (initiator != null) {
            initiator.sendMessage("§c" + (responder != null ? responder.getName() : "相手") + " がトレードを拒否しました");
        }

        return true;
    }

    // Trade Session Management
    public TradeSession getTradeSession(UUID player) {
        return activeSessions.get(player);
    }

    public void confirmTrade(UUID player) {
        TradeSession session = activeSessions.get(player);
        if (session == null) return;

        session.confirmBy(player);

        Player otherPlayer = Bukkit.getPlayer(
            player.equals(session.getPlayer1()) ? session.getPlayer2() : session.getPlayer1()
        );

        Player currentPlayer = Bukkit.getPlayer(player);
        if (currentPlayer != null) {
            currentPlayer.sendMessage("§aトレードを確認しました。相手を待機中...");
        }

        if (otherPlayer != null) {
            otherPlayer.sendMessage("§6相手がトレードを確認しました！");
        }

        // If both confirmed, execute trade
        if (session.areBothConfirmed()) {
            executeTrade(session);
        }
    }

    public void cancelTrade(UUID player) {
        TradeSession session = activeSessions.get(player);
        if (session == null) return;

        UUID otherUuid = player.equals(session.getPlayer1()) ? session.getPlayer2() : session.getPlayer1();

        activeSessions.remove(player);
        activeSessions.remove(otherUuid);

        Player currentPlayer = Bukkit.getPlayer(player);
        Player otherPlayer = Bukkit.getPlayer(otherUuid);

        if (currentPlayer != null) {
            currentPlayer.sendMessage("§cトレードをキャンセルしました");
        }
        if (otherPlayer != null) {
            otherPlayer.sendMessage("§c相手がトレードをキャンセルしました");
        }
    }

    private void executeTrade(TradeSession session) {
        Player player1 = Bukkit.getPlayer(session.getPlayer1());
        Player player2 = Bukkit.getPlayer(session.getPlayer2());

        if (player1 == null || player2 == null) {
            return;
        }

        // Transfer items
        session.getPlayer1Items().forEach(item -> player2.getInventory().addItem(item));
        session.getPlayer2Items().forEach(item -> player1.getInventory().addItem(item));

        player1.sendMessage("§aトレードが完了しました！");
        player2.sendMessage("§aトレードが完了しました！");

        activeSessions.remove(session.getPlayer1());
        activeSessions.remove(session.getPlayer2());
    }

    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            long timeout = TimeUnit.MINUTES.toMillis(5); // 5 minute timeout

            // Clean expired requests
            activeRequests.entrySet().removeIf(entry -> {
                if (now - entry.getValue().getCreatedAt() > timeout) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null) {
                        player.sendMessage("§cトレードリクエストが期限切れになりました");
                    }
                    return true;
                }
                return false;
            });

            // Clean expired sessions
            activeSessions.entrySet().removeIf(entry -> {
                if (now - entry.getValue().getCreatedAt() > timeout) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null) {
                        player.sendMessage("§cトレードセッションがタイムアウトしました");
                    }
                    return true;
                }
                return false;
            });
        }, 0L, 60L * 20L); // Check every 60 seconds
    }

    // Inner Classes
    public static class TradeRequest {
        private final UUID from;
        private final UUID to;
        private final long createdAt;

        public TradeRequest(UUID from, UUID to, long createdAt) {
            this.from = from;
            this.to = to;
            this.createdAt = createdAt;
        }

        public UUID getFrom() { return from; }
        public UUID getTo() { return to; }
        public long getCreatedAt() { return createdAt; }
    }

    public static class TradeSession {
        private final UUID player1;
        private final UUID player2;
        private final long createdAt;
        private final Set<UUID> confirmed = new HashSet<>();
        private final List<org.bukkit.inventory.ItemStack> player1Items = new ArrayList<>();
        private final List<org.bukkit.inventory.ItemStack> player2Items = new ArrayList<>();

        public TradeSession(UUID player1, UUID player2, long createdAt) {
            this.player1 = player1;
            this.player2 = player2;
            this.createdAt = createdAt;
        }

        public void addPlayer1Item(org.bukkit.inventory.ItemStack item) {
            player1Items.add(item);
        }

        public void addPlayer2Item(org.bukkit.inventory.ItemStack item) {
            player2Items.add(item);
        }

        public void confirmBy(UUID player) {
            confirmed.add(player);
        }

        public boolean areBothConfirmed() {
            return confirmed.size() == 2;
        }

        public UUID getPlayer1() { return player1; }
        public UUID getPlayer2() { return player2; }
        public long getCreatedAt() { return createdAt; }
        public List<org.bukkit.inventory.ItemStack> getPlayer1Items() { return player1Items; }
        public List<org.bukkit.inventory.ItemStack> getPlayer2Items() { return player2Items; }
    }
}
