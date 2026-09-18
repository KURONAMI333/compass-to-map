package com.kuronami.compasstomap.compat.jm;

import java.util.List;
import java.util.function.Predicate;

import com.kuronami.compasstomap.CompassToMapFabric;
import journeymap.api.v2.client.IClientAPI;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class WaypointRegistrar {

    private WaypointRegistrar() {}

    /**
     * waypoint を登録する。照合は表示名で行う:
     * 構造物は表示名（座標込み）の完全一致、バイオームは座標を除いた前方一致
     * （"[Biome] " + 名前 + " (" まで）。理由は C2M_DECISIONS.md §1。
     */
    public static void show(IClientAPI api, String prettyName, BlockPos pos,
                            ResourceKey<Level> dimension, boolean biome, int color, boolean persistent) {
        if (api == null) {
            CompassToMapFabric.LOGGER.debug("JourneyMap API not yet initialized, skipping waypoint");
            return;
        }
        String displayName = biome
                ? "[Biome] " + prettyName + " (" + pos.getX() + ", " + pos.getZ() + ")"
                : prettyName + " (" + pos.getX() + ", " + pos.getZ() + ")";
        String prefix = "[Biome] " + prettyName + " (";
        if (alreadyRegistered(api, biome ? name -> name.startsWith(prefix) : displayName::equals)) return;
        registerWaypoint(api, pos, displayName, dimension, color, persistent, biome ? "biome" : "structure");
    }

    /** 既に C2M が立てた waypoint の中に {@code nameMatches} に当たるものがあるか。 */
    static boolean alreadyRegistered(IClientAPI api, Predicate<String> nameMatches) {
        try {
            List<? extends journeymap.api.v2.common.waypoint.Waypoint> own =
                    api.getWaypoints(CompassToMapFabric.MODID);
            for (journeymap.api.v2.common.waypoint.Waypoint wp : own) {
                String name = wp.getName();
                if (name != null && nameMatches.test(name)) return true;
            }
        } catch (Throwable t) {
            CompassToMapFabric.LOGGER.warn("JourneyMap waypoint lookup failed, registering anyway: {}", t.toString());
        }
        return false;
    }

    static void registerWaypoint(IClientAPI api, BlockPos pos, String displayName,
                                 ResourceKey<Level> dimension, int color, boolean persistent, String kind) {
        journeymap.api.v2.common.waypoint.Waypoint wp =
                journeymap.api.v2.common.waypoint.WaypointFactory.createClientWaypoint(
                        CompassToMapFabric.MODID, pos, displayName, dimension, persistent);
        wp.setColor(color);
        api.addWaypoint(CompassToMapFabric.MODID, wp);
        CompassToMapFabric.LOGGER.info("JourneyMap {} waypoint registered: {} @ {} (color=0x{})",
                kind, displayName, pos, Integer.toHexString(color));
    }
}
