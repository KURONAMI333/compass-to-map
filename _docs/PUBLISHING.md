# Publishing Checklist — Compass to Map (C2M)

Modrinth / CurseForge への公開・更新手順。

> 旧版は初回 v1.0.0・NeoForge/1.21.1 専用・Explorer's Compass 必須 を前提に
> していたが、現状と乖離していたため現実に合わせて改訂（2026-05-15）。

準備中の版: **v2.1.1**（既存13セル）。配布先での公開状態は公開直前に直接確認する。
以下は更新リリース時に使う生きたチェックリスト。

---

## 公開前チェックリスト

### コード・ビルド

- [ ] `mod_version` を `gradle.properties` で更新（semver）
- [ ] 各サブプロジェクトで `./gradlew clean build --no-build-cache`（NeoForge 1.21.1 = ルート、他は `<loader>-<mc>` の各ディレクトリ）
- [ ] 各 `build/libs/compass-to-map-journeymap-<modversion>+<loader>-<minecraft>.jar` のサイズ確認（localRuntime 混入なし）
- [ ] 各 jar に `LICENSE_compasstomap` が同梱されてること (`unzip -l <jar> | grep LICENSE`)
- [ ] 所有者付きの既存runclientラッパー で実機確認
  - [ ] Explorer's Compass で構造物検索 → 発見 → JM waypoint 自動登録
  - [ ] Nature's Compass でバイオーム検索 → 発見 → JM waypoint 自動登録（v2.0+）
  - [ ] チャット通知（OP = `/tp` クリック可、非 OP = 装飾なし）
  - [ ] 同一対象の再検索で dedupe（再登録されない）
  - [ ] カテゴリ別色分け
  - [ ] Fabricでも対応JourneyMapを入れて検索結果がwaypointへ登録されること

### ドキュメント

- [ ] `README.md` の機能 / 設定 / 互換 / インストール（jar 名 `compass-to-map-journeymap-<modversion>+<loader>-<minecraft>.jar`）が最新
- [ ] `LICENSE` 存在
- [ ] `_docs/ROADMAP.md` / `_docs/CHANGELOG.md` 更新
- [ ] バージョン番号が各所一致（`gradle.properties`・mods.toml/fabric.mod.json・README）

### メタデータ

- [ ] Sourceリンクを確認し、不具合窓口はCurseForgeコメントまたはXの @kuronami333 のDMとする
- [ ] `description` が現状機能と合致
- [ ] `authors=KURONAMI` / `license=All Rights Reserved`

---

## ストア事実（説明文・タグの基準）

| 項目 | 値 |
|---|---|
| Project name | `Compass to Map` |
| Slug | `compass-to-map` |
| Mod ID | `compasstomap` / package `com.kuronami.compasstomap` |
| Loaders × MC | neoforge: 1.21.1, 1.21.11, 1.21.4, 26.1.2, 26.2 / forge: 1.20.1, 1.21.1 / fabric: 1.20.1, 1.21.1, 1.21.11, 1.21.4, 26.1.2, 26.2 |
| Environment | Client + Server（JourneyMap は Client のみで可） |
| 依存 | **Explorer's Compass / Nature's Compass のうち最低 1 つ**（両方とも任意・少なくとも片方必要）/ JourneyMap（任意・waypoint連携に必要、Fabricも対応）/ Fabric は Forge Config API Port |
| 既知制限 | Forge 1.20.1はJourneyMap 5.10.5のv1 API。Fabric 1.20.1と新世代はJourneyMap 6.x。FabricでもJM連携を実装済み。Forge 1.20.1のJM6や全host patchの組合せは未確認。 |
| License | All Rights Reserved |

> Description 本文は `README.md` をそのままコピペ（Modrinth は Markdown 可、
> CurseForge は BBCode/HTML 変換）。jar 実ファイル名は `compass-to-map-journeymap-<modversion>+<loader>-<minecraft>.jar`
> （Minecraft・loader・MOD版をファイル名とストアのタグで区別）。

---

## 更新リリース手順

1. `gradle.properties` の `mod_version` を更新
2. `_docs/CHANGELOG.md` 更新
3. 全サブプロジェクト `./gradlew clean build --no-build-cache`
4. 実機で動作確認
5. Modrinth: Versions タブから各ローダー/MC の jar をアップロード（タグ付与）
6. CurseForge: Files タブから同上
7. GitHub: `vX.Y.Z` タグ付きリリース作成

この版は正式ビルド・JAR検査・実ホストAPI照合まで実施済み。実ゲーム起動・GUI・全host patchの動作成功は今回未確認。JourneyMap版とXaero版のJARは同じ命名形式を使用し、MOD ID・namespace・版番号は変更しない。
