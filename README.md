# Compass to Map (C2M)

> Auto-register found structures **and/or biomes** as JourneyMap waypoints — for Explorer's Compass and/or Nature's Compass.

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Modrinth](https://img.shields.io/badge/Modrinth-compass--to--map-00AF5C)](https://modrinth.com/mod/compass-to-map)
[![CurseForge](https://img.shields.io/badge/CurseForge-compass--to--map-F16436)](https://www.curseforge.com/minecraft/mc-mods/compass-to-map)

---

## Supported Loaders / Versions (v2.0.0+)

| Minecraft | NeoForge | Forge | Fabric |
|---|:---:|:---:|:---:|
| 1.21.1 | ✅ | ✅ | ⚠️ chat-only |
| 1.20.1 |  —  | ✅ | ⚠️ chat-only |

- ✅ = JourneyMap 統合フル対応（waypoint 自動登録）
- ⚠️ chat-only = JourneyMap 統合 disable、チャット通知のみ動作（[既知制限](#known-limitations) 参照）
- — = ホスト MOD (NeoForge) 1.20.1 リリースなし

---

## Why Compass to Map?

Explorer's Compass / Nature's Compass で構造物・バイオームを見つけても、座標を JourneyMap に **手動で waypoint 登録** するのが面倒だった。  
このアドオン MOD はその手間を **完全に自動化** する。

- ✨ **コンパスで発見した瞬間に、自動で JourneyMap に waypoint 登録**
- 🧭 **Explorer's Compass + Nature's Compass 両対応** (v2.0+)
- 🎨 **カテゴリ別の色分け** + **MOD 構造物への hash 色生成** で modpack でも識別性◎
- 🛡️ **OP 限定の `/tp` 提案**（サバイバルプレイヤーには TP リンク非表示）
- 💡 **シンプル**: テクスチャ・モデルなし、純粋な機能アドオン
- 🔌 **既存 MOD に依存**: EC / NC / JM が既に入ってるなら追加するだけ
- 🛟 **JM 不在でも crash しない** (Inner class isolation で安全)

---

## Features

### 構造物発見 → JourneyMap waypoint 自動登録
Explorer's Compass で構造物を検索 → 見つかった瞬間に紫色の waypoint がマップに追加される。

### カテゴリ別色分け
| 構造物 | 色 |
|---|---|
| 村 (village) | 黄 |
| ダンジョン / 鉱山 | 赤 |
| 要塞 / エンドシティ | 紫 |
| 海洋神殿 / 寺院 | シアン |
| ネザー要塞 / 砦 | オレンジ |
| 廃ポータル | グレー |
| 森の洋館 | 茶 |
| 古代都市 | 水色 |
| 試練の間 | ライム |
| その他 | 白 |

設定で OFF にすればブランド色（紫）の単色固定にもできる。

### 重複登録防止
同じ構造物を再検索しても、すでに登録済みの waypoint は再追加されない。  
ディメンション + 構造物 ID + 座標で識別するので、別ディメンションで偶然同座標の構造物があっても誤判定しない。

### OP 限定の TP 提案
チャット通知の座標は OP 権限を持つプレイヤーには **クリック可能な `/tp` コマンド提案**になる。サバイバルプレイヤーには装飾なしの普通の座標表示。サーバ運営者にやさしい挙動。

---

## Installation

1. **ローダーを導入** (Minecraft バージョンに合わせる):
   - 1.21.1 → [NeoForge](https://neoforged.net) / [Forge](https://files.minecraftforge.net) / [Fabric](https://fabricmc.net)
   - 1.20.1 → [Forge](https://files.minecraftforge.net) / [Fabric](https://fabricmc.net)
2. **以下 2 つから少なくとも 1 つ導入** (両方でも OK):
   - [Explorer's Compass](https://modrinth.com/mod/explorers-compass) — 構造物検出に必要
   - [Nature's Compass](https://modrinth.com/mod/natures-compass) — バイオーム検出に必要
3. **Forge / NeoForge のみ**: [JourneyMap](https://modrinth.com/mod/journeymap) を導入（推奨、これがないと waypoint 登録できない）  
   **Fabric**: 現状 JM 統合 disable、チャット通知のみ ([既知制限](#known-limitations))
4. **Fabric のみ**: [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) を追加導入
5. ローダー × MC バージョンに合った `compass2map-2.0.2-{loader}-{mc}.jar` を `mods/` フォルダに放り込む

### 構成パターン

| EC | NC | JM | 動作 |
|---|---|---|---|
| ✅ | ✅ | ✅ | フル機能 (構造物 + バイオーム両方) |
| ✅ | ❌ | ✅ | 構造物のみ |
| ❌ | ✅ | ✅ | バイオームのみ |
| ✅ | ✅ | ❌ | 検出はするが waypoint 登録は無効 (チャット通知だけ動く) |
| ❌ | ❌ | ✅ | C2M は起動するが何もしない |

---

## Configuration

`config/compasstomap-common.toml` を編集（または NeoForge / Forge の Mod Settings GUI から）:

| キー | 既定 | 説明 |
|---|---|---|
| `feature.enabled` | true | マスタースイッチ |
| `feature.enableStructure` | true | Explorer's Compass の構造物 waypoint 登録 |
| `feature.enableBiome` | true | Nature's Compass のバイオーム waypoint 登録 (v2.0+) |
| `notification.notifyOnFound` | true | 発見時のチャット通知 |
| `appearance.colorByCategory` | true | カテゴリ別色分け（OFF で紫単色） |
| `appearance.persistentWaypoints` | true | 次回起動でも waypoint 保持 |

---

## Compatibility

| MOD | サポート | 備考 |
|---|---|---|
| **Explorer's Compass** | optional | 構造物検出のホスト、未導入時はバイオーム機能のみ |
| **Nature's Compass** | optional (v2.0+) | バイオーム検出のホスト、未導入時は構造物機能のみ |
| **JourneyMap** | optional (CLIENT のみ) | waypoint 登録のターゲット、なければ静かに無視 |
| Xaero's Minimap / Worldmap | 未対応 | Xaero は公式 API なし、技術的に連携不可 |
| VoxelMap | 未対応 | 将来検討 |

注: EC / NC のうち少なくとも 1 つ無いと検出機能は動かない。両方無くても起動はする (idle 状態)。

---

## Known Limitations

### Fabric ビルド: JourneyMap 統合 disable
**1.20.1 / 1.21.1 の Fabric ビルドのみ**、JourneyMap への waypoint 自動登録が現状 **無効**になっており、チャット通知のフォールバックのみ動作する。

理由: JourneyMap Fabric jar が要求する Loom 1.14 がまだ unreleased で、現在の Loom 1.10-SNAPSHOT では JM API がリンクできない。

代替動作: 構造物・バイオーム発見時、座標を含むチャットメッセージは正常に表示される（OP には `/tp` クリック提案も付く）。

将来予定: JM v1.1/v2.1 reflection bridge による迂回実装。Loom 1.14 リリースかリフレクションブリッジのどちらか早い方で解消予定。

### NeoForge 1.20.1 ビルドなし
NeoForge は 1.21+ から派生したプロジェクトのため、1.20.1 用 NeoForge ビルドは存在しない。1.20.1 で NeoForge 系を使いたい場合は Forge 1.20.1 ビルドを使ってください。

---

## FAQ

**Q. 既存ワールドに途中から入れて壊れない？**  
A. はい、安全です。ロード時から自動で動作します。

**Q. Xaero's Minimap も使ってるけど対応してくれる？**  
A. 申し訳ないですが Xaero は公式 API がなく、ファイル直書きはマップ MOD 側に上書きで消されるため技術的に不可です。JourneyMap への一本化を推奨します。

**Q. Nature's Compass（バイオーム検索）も対応してる？**  
A. **v2.0 から対応済み**。NC を入れてれば自動でバイオーム waypoint も登録されます (色は biome カテゴリ別)。NC が入ってなくても構造物機能だけで動作します。

**Q. 自分の作った modpack に入れていい？**  
A. もちろん。MIT ライセンスなので modpack 利用 OK、許可・通知不要です。

**Q. シングルプレイとマルチプレイ両方で動く？**  
A. 両方対応。マルチサーバではサーバ・クライアント両方に MOD を入れてください（JourneyMap は CLIENT のみで OK）。

**Q. waypoint がたまりすぎて困る**  
A. JourneyMap の waypoint 管理画面から手動削除できます。v2 で「一括削除」コマンド追加予定。

---

## Bug Reports / Feature Requests

GitHub Issues に投げてください: [Issues](https://github.com/KURONAMI333/compass-to-map/issues)

---

## License

[MIT License](LICENSE) — modpack 利用、改変、再配布 OK。クレジット不要ですが歓迎。

---

## Credits

- Author: KURONAMI
- Assist: Claude (Anthropic)
- Built on:
  - [Explorer's Compass](https://modrinth.com/mod/explorers-compass) by ChaosTheDude / MattCzyr
  - [JourneyMap](https://modrinth.com/mod/journeymap) by TeamJM
