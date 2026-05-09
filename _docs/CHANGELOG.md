# Changelog

All notable changes to Compass to Map will be documented in this file.
Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) — [Semver](https://semver.org/)

## [Unreleased]

### Notes
- v3 (マルチローダー対応 / Forge backport) は将来予定。`_docs/ROADMAP.md` 参照。

## [2.0.0] - YYYY-MM-DD (未公開)

### Added
- **Nature's Compass 対応**: バイオーム発見時にも自動 waypoint 登録
  - 構造物と同じパターンで動作 (PlayerTickEvent.Post + DataComponent 監視)
  - waypoint 名は `[Biome] Desert (123, -456)` 形式で構造物と区別
  - チャット通知: `Discovered biome %s at %s` / `バイオーム %s を %s で発見しました`
- **バイオーム専用カラーマッピング** (vanilla 14 カテゴリ)
  - 砂漠系=wheat, ジャングル=暗緑, 森林=forest green, 海=blue, 雪=azure, 山岳=gray
  - ネザー=red, エンド=purple, 平原=yellow green, 海岸=moccasin, 沼=dark olive
  - キノコ=pink, 桜=light pink, 洞窟=brown
- **MOD バイオームへの hash 色生成** (構造物より落ち着いた彩度で視覚的に区別: S=0.55, L=0.6)
- **Inner class isolation の二重適用**: NC が classpath に無くても crash しない
- **dedupe key の prefix 拡張**: `s|...` (structure) と `b|...` (biome) で source 種別を区別
- **Config 追加**: `feature.enableStructure` / `feature.enableBiome` で個別 ON/OFF
- **22 言語に biome 翻訳追加** (en/ja は手動、他 20 言語は機械翻訳ベース)

### Changed
- **payload 構造を拡張**: `StructureFoundPayload` 維持 + `BiomeFoundPayload` 追加 (registrar version `"2"`)
- **JourneyMapClientHook**: `prettifyResourceName` (汎用) を導入、`prettifyStructureName` は `@Deprecated` で互換維持
- **CompassWatcher**: 構造物/バイオーム検出を別メソッドに分離、共通ヘルパー (`recordSeen` / `estimateY` / `sendChatNotification`) を抽出

### Compatibility
- Minecraft 1.21.1
- NeoForge 21.1+
- **Optional**: Explorer's Compass (構造物検出、未導入時はバイオーム機能のみ)
- **Optional**: Nature's Compass (バイオーム検出、未導入時は構造物機能のみ)
- Optional: JourneyMap (1.21 系、CLIENT のみ、未導入でも crash しない)
- 動作条件: EC/NC のうち少なくとも 1 つあれば検出機能あり、両方無くても crash せず idle

### Notes
- v1.0 ユーザーは jar 差し替えだけで OK、Config は自動マイグレート (新項目はデフォルト ON)
- NC 不在環境では `naturescompass` の ModList check で早期 return → 永久サスペンド不要
- Mixin / AT / リフレクション 不使用 (v1.0 から維持)

### ⚠️ Server / Client Both Update Required
- payload registrar version が `"1"` → `"2"` に更新されたため、**v2.0 はサーバ・クライアント両方の更新が必要**
- 片方だけ v2.0 に更新すると、registrar version の不一致で payload が一切流れない (mod 自体は `optional()` で disconnect は防止)

## [1.0.0] - YYYY-MM-DD (未公開)

### Added
- Explorer's Compass の構造物発見をフックして、JourneyMap に waypoint を自動登録
- **構造物カテゴリ別の色分け** (バニラ 10 カテゴリ + テンプル系拡張: pyramid / swamp_hut / igloo / 海賊系 shipwreck/buried_treasure)
- **MOD 追加構造物への hash 色生成** (HSL の hue を ID hash から生成、saturation/lightness 固定で鮮明色)
  - vanilla 命名規則の MOD 構造物 (例: `aether:bronze_dungeon`) はキーワード判定で統一色
  - 真にユニーク命名 (例: `terralith:lost_city`) は ID hash で一意の色
- `prettifyStructureName`: snake_case → Title Case で読みやすい waypoint 名
- **dedupe**: dimension + structureId + x + z の Set で履歴保持、A→B→A 再発見でも A の重複登録を防ぐ
- **Y 座標フォールバック**: チャンク未ロードで Heightmap が world floor を返す場合、dimension + 構造物種別ごとに安全な Y を推定 (奈落落ち防止)
  - End: 64 / Nether: 96 / 地下系 (mineshaft 等): 40 / 海洋系 (ocean_monument 等): 80 / その他: 96
- **OP 限定の `/tp` 提案** (非OP には装飾なし、TP 期待を与えない)
- **チャット表示は X, Z のみ** (Y は構造物の実位置とズレるため非表示、TP コマンド内部用にのみ使用)
- 22 言語の lang ファイル (en/ja は手動、他 20 言語は機械翻訳ベース)
- Config: feature.enabled / notification.notifyOnFound / appearance.colorByCategory / appearance.persistentWaypoints

### Compatibility
- Minecraft 1.21.1
- NeoForge 21.1.227+ (mods.toml では `[21.1,)` で緩く指定)
- Required: Explorer's Compass (versionRange `[0,)`、API 不一致時は try-catch で永久サスペンド)
- Optional: JourneyMap (1.21 系、CLIENT のみ、不在時は静かに無視)

### Notes
- Mixin / AccessTransformer / リフレクション不使用
- payload はサーバ→クライアント方向のみ (攻撃面ゼロ)
- EC API 不一致時は永久サスペンドでログ汚染防止
- JM 不在環境でも crash せず動作 (Inner class isolation で NoClassDefFoundError 回避)
