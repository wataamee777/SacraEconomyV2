# SacraEconomyV2 v1.1.0 - リリースノート

## 🎉 新機能

### 1. トレードシステム完全実装
- **トレードリクエスト**: `/sc trade <player>` で他のプレイヤーにトレード申請
- **トレード受け入れ/拒否**: `/sc trade accept` または `/sc trade decline`
- **トレードキャンセル**: `/sc trade cancel` でアクティブなトレードをキャンセル
- **自動タイムアウト**: 5分以上のトレードは自動的にキャンセル
- **双方確認システム**: 両プレイヤーが確認する必要がある

### 2. プレイヤーショップシステム完全実装
- **ショップ作成**: `/sc shop create <名前>` で新規ショップ作成
- **ショップ管理**: `/sc shop list` でショップ一覧表示、`/sc shop delete <ID>` で削除
- **ショップサイン**: `[sc:shop]` プレッシブで作成（管理者権限必須）
- **サイン統合**: 右クリックで即座に購入可能
- **在庫管理**: データベースで正確に管理

### 3. 拡張イベント処理
- **サイン右クリック検出**: ショップサインの自動処理
- **プレイヤー退出時処理**: 退出時にアクティブなトレードを自動キャンセル
- **サイン作成権限**: 権限ベースでサイン作成を制御

## 📊 技術的改善

### 新しいマネージャークラス
- **TradeRequestManager**: トレードリクエスト＆セッション管理
  - トレードリクエスト保管
  - トレードセッション追跡
  - タイムアウト管理
  - 双方確認システム

- **ShopManager**: プレイヤーショップ管理
  - ショップCRUD操作
  - サイン処理
  - 在庫・価格管理
  - トランザクション処理

### コード量増加
- 合計12個のJavaファイル（v1.0.0から+2）
- TradeRequestManager.java (200+ lines)
- ShopManager.java (280+ lines)
- 既存ファイル拡張

### データベーステーブル（既存のまま）
- player_shops
- shop_items
- player_accounts
- transactions
- achievements
- giveaway_codes
- work_cooldown

## 🔧 コマンド体系の整理

### 経済関連
```
/sc status        - 自分の情報表示
/sc work <job>    - 仕事で稼ぐ
/sc redeem <code> - ギフトコード
```

### 公式ショップ
```
/sc offbuy        - 公式ショップから購入
/sc offsell       - 公式ショップに売却
```

### プレイヤー間取引
```
/sc trade <player>  - トレード申請
/sc trade accept    - 承認
/sc trade decline   - 拒否
/sc trade cancel    - キャンセル
```

### プレイヤーショップ
```
/sc shop create <名前>  - ショップ作成
/sc shop list           - ショップ一覧
/sc shop delete <ID>    - ショップ削除
/sc settings            - 設定（JSON UI）
```

### 管理者
```
/sc reload  - 設定リロード (op権限)
```

## 🔐 パーミッション

新しいパーミッションを追加：
- `sacraeconomy.admin` - 管理者コマンド
- `sacraeconomy.shop.create` - ショップサイン作成
- `sacraeconomy.trade` - トレード機能
- `sacraeconomy.shop` - ショップ機能

## 📈 パフォーマンス

- 非同期タスク: トレードタイムアウト処理（毎60秒）
- キャッシング: ショップデータのメモリキャッシュ
- データベース: 接続は1つの共有インスタンス

## 🐛 修正事項

- トレード中断時のアイテム損失を防止
- サイン無効化の自動検出
- デッドロック防止メカニズム

## ⚠️ 既知の制限

1. **チェストGUI**: 完全なインタラクティブUIは実装未定（v1.2予定）
2. **グローバルマーケット**: マーケット掲示板機能は未実装（v1.2予定）
3. **在庫同期**: 複数サーバー間の同期機能は未実装

## 📚 API の変更

### 新しいメソッド
```java
// TradeRequestManager
void sendTradeRequest(UUID from, UUID to)
boolean acceptTradeRequest(UUID player)
void confirmTrade(UUID player)
void cancelTrade(UUID player)

// ShopManager
int createShop(UUID owner, String name)
void deleteShop(int shopId)
boolean createShopSign(Sign sign, int shopId, String item, double price)
boolean processShopSign(Player player, Sign sign, String action)
```

### 変更されたメソッド
- `PlayerEventListener`: イベント処理が大幅に拡張

## 🚀 アップグレード手順

### v1.0.1 から v1.1.0 へ
1. サーバーを停止
2. 古いJARを削除
3. 新しいJAR (SacraEconomyV2-1.1.0.jar) をデプロイ
4. サーバー起動
5. `/sc reload` で設定をリロード（オプション）

**注意**: データベースは自動マイグレーションされません（同じスキーマを使用）

## 📊 ステータス

| 機能 | v1.0.0 | v1.1.0 |
|-----|--------|--------|
| 経済システム | ✅ | ✅ |
| 職業システム | ✅ | ✅ |
| 実績システム | ✅ | ✅ |
| ギフトコード | ✅ | ✅ |
| トレード | 🔲 | ✅ |
| プレイヤーショップ | 🔲 | ✅ |
| チェストGUI | 🔲 | ⏳ |
| グローバルマーケット | 🔲 | ⏳ |
| JSON UI | 🔲 | ⏳ |

**凡例**: ✅ = 実装済み, ⏳ = 次バージョン予定, 🔲 = 未実装

## 🎯 次のマイルストーン (v1.2.0)

- チェストGUIのインタラクティブ機能
- グローバルマーケット掲示板
- 自動マッチング機能
- 詳細なJSON UI

---

**リリース日**: 2026年9月2日  
**ビルド**: SacraEconomyV2-1.1.0.jar (14MB)  
**Java要件**: 21+  
**Paper要件**: 1.21+
