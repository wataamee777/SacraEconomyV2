package si.f5.wata777.sacraeconomyv2.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import si.f5.wata777.sacraeconomyv2.SacraEconomyV2;

public class CommandHandler implements CommandExecutor {
    
    private final SacraEconomyV2 plugin;

    public CommandHandler(SacraEconomyV2 plugin) {
        this.plugin = plugin;
        plugin.getCommand("sc").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "reload":
                handleReload(sender);
                break;
            case "status":
                handleStatus(sender);
                break;
            case "work":
                handleWork(sender, args);
                break;
            case "redeem":
                handleRedeem(sender, args);
                break;
            case "buy":
                handleBuy(sender, args);
                break;
            case "sell":
                handleSell(sender, args);
                break;
            case "offbuy":
                handleOffBuy(sender, args);
                break;
            case "offsell":
                handleOffSell(sender, args);
                break;
            case "trade":
                handleTrade(sender, args);
                break;
            case "shop":
                handleShop(sender, args);
                break;
            case "settings":
                handleSettings(sender);
                break;
            case "admin":
                handleAdmin(sender, args);
                break;
            default:
                sendHelpMessage(sender);
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("sacraeconomy.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return;
        }
        plugin.getConfigManager().reloadConfig();
        sender.sendMessage("§aConfiguration reloaded successfully!");
    }

    private void handleStatus(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return;
        }
        Player player = (Player) sender;
        String username = plugin.getEconomyManager().getUsername(player.getUniqueId());
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        int level = plugin.getEconomyManager().getLevel(player.getUniqueId());
        String job = plugin.getEconomyManager().getJob(player.getUniqueId());
        String symbol = plugin.getConfigManager().getCurrencySymbol();

