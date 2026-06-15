package com.kuronami.compasstomap.compat.jm;

import com.kuronami.compasstomap.CompassToMapFabric;
import com.kuronami.compasstomap.Config;
import com.kuronami.compasstomap.network.BiomeFoundPayload;
import com.kuronami.compasstomap.network.StructureFoundPayload;

import net.minecraft.resources.Identifier;

/**
 * クライアント側で payload を受信して JourneyMap に waypoint 登録する。
 *
 * 設計:
 *  - JM 不在時に NoClassDefFoundError を起こさないため Inner class で API 参照を分離
 *  - 構造物・バイオーム両方に対応 (v2.0+)
 *  - カテゴリ別色分け (Config.COLOR_BY_CATEGORY)
 */
public final class JourneyMapClientHook {

    /** ブランド色 (紫) - カテゴリ別色が無効な場合に使用 */
    private static final int BRAND_COLOR = 0x8B5CF6;

    private JourneyMapClientHook() {}

    public static boolean isJourneyMapLoaded() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("journeymap");
    }

    public static void onStructureFound(StructureFoundPayload payload) {
        if (!isJourneyMapLoaded()) return;
        try {
            Inner.showStructure(payload);
        } catch (Throwable t) {
            CompassToMapFabric.LOGGER.warn("JourneyMap structure waypoint show failed: {}", t.toString());
        }
    }

    public static void onBiomeFound(BiomeFoundPayload payload) {
        if (!isJourneyMapLoaded()) return;
        try {
            Inner.showBiome(payload);
        } catch (Throwable t) {
            CompassToMapFabric.LOGGER.warn("JourneyMap biome waypoint show failed: {}", t.toString());
        }
    }

    /**
     * Identifier 文字列から見やすい表示名を生成。
     * snake_case → Title Case (例: minecraft:village_plains → Village Plains)
     * 構造物・バイオーム両方で使用。
     */
    public static String prettifyResourceName(String resourceId) {
        try {
            Identifier rl = Identifier.parse(resourceId);
            String path = rl.getPath();
            String[] parts = path.split("_");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (part.isEmpty()) continue;
                if (sb.length() > 0) sb.append(" ");
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
            return sb.toString();
        } catch (Throwable t) {
            return resourceId;
        }
    }

    /** 旧 v1.0 API との互換のため残す (内部呼び出しは prettifyResourceName 推奨)。 */
    @Deprecated
    public static String prettifyStructureName(String structureId) {
        return prettifyResourceName(structureId);
    }

    /**
     * 構造物カテゴリ別色を返す。Config.COLOR_BY_CATEGORY=false なら BRAND_COLOR 単色。
     *
     * 判定順:
     *  1. キーワード判定 (vanilla / MOD 問わずカテゴリキーワード入りはここでヒット)
     *  2. namespace が minecraft 以外で未ヒット → ID から hash 色生成
     *  3. それ以外 (バニラの未分類構造物) → 白
     */
    public static int colorByCategory(String structureId) {
        if (!Config.COLOR_BY_CATEGORY.get()) return BRAND_COLOR;

        String lower = structureId.toLowerCase();
        if (lower.contains("village")) return 0xFFD700;             // 黄
        if (lower.contains("mineshaft") || lower.contains("dungeon")) return 0xCC3333; // 赤
        if (lower.contains("stronghold") || lower.contains("end_city")) return 0x9B59B6; // 紫
        if (lower.contains("ocean_monument")
                || lower.contains("temple")
                || lower.contains("pyramid")
                || lower.contains("swamp_hut")
                || lower.contains("igloo")) return 0x00CED1;        // シアン (テンプル系)
        if (lower.contains("fortress") || lower.contains("bastion")) return 0xFF8C00;   // オレンジ
        if (lower.contains("ruined_portal")) return 0x808080;       // グレー
        if (lower.contains("woodland_mansion")) return 0x8B4513;    // 茶
        if (lower.contains("ancient_city")) return 0x00FFFF;        // 水色
        if (lower.contains("trial_chambers")) return 0x32CD32;      // ライム
        if (lower.contains("shipwreck") || lower.contains("buried_treasure")) return 0xDAA520; // 山吹色 (海賊系)

        // MOD 構造物 → hash 色
        try {
            Identifier rl = Identifier.parse(structureId);
            if (!"minecraft".equals(rl.getNamespace())) {
                int hue = Math.floorMod(structureId.hashCode(), 360);
                return hslToRgb(hue, 0.7f, 0.55f);
            }
        } catch (Throwable t) { /* fall through */ }

        return 0xFFFFFF; // 白
    }

    /**
     * バイオームカテゴリ別色を返す (v2.0+)。Config.COLOR_BY_CATEGORY=false なら BRAND_COLOR 単色。
     *
     * 判定順:
     *  1. キーワード判定 (バイオーム種別ベース、vanilla / MOD 共通)
     *     例: minecraft:desert / mymod:hot_desert → 黄系
     *  2. namespace が minecraft 以外で未ヒット → hash 色 (構造物より落ち着いた彩度で区別)
     *  3. それ以外 → 白
     */
    public static int colorByBiome(String biomeId) {
        if (!Config.COLOR_BY_CATEGORY.get()) return BRAND_COLOR;

        String lower = biomeId.toLowerCase();

        // ⚠️ 判定順序重要: 個別キーワード (cherry / crimson / warped / deep_dark) を先に判定して、
        // 後続の汎用キーワード (forest / grove / deep) に飲まれないようにする。
        // 例: cherry_grove は "grove" より "cherry" を優先、crimson_forest は "forest" より "crimson" を優先。

        // ① 個別バイオーム判定 (汎用キーワードと衝突するもの)
        if (lower.contains("cherry"))
            return 0xFFB6C1; // light pink (cherry_grove)
        if (lower.contains("crimson") || lower.contains("warped")
                || lower.contains("nether") || lower.contains("basalt")
                || lower.contains("soul_sand"))
            return 0xCC3333; // 赤 (ネザー系: crimson_forest, warped_forest 等)
        if (lower.contains("deep_dark") || lower.contains("dripstone")
                || lower.contains("lush") || lower.contains("cave"))
            return 0x8B4513; // 茶 (洞窟系: deep_dark, dripstone_caves 等)
        if (lower.contains("mushroom"))
            return 0xFF69B4; // pink (mushroom_fields)

        // ② 汎用カテゴリ判定
        // 砂漠・荒野系 (暑い)
        if (lower.contains("desert") || lower.contains("badlands") || lower.contains("mesa"))
            return 0xF5DEB3; // wheat
        // ジャングル
        if (lower.contains("jungle"))
            return 0x2D5016; // 暗緑
        // 森林・タイガ系
        if (lower.contains("forest") || lower.contains("taiga") || lower.contains("birch")
                || lower.contains("grove") || lower.contains("woodland"))
            return 0x228B22; // forest green
        // 海・川
        if (lower.contains("ocean") || lower.contains("river"))
            return 0x1E90FF; // dodger blue
        // 雪・氷
        if (lower.contains("snow") || lower.contains("frozen") || lower.contains("ice"))
            return 0xF0FFFF; // azure
        // 山岳系
        if (lower.contains("mountain") || lower.contains("peak") || lower.contains("hill")
                || lower.contains("slope") || lower.contains("meadow"))
            return 0xA9A9A9; // dark gray
        // エンド系
        if (lower.contains("end") || lower.contains("void"))
            return 0x9B59B6; // 紫
        // 平原・サバンナ系
        if (lower.contains("plain") || lower.contains("savanna"))
            return 0x9ACD32; // yellow green
        // 海岸・浜
        if (lower.contains("beach") || lower.contains("shore"))
            return 0xFFE4B5; // moccasin
        // 沼・マングローブ
        if (lower.contains("swamp") || lower.contains("mangrove"))
            return 0x556B2F; // dark olive

        // MOD バイオーム → hash 色 (構造物との視認区別のため saturation 低め)
        try {
            Identifier rl = Identifier.parse(biomeId);
            if (!"minecraft".equals(rl.getNamespace())) {
                int hue = Math.floorMod(biomeId.hashCode(), 360);
                return hslToRgb(hue, 0.55f, 0.6f); // 構造物 (S=0.7, L=0.55) よりパステル寄り
            }
        } catch (Throwable t) { /* fall through */ }

        return 0xFFFFFF; // 白
    }

    /**
     * HSL → RGB 変換。saturation/lightness 固定で揃えると鮮明で見やすい色になる。
     */
    private static int hslToRgb(float h, float s, float l) {
        float c = (1f - Math.abs(2f * l - 1f)) * s;
        float x = c * (1f - Math.abs((h / 60f) % 2f - 1f));
        float m = l - c / 2f;

        float r, g, b;
        if (h < 60)       { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else              { r = c; g = 0; b = x; }

        int ri = clamp255(Math.round((r + m) * 255));
        int gi = clamp255(Math.round((g + m) * 255));
        int bi = clamp255(Math.round((b + m) * 255));
        return (ri << 16) | (gi << 8) | bi;
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }

    /**
     * JM API への参照を Inner class に閉じ込める isolation パターン。
     * JM 不在時に NoClassDefFoundError を起こさない。
     */
    private static final class Inner {
        static void showStructure(StructureFoundPayload payload) {
            journeymap.api.v2.client.IClientAPI api = CompassToMapJourneyMapPlugin.api;
            if (api == null) {
                CompassToMapFabric.LOGGER.debug("JourneyMap API not yet initialized, skipping structure waypoint");
                return;
            }
            String prettyName = prettifyResourceName(payload.structureId());
            String displayName = prettyName + " ("
                    + payload.pos().getX() + ", " + payload.pos().getZ() + ")";
            int color = colorByCategory(payload.structureId());
            registerWaypoint(api, payload.pos(), displayName, payload.dimension(), color, "structure");
        }

        static void showBiome(BiomeFoundPayload payload) {
            journeymap.api.v2.client.IClientAPI api = CompassToMapJourneyMapPlugin.api;
            if (api == null) {
                CompassToMapFabric.LOGGER.debug("JourneyMap API not yet initialized, skipping biome waypoint");
                return;
            }
            String prettyName = prettifyResourceName(payload.biomeId());
            // バイオーム waypoint は "[Biome] Desert" 形式で識別性を上げる
            String displayName = "[Biome] " + prettyName + " ("
                    + payload.pos().getX() + ", " + payload.pos().getZ() + ")";
            int color = colorByBiome(payload.biomeId());
            registerWaypoint(api, payload.pos(), displayName, payload.dimension(), color, "biome");
        }

        private static void registerWaypoint(journeymap.api.v2.client.IClientAPI api,
                                              net.minecraft.core.BlockPos pos,
                                              String displayName,
                                              net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim,
                                              int color,
                                              String kind) {
            journeymap.api.v2.common.waypoint.Waypoint wp =
                    journeymap.api.v2.common.waypoint.WaypointFactory.createClientWaypoint(
                            CompassToMapFabric.MODID,
                            pos,
                            displayName,
                            dim,
                            Config.PERSISTENT_WAYPOINTS.get()
                    );
            wp.setColor(color);
            api.addWaypoint(CompassToMapFabric.MODID, wp);
            CompassToMapFabric.LOGGER.info("JourneyMap {} waypoint registered: {} @ {} (color=0x{})",
                    kind, displayName, pos, Integer.toHexString(color));
        }
    }
}
