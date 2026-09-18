package com.kuronami.compasstomap.compat.jm;

import com.kuronami.compasstomap.CompassToMapFabric;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class WaypointRegistrar {
    private WaypointRegistrar() {}

    public static void show(IClientAPI api, String prettyName, BlockPos pos,
                            ResourceKey<Level> dimension, boolean biome, int color, boolean persistent) {
        if (api == null) return;
        String prefix = (biome ? "[Biome] " : "") + prettyName + " (";
        String displayName = prefix + pos.getX() + ", " + pos.getZ() + ")";
        try {
            for (Waypoint own : api.getWaypoints(CompassToMapFabric.MODID)) {
                String name = own.getName();
                if (name != null && (biome ? name.startsWith(prefix) : name.equals(displayName))) return;
            }
        } catch (RuntimeException | LinkageError e) {
            CompassToMapFabric.LOGGER.warn("JourneyMap waypoint lookup failed, registering anyway: {}", e.toString());
        }
        Waypoint waypoint = WaypointFactory.createWaypoint(
                CompassToMapFabric.MODID, pos, displayName, dimension, persistent);
        waypoint.setColor(color);
        api.addWaypoint(CompassToMapFabric.MODID, waypoint);
        CompassToMapFabric.LOGGER.info("JourneyMap waypoint registered: {} @ {}", displayName, pos);
    }
}
