# SacraEconomyV2 - デプロイメント & セットアップガイド

## 🚀 インストール手順

### 1. ビルド
```bash
git clone https://github.com/wataamee777/SacraEconomyV2.git
cd SacraEconomyV2
mvn clean package
```

### 2. デプロイ
```bash
# JARファイルをプラグインフォルダにコピー
cp target/SacraEconomyV2-1.0.0.jar /path/to/server/plugins/

# サーバーを再起動
```

### 3. 初期設定
1. サーバーを起動させ、プラグインを読み込ませる
2. `plugins/SacraEconomyV2/config.yml` が自動生成される
3. 必要に応じてconfigを編集
4. `/sc reload` でコンフィグをリロード

## 📋 必要な環境
- **Java**: 21 以上
- **Paper Server**: 1.21 以上
- **Database**: SQLite（デフォルト）または MySQL

## 🎮 プレイヤーコマンド一覧

### ステータス確認
```
/sc                 - ヘルプを表示
/sc status          - アカウント情報を表示
/sc settings        - ショップ設定UI (JSON UI)
```

### 経済・職業システム
```
/sc work <job>      - 仕事をして金稼ぎ
                      対応職業: mining, farming, fishing, hunting, crafting

/sc redeem <code>   - ギフトコードを使用
                      例: /sc redeem NEW2024
```

### 公式ショップ
```
/sc offbuy          - 公式ショップから購入
/sc offsell         - 公式ショップに売却
```

### プレイヤー間取引
```
/sc buy             - プレイヤーから購入
/sc sell            - プレイヤーに売却
/sc trade           - チェストGUIでトレード
```

## 👨‍💼 管理者コマンド

```
/sc reload          - コンフィグをリロード（权限: sacraeconomy.admin）
/sc admin           - 管理者機能メニュー（今後実装予定）
```

## ⚙️ 設定ファイル詳細

### config.yml - 主要設定

**データベース設定**
```yaml
database:
  type: "sqlite"  # sqlite または mysql
```

**経済システム**
```yaml
economy:
  currency-symbol: "¥"           # 通貨記号
  starting-balance: 1000.0       # 初期残高
  max-balance: 9999999999.0      # 最大残高
```

**職業システム**
```yaml
jobs:
  cooldown-seconds: 300          # 仕事のクールタイム（秒）
  rewards:
    mining: 150.0                # 採掘の報酬
    farming: 120.0               # 農業の報酬
    fishing: 140.0               # 漁業の報酬
    hunting: 130.0               # 狩猟の報酬
    crafting: 110.0              # 工作の報酬
```

**公式ショップ**
```yaml
official-shop:
  buy-multiplier: 0.8            # 売却時は定価の80%
  sell-multiplier: 1.2           # 購入時は定価の120%
```

**実績・報酬システム**
```yaml
achievements:
  rewards-per-level:
    1: 500.0                     # レベル1の報酬
    2: 750.0                     # レベル2の報酬
    3: 1000.0                    # レベル3の報酬
    4: 1500.0                    # レベル4の報酬
    5: 2000.0                    # レベル5の報酬
```

**ギフトコード**
```yaml
giveaways:
  default-codes:
    "NEW2024": 5000.0            # コード: 報酬額
    "WELCOME": 2500.0
```

## 💾 データベース構造

### テーブル一覧

**player_accounts**
- プレイヤーのアカウント情報
- UUID, ユーザー名, 残高, レベル, 職業

**transactions**
- 取引履歴
- 送信者, 受信者, 金額, 取引タイプ, 説明

**achievements**
- プレイヤーの実績
- UUID, 実績ID, レベル

**giveaway_codes**
- ギフトコード管理
- コード, 金額, 最大使用回数, 現在の使用回数

**player_shops**
- プレイヤーショップ情報
- ショップID, 所有者UUID, ショップ名, 位置

**shop_items**
- ショップアイテム
- アイテムID, ショップID, アイテム名, 価格, 数量

## 🔧 トラブルシューティング

### プラグインが読み込まれない
1. サーバーログを確認: `logs/latest.log`
2. Java バージョン確認: `java -version` (21以上必須)
3. plugin.yml の構文確認

### データベースエラー
1. SQLiteの場合、パーミッションを確認
2. MySQLの場合、接続設定を確認
3. config.ymlのデータベース設定を確認

### コマンドが実行できない
1. プレイヤーがオンラインか確認
2. パーミッションを確認
3. `/sc` でヘルプが表示されるか確認

## 📦 ファイル構成

```
SacraEconomyV2/
├── pom.xml                          # Mavenビルド設定
├── README.md                         # 概要ドキュメント
├── DEPLOY.md                        # このファイル
├── src/
│   └── main/
│       ├── java/jp/wataamee777/sacraeconomyv2/
│       │   ├── SacraEconomyV2.java         # メインクラス
│       │   ├── command/
│       │   │   └── CommandHandler.java      # コマンド処理
│       │   ├── manager/
│       │   │   ├── ConfigManager.java       # 設定管理
│       │   │   ├── DatabaseManager.java     # DB操作
│       │   │   ├── EconomyManager.java      # 経済管理
│       │   │   ├── AchievementManager.java  # 実績管理
│       │   │   ├── ChestTradeManager.java   # トレード管理
│       │   │   └── PlayerShopManager.java   # プレイヤーショップ
│       │   ├── listener/
│       │   │   └── PlayerEventListener.java # イベントリスナー
│       │   └── ui/
│       │       └── SettingsUI.java          # 設定UI
│       └── resources/
│           ├── plugin.yml                    # プラグイン設定
│           └── config.yml                    # デフォルト設定
└── target/
    └── SacraEconomyV2-1.0.0.jar            # 生成されたJAR
```

## 🔗 関連リンク

- [Paper Server](https://papermc.io/)
- [Bukkit/Spigot API](https://hub.spigotmc.org/)
- [Maven Documentation](https://maven.apache.org/)

## 📝 ライセンス

Developed for Sacra Servers

---

**バージョン**: 1.0.0  
**最終更新**: 2026年9月2日
