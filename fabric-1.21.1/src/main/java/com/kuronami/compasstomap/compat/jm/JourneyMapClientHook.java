package com.kuronami.compasstomap.compat.jm;

import com.kuronami.compasstomap.CompassToMapFabric;
import com.kuronami.compasstomap.Config;
import com.kuronami.compasstomap.network.BiomeFoundPayload;
import com.kuronami.compasstomap.network.StructureFoundPayload;

import net.minecraft.resources.ResourceLocation;

/**
 * Fabric v2.0: JM 統合は無効化 (intermediary mapping × Loom 1.14 制約のため)。
 *
 * 動作：
 *  - chat 通知 (server side で発行) は通常通り動く
 *  - JM waypoint 自動登録 は v2.0 Fabric では未対応
 *
 * v2.1 計画: reflection ベースの JM hook で intermediary mapping bypass を実現。
 *
 * このクラスは prettifyResourceName / colorByCategory などのヘルパーを保持。
 * Server 側 CompassWatcher のチャット通知 prettifyResourceName 呼び出しに使用。
 */
public final class JourneyMapClientHook {

    /** ブランド色 (紫) - カテゴリ別色が無効な場合に使用 */
    private static final int BRAND_COLOR = 0x8B5CF6;

    private JourneyMapClientHook() {}

    /** v2.0 Fabric では常に false (JM 連携無し)。 */
    public static boolean isJourneyMapLoaded() {
        return false;
    }

    public static void onStructureFound(StructureFoundPayload payload) {
        // No-op on Fabric v2.0. server-side chat 通知が通知役を担う。
        CompassToMapFabric.LOGGER.debug(
                "Structure waypoint received but JM integration disabled on Fabric v2.0: {}", payload.structureId());
    }

    public static void onBiomeFound(BiomeFoundPayload payload) {
        CompassToMapFabric.LOGGER.debug(
                "Biome waypoint received but JM integration disabled on Fabric v2.0: {}", payload.biomeId());
    }

    /**
     * ResourceLocation 文字列から見やすい表示名を生成。
     * NF/Forge と同一実装。
     */
    public static String prettifyResourceName(String resourceId) {
        try {
            ResourceLocation rl = ResourceLocation.parse(resourceId);
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

    /**
     * 構造物カテゴリ別色 (将来 v2.1 で reflection 経由 JM 登録時に使用)。
     */
    public static int colorByCategory(String structureId) {
        if (!Config.COLOR_BY_CATEGORY.get()) return BRAND_COLOR;
        String lower = structureId.toLowerCase();
        if (lower.contains("village")) return 0xFFD700;
        if (lower.contains("mineshaft") || lower.contains("dungeon")) return 0xCC3333;
        if (lower.contains("stronghold") || lower.contains("end_city")) return 0x9B59B6;
        if (lower.contains("ocean_monument") || lower.contains("temple")
                || lower.contains("pyramid") || lower.contains("swamp_hut")
                || lower.contains("igloo")) return 0x00CED1;
        if (lower.contains("fortress") || lower.contains("bastion")) return 0xFF8C00;
        if (lower.contains("ruined_portal")) return 0x808080;
        if (lower.contains("woodland_mansion")) return 0x8B4513;
        if (lower.contains("ancient_city")) return 0x00FFFF;
        if (lower.contains("trial_chambers")) return 0x32CD32;
        if (lower.contains("shipwreck") || lower.contains("buried_treasure")) return 0xDAA520;
        try {
            ResourceLocation rl = ResourceLocation.parse(structureId);
            if (!"minecraft".equals(rl.getNamespace())) {
                int hue = Math.floorMod(structureId.hashCode(), 360);
                return hslToRgb(hue, 0.7f, 0.55f);
            }
        } catch (Throwable t) { /* fall through */ }
        return 0xFFFFFF;
    }

    public static int colorByBiome(String biomeId) {
        if (!Config.COLOR_BY_CATEGORY.get()) return BRAND_COLOR;
        String lower = biomeId.toLowerCase();
        if (lower.contains("cherry")) return 0xFFB6C1;
        if (lower.contains("crimson") || lower.contains("warped")
                || lower.contains("nether") || lower.contains("basalt")
                || lower.contains("soul_sand")) return 0xCC3333;
        if (lower.contains("deep_dark") || lower.contains("dripstone")
                || lower.contains("lush") || lower.contains("cave")) return 0x8B4513;
        if (lower.contains("mushroom")) return 0xFF69B4;
        if (lower.contains("desert") || lower.contains("badlands") || lower.contains("mesa")) return 0xF5DEB3;
        if (lower.contains("jungle")) return 0x2D5016;
        if (lower.contains("forest") || lower.contains("taiga") || lower.contains("birch")
                || lower.contains("grove") || lower.contains("woodland")) return 0x228B22;
        if (lower.contains("ocean") || lower.contains("river")) return 0x1E90FF;
        if (lower.contains("snow") || lower.contains("frozen") || lower.contains("ice")) return 0xF0FFFF;
        if (lower.contains("mountain") || lower.contains("peak") || lower.contains("hill")
                || lower.contains("slope") || lower.contains("meadow")) return 0xA9A9A9;
        if (lower.contains("end") || lower.contains("void")) return 0x9B59B6;
        if (lower.contains("plain") || lower.contains("savanna")) return 0x9ACD32;
        if (lower.contains("beach") || lower.contains("shore")) return 0xFFE4B5;
        if (lower.contains("swamp") || lower.contains("mangrove")) return 0x556B2F;
        try {
            ResourceLocation rl = ResourceLocation.parse(biomeId);
            if (!"minecraft".equals(rl.getNamespace())) {
                int hue = Math.floorMod(biomeId.hashCode(), 360);
                return hslToRgb(hue, 0.55f, 0.6f);
            }
        } catch (Throwable t) { /* fall through */ }
        return 0xFFFFFF;
    }

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

    private static int clamp255(int v) { return Math.max(0, Math.min(255, v)); }
}
