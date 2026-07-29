# Publishing Checklist — Compass to Map (C2M)

Modrinth / CurseForge への公開・更新手順。

> 旧版は初回 v1.0.0・NeoForge/1.21.1 専用・Explorer's Compass 必須 を前提に
> していたが、現状と乖離していたため現実に合わせて改訂（2026-05-15）。

現状: **v2.0.3 公開済み**（Modrinth `compass-to-map` / CurseForge `compass-to-map`）。
以下は更新リリース時に使う生きたチェックリスト。

---

## 公開前チェックリスト

### コード・ビルド

- [ ] `mod_version` を `gradle.properties` で更新（semver）
- [ ] 各サブプロジェクトで `./gradlew clean build`（NeoForge 1.21.1 = ルート、他は `<loader>-<mc>` の各ディレクトリ）
- [ ] 各 `build/libs/compasstomap-x.y.z.jar` のサイズ確認（localRuntime 混入なし）
- [ ] 各 jar に `LICENSE_compasstomap` が同梱されてること (`unzip -l <jar> | grep LICENSE`)
- [ ] `./gradlew runClient` で実機確認
  - [ ] Explorer's Compass で構造物検索 → 発見 → JM waypoint 自動登録
  - [ ] Nature's Compass でバイオーム検索 → 発見 → JM waypoint 自動登録（v2.0+）
  - [ ] チャット通知（OP = `/tp` クリック可、非 OP = 装飾なし）
  - [ ] 同一対象の再検索で dedupe（再登録されない）
  - [ ] カテゴリ別色分け
  - [ ] Fabric ビルドは JM 統合 disable・チャット通知のみ（既知制限どおり）

### ドキュメント

- [ ] `README.md` の機能 / 設定 / 互換 / インストール（jar 名 `compasstomap-x.y.z.jar`）が最新
- [ ] `LICENSE` 存在
- [ ] `_docs/ROADMAP.md` / `_docs/CHANGELOG.md` 更新
- [ ] バージョン番号が各所一致（`gradle.properties`・mods.toml/fabric.mod.json・README）

### メタデータ

- [ ] `displayURL` / `issueTrackerURL` が `github.com/KURONAMI333/compass-to-map`
- [ ] `description` が現状機能と合致
- [ ] `authors=KURONAMI` / `license=MIT`

---

## ストア事実（説明文・タグの基準）

| 項目 | 値 |
|---|---|
| Project name | `Compass to Map` |
| Slug | `compass-to-map` |
| Mod ID | `compasstomap` / package `com.kuronami.compasstomap` |
| Loaders × MC | NeoForge/Forge/Fabric × 1.21.1、Forge/Fabric × 1.20.1（**NeoForge 1.20.1 なし**） |
| Environment | Client + Server（JourneyMap は Client のみで可） |
| 依存 | **Explorer's Compass / Nature's Compass のうち最低 1 つ**（両方とも任意・少なくとも片方必要）/ JourneyMap（任意・推奨、Forge/NeoForge のみ）/ Fabric は Forge Config API Port |
| 既知制限 | Fabric ビルドは JM 統合 disable（Loom 1.14 未リリース）。チャット通知のみ |
| License | MIT |

> Description 本文は `README.md` をそのままコピペ（Modrinth は Markdown 可、
> CurseForge は BBCode/HTML 変換）。jar 実ファイル名は `compasstomap-<version>.jar`
> （ローダー/MC はストアのタグで区別、ファイル名に接尾辞は付かない）。

---

## 更新リリース手順

1. `gradle.properties` の `mod_version` を更新
2. `_docs/CHANGELOG.md` 更新
3. 全サブプロジェクト `./gradlew clean build`
4. 実機で動作確認
5. Modrinth: Versions タブから各ローダー/MC の jar をアップロード（タグ付与）
6. CurseForge: Files タブから同上
7. GitHub: `vX.Y.Z` タグ付きリリース作成