        player.sendMessage("§6§m----------------------------------------");
        player.sendMessage("§b§lSacra Economy Status");
        player.sendMessage("§7Player: §f" + username);
        player.sendMessage("§7Balance: §f" + symbol + String.format("%.2f", balance));
        player.sendMessage("§7Level: §f" + level);
        player.sendMessage("§7Job: §f" + (job != null ? job : "None"));
        player.sendMessage("§6§m----------------------------------------");
    }

    private void handleWork(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return;
        }
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("§cUsage: /sc work <job>");
            return;
        }

        String job = args[1].toLowerCase();
        
        if (!plugin.getEconomyManager().canWork(player.getUniqueId())) {
            int cooldown = plugin.getConfigManager().getJobCooldown();
            player.sendMessage("§cYou must wait before working again! (Cooldown: " + cooldown + "s)");
            return;
        }

        plugin.getEconomyManager().work(player.getUniqueId(), job);
        double reward = plugin.getConfigManager().getJobReward(job);
        String symbol = plugin.getConfigManager().getCurrencySymbol();
        player.sendMessage("§aYou worked as a " + job + " and earned " + symbol + String.format("%.2f", reward) + "!");
    }

    private void handleRedeem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return;
        }
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("§cUsage: /sc redeem <code>");
            return;
        }

        String code = args[1];
        if (plugin.getEconomyManager().useGiveawayCode(player.getUniqueId(), code)) {
            player.sendMessage("§aGiveaway code redeemed successfully!");
        } else {
            player.sendMessage("§cInvalid or expired giveaway code!");
        }
    }

    private void handleBuy(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return;
        }
        sender.sendMessage("§6/sc buy feature coming soon!");
    }

    private void handleSell(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return;
        }
        sender.sendMessage("§6/sc sell feature coming soon!");
    }

    private void handleOffBuy(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return;
        }
        sender.sendMessage("§6/sc offbuy feature coming soon!");
    }

    private void handleOffSell(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return;
        }
        sender.sendMessage("§6/sc offsell feature coming soon!");
    }

    private void handleTrade(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return;
        }
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("§6トレードコマンド:");
            player.sendMessage("§7/sc trade <player> - トレードリクエスト送信");
            player.sendMessage("§7/sc trade accept - トレード承認");
            player.sendMessage("§7/sc trade decline - トレード拒否");
            player.sendMessage("§7/sc trade cancel - トレードキャンセル");
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "accept":
                plugin.getTradeRequestManager().acceptTradeRequest(player.getUniqueId());
                break;
            case "decline":
                plugin.getTradeRequestManager().declineTradeRequest(player.getUniqueId());
                break;
            case "cancel":
                plugin.getTradeRequestManager().cancelTrade(player.getUniqueId());
                break;
            default:
                // Assume it's a player name
                Player targetPlayer = sender.getServer().getPlayer(args[1]);
                if (targetPlayer == null) {
                    player.sendMessage("§cそのプレイヤーは見つかりません");
                    return;
                }
                plugin.getTradeRequestManager().sendTradeRequest(player.getUniqueId(), targetPlayer.getUniqueId());
        }
    }

    private void handleShop(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return;
        }
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("§6ショップコマンド:");
            player.sendMessage("§7/sc shop create <名前> - ショップ作成");
            player.sendMessage("§7/sc shop list - ショップ一覧");
            player.sendMessage("§7/sc shop delete <名前> - ショップ削除");
            player.sendMessage("§7/sc shop additem <shopId> <item> <価格> - アイテム追加");
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "create":
                if (args.length < 3) {
                    player.sendMessage("§c使用法: /sc shop create <名前>");
                    return;
                }
                String shopName = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                int shopId = plugin.getShopManager().createShop(player.getUniqueId(), shopName);
                if (shopId > 0) {
                    player.sendMessage("§aショップを作成しました！ ID: " + shopId);
                } else {
                    player.sendMessage("§cショップの作成に失敗しました");
                }
                break;

            case "list":
                java.util.List<si.f5.wata777.sacraeconomyv2.manager.ShopManager.PlayerShop> shops = 
                    plugin.getShopManager().getPlayerShops(player.getUniqueId());
                if (shops.isEmpty()) {
                    player.sendMessage("§7ショップがありません");
                    return;
                }
                player.sendMessage("§6あなたのショップ:");
                for (si.f5.wata777.sacraeconomyv2.manager.ShopManager.PlayerShop shop : shops) {
                    player.sendMessage("§7- " + shop.getShopName() + " (ID: " + shop.getShopId() + ")");
                }
                break;

            case "delete":
                if (args.length < 3) {
                    player.sendMessage("§c使用法: /sc shop delete <ID>");
                    return;
                }
                try {
                    int delShopId = Integer.parseInt(args[2]);
                    si.f5.wata777.sacraeconomyv2.manager.ShopManager.PlayerShop delShop = 
                        plugin.getShopManager().getShop(delShopId);
                    if (delShop == null || !delShop.getOwnerUuid().equals(player.getUniqueId())) {
                        player.sendMessage("§cそのショップは見つかりません");
                        return;
                    }
                    plugin.getShopManager().deleteShop(delShopId);
                    player.sendMessage("§aショップを削除しました");
                } catch (NumberFormatException e) {
                    player.sendMessage("§c無効なID");
                }
                break;

            default:
                player.sendMessage("§c不明なコマンド");
        }
    }

    private void handleSettings(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return;
        }
        sender.sendMessage("§6/sc settings JSON UI coming soon!");
    }

    private void handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("sacraeconomy.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return;
        }
        sender.sendMessage("§6Admin commands coming soon!");
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§6§m----------------------------------------");
        sender.sendMessage("§b§lSacra Economy V2 Help");
        sender.sendMessage("§7/sc reload - 設定をリロード (管理者)");
        sender.sendMessage("§7/sc status - ステータス表示");
        sender.sendMessage("§7/sc work <job> - 仕事で稼ぐ");
        sender.sendMessage("§7/sc redeem <code> - ギフトコード使用");
        sender.sendMessage("§7/sc offbuy - 公式ショップから購入");
        sender.sendMessage("§7/sc offsell - 公式ショップに売却");
        sender.sendMessage("§7/sc buy - プレイヤーから購入");
        sender.sendMessage("§7/sc sell - プレイヤーに売却");
        sender.sendMessage("§7/sc trade <player> - プレイヤーとトレード");
        sender.sendMessage("§7/sc shop - ショップ管理");
        sender.sendMessage("§7/sc settings - 設定を表示");
        sender.sendMessage("§6§m----------------------------------------");
    }
}
