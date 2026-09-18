package com.kuronami.compasstomap.compat.jm;

import com.kuronami.compasstomap.CompassToMapFabric;
import com.kuronami.compasstomap.Config;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class JourneyMapClientHook {

    private static final int BRAND_COLOR = 0x8B5CF6;

    private JourneyMapClientHook() {}

    public static boolean isJourneyMapLoaded() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("journeymap");
    }

    public static void show(String id, BlockPos pos, ResourceLocation dimension, boolean biome) {
        dispatch(isJourneyMapLoaded(), () -> Inner.show(id, pos, dimension, biome));
    }

    static void dispatch(boolean loaded, Runnable show) {
        if (!loaded) return;
        try {
            show.run();
        } catch (RuntimeException | LinkageError e) {
            CompassToMapFabric.LOGGER.warn("JourneyMap waypoint registration failed: {}", e.toString());
        }
    }

    private static final class Inner {
        static void show(String id, BlockPos pos, ResourceLocation dimension, boolean biome) {
            ResourceKey<Level> dim = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimension);
            WaypointRegistrar.show(CompassToMapJourneyMapPlugin.api, prettifyResourceName(id), pos,
                    dim, biome, color(id, biome), Config.PERSISTENT_WAYPOINTS.get());
        }
    }

    static int color(String id, boolean biome) {
        if (!Config.COLOR_BY_CATEGORY.get()) return BRAND_COLOR;
        String lower = id.toLowerCase(java.util.Locale.ROOT);
        String[][] categories = biome ? new String[][] {
                {"cherry"}, {"crimson", "warped", "nether", "basalt", "soul_sand"},
                {"deep_dark", "dripstone", "lush", "cave"}, {"mushroom"},
                {"desert", "badlands", "mesa"}, {"jungle"},
                {"forest", "taiga", "birch", "grove", "woodland"}, {"ocean", "river"},
                {"snow", "frozen", "ice"}, {"mountain", "peak", "hill", "slope", "meadow"},
                {"end", "void"}, {"plain", "savanna"}, {"beach", "shore"}, {"swamp", "mangrove"}
        } : new String[][] {
                {"village"}, {"mineshaft", "dungeon"}, {"stronghold", "end_city"},
                {"ocean_monument", "temple", "pyramid", "swamp_hut", "igloo"},
                {"fortress", "bastion"}, {"ruined_portal"}, {"woodland_mansion"},
                {"ancient_city"}, {"trial_chambers"}, {"shipwreck", "buried_treasure"}
        };
        int[] colors = biome
                ? new int[] {0xFFB6C1, 0xCC3333, 0x8B4513, 0xFF69B4, 0xF5DEB3, 0x2D5016,
                    0x228B22, 0x1E90FF, 0xF0FFFF, 0xA9A9A9, 0x9B59B6, 0x9ACD32, 0xFFE4B5, 0x556B2F}
                : new int[] {0xFFD700, 0xCC3333, 0x9B59B6, 0x00CED1, 0xFF8C00, 0x808080,
                    0x8B4513, 0x00FFFF, 0x32CD32, 0xDAA520};
        for (int i = 0; i < categories.length; i++) {
            for (String keyword : categories[i]) if (lower.contains(keyword)) return colors[i];
        }
        ResourceLocation resource = ResourceLocation.tryParse(id);
        if (resource == null || resource.getNamespace().equals("minecraft")) return 0xFFFFFF;
        float h = Math.floorMod(id.hashCode(), 360);
        float s = biome ? 0.55f : 0.7f;
        float l = biome ? 0.6f : 0.55f;
        float c = (1f - Math.abs(2f * l - 1f)) * s;
        float x = c * (1f - Math.abs((h / 60f) % 2f - 1f));
        float m = l - c / 2f;
        float[] rgb = h < 60 ? new float[] {c, x, 0} : h < 120 ? new float[] {x, c, 0}
                : h < 180 ? new float[] {0, c, x} : h < 240 ? new float[] {0, x, c}
                : h < 300 ? new float[] {x, 0, c} : new float[] {c, 0, x};
        return (Math.round((rgb[0] + m) * 255) << 16)
                | (Math.round((rgb[1] + m) * 255) << 8) | Math.round((rgb[2] + m) * 255);
    }

    static String prettifyResourceName(String resourceId) {
        try {
            ResourceLocation rl = new ResourceLocation(resourceId);
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
}
