package com.kuronami.compasstomap.compat.jm;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;

class WaypointRegistrarTest {
    static {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    private final List<Waypoint> own = new ArrayList<>();
    private final ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation("minecraft:overworld"));
    private boolean lookupFails;
    private boolean addFails;
    private int creates;
    private IClientAPI api;

    @BeforeEach
    void setup() {
        api = (IClientAPI) Proxy.newProxyInstance(IClientAPI.class.getClassLoader(),
                new Class<?>[] { IClientAPI.class }, (proxy, method, args) -> {
                    assertEquals("compasstomap", args[0]);
                    if (method.getName().equals("getWaypoints")) {
                        if (lookupFails) throw new IllegalStateException("lookup unavailable");
                        return own;
                    }
                    if (method.getName().equals("addWaypoint")) {
                        if (addFails) throw new IllegalStateException("add unavailable");
                        own.add((Waypoint) args[1]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        new WaypointFactory((WaypointFactory.WaypointStore) Proxy.newProxyInstance(
                WaypointFactory.WaypointStore.class.getClassLoader(),
                new Class<?>[] { WaypointFactory.WaypointStore.class }, (proxy, method, args) -> {
                    assertEquals("createWaypoint", method.getName());
                    assertEquals("compasstomap", args[0]);
                    assertEquals("minecraft:overworld", args[3]);
                    creates++;
                    int[] color = {0};
                    return Proxy.newProxyInstance(Waypoint.class.getClassLoader(),
                            new Class<?>[] { Waypoint.class }, (wp, call, values) -> switch (call.getName()) {
                                case "getName" -> args[2];
                                case "getBlockPos" -> args[1];
                                case "isPersistent" -> args[4];
                                case "getColor" -> color[0];
                                case "setColor" -> { color[0] = (int) values[0]; yield null; }
                                default -> throw new UnsupportedOperationException(call.getName());
                            });
                }));
    }

    private void show(String name, int x, int y, boolean biome) {
        WaypointRegistrar.show(api, name, new BlockPos(x, y, -20), dimension, biome, 0x228B22, true);
    }

    @Test void registersThroughActualFactory() {
        show("Village Plains", 10, 64, false);
        assertEquals(1, creates);
        assertEquals("Village Plains (10, -20)", own.get(0).getName());
        assertEquals(new BlockPos(10, 64, -20), own.get(0).getBlockPos());
        assertEquals(0x228B22, own.get(0).getColor());
        assertTrue(own.get(0).isPersistent());
    }

    @Test void repeatsIgnoreEstimatedHeight() {
        show("Village Plains", 10, 64, false);
        show("Village Plains", 10, 96, false);
        assertEquals(1, own.size());
    }

    @Test void differentStructuresRegister() {
        show("Village Plains", 10, 64, false);
        show("Village Plains", 100, 64, false);
        assertEquals(2, own.size());
    }

    @Test void biomesIgnoreCoordinatesButKeepNameBoundary() {
        show("Forest", 10, 64, true);
        show("Forest", 100, 96, true);
        show("Forest Hills", 100, 96, true);
        assertEquals(2, own.size());
        assertEquals("[Biome] Forest (10, -20)", own.get(0).getName());
    }

    @Test void uninitializedApiDoesNotCreate() {
        WaypointRegistrar.show(null, "Forest", BlockPos.ZERO, dimension, true, 0, true);
        assertEquals(0, creates);
    }

    @Test void lookupFailureStillRegisters() {
        lookupFails = true;
        show("Forest", 10, 64, true);
        assertEquals(1, own.size());
    }

    @Test void registrationFailureIsContainedByDispatch() {
        addFails = true;
        assertDoesNotThrow(() -> JourneyMapClientHook.dispatch(true, () -> show("Forest", 10, 64, true)));
        assertTrue(own.isEmpty());
    }

    @Test void absentJourneyMapSkipsDispatch() {
        JourneyMapClientHook.dispatch(false, () -> fail("Optional API must not be called"));
        assertEquals(0, creates);
    }

    @Test void linkageFailureIsContained() {
        assertDoesNotThrow(() -> JourneyMapClientHook.dispatch(true, () -> { throw new NoClassDefFoundError("optional API"); }));
    }
}
