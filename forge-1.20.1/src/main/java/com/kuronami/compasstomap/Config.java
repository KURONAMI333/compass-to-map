package com.kuronami.compasstomap;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Compass to Map の COMMON 設定 (Forge 1.21.1)。
 * NeoForge 版の {@code ModConfigSpec} を {@code ForgeConfigSpec} に置換したのみ。
 */
public final class Config {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLED = B
            .comment("Master switch. If false, no waypoints are auto-registered.")
            .define("feature.enabled", true);

    public static final ForgeConfigSpec.BooleanValue ENABLE_STRUCTURE = B
            .comment("If true, register Explorer's Compass structure discoveries as waypoints.")
            .define("feature.enableStructure", true);

    public static final ForgeConfigSpec.BooleanValue ENABLE_BIOME = B
            .comment("If true, register Nature's Compass biome discoveries as waypoints (v2.0+).")
            .define("feature.enableBiome", true);

    public static final ForgeConfigSpec.BooleanValue NOTIFY_ON_FOUND = B
            .comment("Send a chat message to the player when a structure or biome is found and registered.")
            .define("notification.notifyOnFound", true);

    public static final ForgeConfigSpec.BooleanValue COLOR_BY_CATEGORY = B
            .comment(
                    "If true, waypoint color depends on category:",
                    "  Structures: village=yellow, mineshaft/dungeon=red, stronghold/end_city=purple,",
                    "    ocean_monument/temple=cyan, fortress/bastion=orange, others=white.",
                    "  Biomes (v2.0+): desert/badlands=wheat, jungle=dark green, forest/taiga=forest green,",
                    "    ocean/river=blue, snow/frozen=azure, mountain/peak=gray,",
                    "    nether/crimson/warped=red, end/void=purple, plain/savanna=yellow green,",
                    "    swamp/mangrove=dark olive, mushroom=pink, cherry=light pink, cave=brown.",
                    "  MOD-added structures/biomes: hash-generated unique color from ID.",
                    "If false, all Compass-to-Map waypoints use the brand color (purple)."
            )
            .define("appearance.colorByCategory", true);

    public static final ForgeConfigSpec.BooleanValue PERSISTENT_WAYPOINTS = B
            .comment("If true, waypoints persist across sessions (saved by JourneyMap).")
            .define("appearance.persistentWaypoints", true);

    static final ForgeConfigSpec SPEC = B.build();

    private Config() {}
}
