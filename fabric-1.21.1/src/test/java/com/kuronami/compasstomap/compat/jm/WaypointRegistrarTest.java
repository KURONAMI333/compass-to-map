package com.kuronami.compasstomap.compat.jm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kuronami.compasstomap.CompassToMapFabric;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;

/**
 * WaypointRegistrar の実際の登録経路 (WaypointFactory 経由) を固定する。
 *
 * <p>JourneyMap 本体の描画・保存は別 (実機確認)。ここで見るのは:
 *  - api==null (JM 未初期化) で登録しない
 *  - 構造物の完全一致照合・バイオームの前方一致照合で重複登録しない
 *  - factory に渡る modid / 表示名 / 次元 / persistent
 */
class WaypointRegistrarTest {

    @org.junit.jupiter.api.BeforeEach
    void initializeFactory() {
        new WaypointFactory((WaypointFactory.WaypointStore) java.lang.reflect.Proxy.newProxyInstance(
                WaypointFactory.WaypointStore.class.getClassLoader(),
                new Class<?>[] { WaypointFactory.WaypointStore.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("createClientWaypoint")) {
                        return new TestWaypoint((String) args[0], (BlockPos) args[1],
                                (String) args[2], (String) args[3], (boolean) args[4]);
                    }
                    throw new UnsupportedOperationException(method.getName());
                }));
    }

    private static final ResourceKey<Level> DIM = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"));

    /** IClientAPI の必要部分だけを持つスタブ。 */
    private static final class StubApi implements journeymap.api.v2.client.IClientAPI {
        final List<Waypoint> added = new ArrayList<>();
        final List<Waypoint> own;

        StubApi(Collection<Waypoint> own) { this.own = new ArrayList<>(own); }

        @Override
        public java.util.List<? extends Waypoint> getWaypoints(String modId) {
            return CompassToMapFabric.MODID.equals(modId) ? own : List.of();
        }

        @Override
        public void addWaypoint(String modId, Waypoint wp) {
            if (!CompassToMapFabric.MODID.equals(modId)) {
                throw new IllegalArgumentException("unexpected modId: " + modId);
            }
            added.add(wp);
            own.add(wp);
        }

        // 以下はこのテストが使わない IClientAPI の面。NoClassDefFoundError を避けるため
        // RuntimeException を投げる実装にする (呼ばれたら落ちる＝呼ばれていない証拠)。
        @Override public void show(journeymap.api.v2.client.display.Displayable d) { throw new UnsupportedOperationException(); }
        @Override public void remove(journeymap.api.v2.client.display.Displayable d) { throw new UnsupportedOperationException(); }
        @Override public void removeAll(String s, journeymap.api.v2.client.display.DisplayType t) { throw new UnsupportedOperationException(); }
        @Override public void removeAll(String s) { throw new UnsupportedOperationException(); }
        @Override public boolean exists(journeymap.api.v2.client.display.Displayable d) { throw new UnsupportedOperationException(); }
        @Override public boolean playerAccepts(String s, journeymap.api.v2.client.display.DisplayType t) { throw new UnsupportedOperationException(); }
        @Override public journeymap.api.v2.client.util.UIState getUIState(journeymap.api.v2.client.display.Context.UI u) { throw new UnsupportedOperationException(); }
        @Override public void requestMapTile(String s, ResourceKey<Level> k, journeymap.api.v2.client.display.Context.MapType m, net.minecraft.world.level.ChunkPos a, net.minecraft.world.level.ChunkPos b, Integer i, int j, boolean fl, java.util.function.Consumer<com.mojang.blaze3d.platform.NativeImage> c) { throw new UnsupportedOperationException(); }
        @Override public void disableFeature(ResourceKey<Level> k, journeymap.api.v2.client.display.Context.MapType m, boolean b) { throw new UnsupportedOperationException(); }
        @Override public java.io.File getDataPath(String s) { throw new UnsupportedOperationException(); }
        @Override public java.util.List<? extends Waypoint> getAllWaypoints() { throw new UnsupportedOperationException(); }
        @Override public java.util.List<? extends Waypoint> getAllWaypoints(ResourceKey<Level> k) { throw new UnsupportedOperationException(); }
        @Override public Waypoint getWaypoint(String s1, String s2) { throw new UnsupportedOperationException(); }
        @Override public void removeWaypoint(String s, Waypoint w) { throw new UnsupportedOperationException(); }
        @Override public void removeAllWaypoints(String s) { throw new UnsupportedOperationException(); }
        @Override public void addWaypointGroup(journeymap.api.v2.common.waypoint.WaypointGroup g) { throw new UnsupportedOperationException(); }
        @Override public journeymap.api.v2.common.waypoint.WaypointGroup getWaypointGroup(String s) { throw new UnsupportedOperationException(); }
        @Override public journeymap.api.v2.common.waypoint.WaypointGroup getWaypointGroupByName(String s1, String s2) { throw new UnsupportedOperationException(); }
        @Override public java.util.List<? extends journeymap.api.v2.common.waypoint.WaypointGroup> getWaypointGroups(String s) { throw new UnsupportedOperationException(); }
        @Override public java.util.List<? extends journeymap.api.v2.common.waypoint.WaypointGroup> getAllWaypointGroups() { throw new UnsupportedOperationException(); }
        @Override public void removeWaypointGroup(journeymap.api.v2.common.waypoint.WaypointGroup g, boolean b) { throw new UnsupportedOperationException(); }
        @Override public void removeWaypointGroups(String s, boolean b) { throw new UnsupportedOperationException(); }
        @Override public String getWorldId() { throw new UnsupportedOperationException(); }
        @Override public void toggleMinimap(boolean b) { throw new UnsupportedOperationException(); }
        @Override public boolean minimapEnabled() { throw new UnsupportedOperationException(); }
    }

    /** WaypointFactory に差し込む最小 store。実 JM 本体に触れない。 */
    private static final class TestWaypoint implements Waypoint {
        final String modId;
        final BlockPos pos;
        final String name;
        final String dimension;
        final boolean persistent;
        int color = 0xFFFFFF;

        TestWaypoint(String modId, BlockPos pos, String name, String dimension, boolean persistent) {
            this.modId = modId;
            this.pos = pos;
            this.name = name;
            this.dimension = dimension;
            this.persistent = persistent;
            this.color = 0;
        }

        @Override public String getModId() { return modId; }
        @Override public String getName() { return name; }
        @Override public void setName(String n) { }
        @Override public BlockPos getBlockPos() { return pos; }
        @Override public int getX() { return pos.getX(); }
        @Override public int getY() { return pos.getY(); }
        @Override public int getZ() { return pos.getZ(); }
        @Override public void setX(int x) { }
        @Override public void setY(int y) { }
        @Override public void setZ(int z) { }
        @Override public int getColor() { return color; }
        @Override public void setColor(int c) { this.color = c; }
        @Override public boolean isPersistent() { return persistent; }
        @Override public void setPersistent(boolean p) { }
        @Override public String getPrimaryDimension() { return DIM.location().toString(); }
        @Override public void setPrimaryDimension(String d) { }
        // 残りの面はこのテストが触らない
        @Override public String getId() { throw new UnsupportedOperationException(); }
        @Override public String getGuid() { throw new UnsupportedOperationException(); }
        @Override public String getGroupId() { throw new UnsupportedOperationException(); }
        @Override public String getDescription() { throw new UnsupportedOperationException(); }
        @Override public void setDescription(String d) { throw new UnsupportedOperationException(); }
        @Override public void setPos(int x, int y, int z) { throw new UnsupportedOperationException(); }
        @Override public void setBlockPos(BlockPos p) { throw new UnsupportedOperationException(); }
        @Override public int getRed() { throw new UnsupportedOperationException(); }
        @Override public void setRed(int r) { throw new UnsupportedOperationException(); }
        @Override public int getGreen() { throw new UnsupportedOperationException(); }
        @Override public void setGreen(int g) { throw new UnsupportedOperationException(); }
        @Override public int getBlue() { throw new UnsupportedOperationException(); }
        @Override public void setBlue(int b) { throw new UnsupportedOperationException(); }
        @Override public TreeSet<String> getDimensions() { throw new UnsupportedOperationException(); }
        @Override public void setDimensions(Collection<String> d) { throw new UnsupportedOperationException(); }
        @Override public void setPrimaryDimension(ResourceKey<Level> d) { throw new UnsupportedOperationException(); }
        @Override public boolean isEnabled() { throw new UnsupportedOperationException(); }
        @Override public void setEnabled(boolean e) { throw new UnsupportedOperationException(); }
        @Override public boolean showDeviation() { throw new UnsupportedOperationException(); }
        @Override public void setShowDeviation(boolean s) { throw new UnsupportedOperationException(); }
        @Override public int getIconRotation() { throw new UnsupportedOperationException(); }
        @Override public void setIconRotation(int r) { throw new UnsupportedOperationException(); }
        @Override public Integer getIconColor() { throw new UnsupportedOperationException(); }
        @Override public void setIconColor(Integer c) { throw new UnsupportedOperationException(); }
        @Override public float getIconOpacity() { throw new UnsupportedOperationException(); }
        @Override public void setIconOpacity(float o) { throw new UnsupportedOperationException(); }
        @Override public ResourceLocation getIconResourceLocation() { throw new UnsupportedOperationException(); }
        @Override public void setIconResourceLoctaion(ResourceLocation i) { throw new UnsupportedOperationException(); }
        @Override public int getIconTextureWidth() { throw new UnsupportedOperationException(); }
        @Override public void setIconTextureWidth(Integer w) { throw new UnsupportedOperationException(); }
        @Override public int getIconTextureHeight() { throw new UnsupportedOperationException(); }
        @Override public void setIconTextureHeight(Integer h) { throw new UnsupportedOperationException(); }
        @Override public void setCustomData(String data) { throw new UnsupportedOperationException(); }
        @Override public String getCustomData() { throw new UnsupportedOperationException(); }
        @Override public void setCustomData(String k, String v) { throw new UnsupportedOperationException(); }
        @Override public String getCustomData(String k) { throw new UnsupportedOperationException(); }
    }

    private static StubApi apiWithExisting(Waypoint... existing) {
        return new StubApi(java.util.Arrays.asList(existing));
    }

    @Test
    void absentJourneyMapSkipsDispatch() {
        JourneyMapClientHook.dispatch(false, () -> org.junit.jupiter.api.Assertions.fail("Must not load optional API"));
    }

    @Test
    void runtimeFailureIsContained() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> JourneyMapClientHook.dispatch(true,
                () -> { throw new IllegalStateException("API unavailable"); }));
    }

    @Test
    void linkageFailureIsContained() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> JourneyMapClientHook.dispatch(true,
                () -> { throw new NoClassDefFoundError("optional API"); }));
    }

    @Test
    void loadedDispatchRuns() {
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        JourneyMapClientHook.dispatch(true, calls::incrementAndGet);
        assertEquals(1, calls.get());
    }

    @Test
    @DisplayName("api==null (JM 未初期化) では登録しない")
    void nullApiRegistersNothing() {
        StubApi api = apiWithExisting();
        WaypointRegistrar.show(null, "Village Plains", new BlockPos(100, 64, -200), DIM, false, 0xFFD700, true);
        assertEquals(0, api.added.size());
    }

    @Test
    @DisplayName("初回の構造物は登録される (factory 実体を通る)")
    void structureRegistersThroughFactory() {
        StubApi api = apiWithExisting();
        WaypointRegistrar.show(api, "Village Plains", new BlockPos(100, 64, -200), DIM, false, 0xFFD700, true);
        assertEquals(1, api.added.size());
        Waypoint wp = api.added.get(0);
        assertEquals("Village Plains (100, -200)", wp.getName());
        assertEquals(0xFFD700, wp.getColor());
        assertEquals(true, wp.isPersistent());
    }

    @Test
    @DisplayName("同じ構造物の再発見は登録しない (表示名完全一致)")
    void structureRepeatSuppressed() {
        StubApi api = apiWithExisting();
        WaypointRegistrar.show(api, "Village Plains", new BlockPos(100, 64, -200), DIM, false, 0xFFD700, true);
        WaypointRegistrar.show(api, "Village Plains", new BlockPos(100, 64, -200), DIM, false, 0xFFD700, true);
        assertEquals(1, api.added.size());
    }

    @Test
    @DisplayName("別の構造物 (座標違い) は登録される")
    void structureDifferentPositionRegisters() {
        StubApi api = apiWithExisting();
        WaypointRegistrar.show(api, "Village Plains", new BlockPos(100, 64, -200), DIM, false, 0xFFD700, true);
        WaypointRegistrar.show(api, "Village Plains", new BlockPos(800, 64, 900), DIM, false, 0xFFD700, true);
        assertEquals(2, api.added.size());
    }

    @Test
    @DisplayName("バイオームは座標を除いた前方一致で抑止 (Forest が Forest Hills に当たらない)")
    void biomePrefixMatchSuppressesSameBiomeOnly() {
        StubApi api = apiWithExisting();
        WaypointRegistrar.show(api, "Forest", new BlockPos(10, 64, 10), DIM, true, 0x228B22, true);
        WaypointRegistrar.show(api, "Forest", new BlockPos(40, 64, 44), DIM, true, 0x228B22, true);
        assertEquals(1, api.added.size(), "同じバイオームの再検索は増やさない");
        WaypointRegistrar.show(api, "Forest Hills", new BlockPos(70, 64, 70), DIM, true, 0x228B22, true);
        assertEquals(2, api.added.size(), "別名バイオームは登録される");
    }

    @Test
    @DisplayName("バイオーム表示名は [Biome] 接頭辞付き・座標込みで作られる")
    void biomeDisplayNameHasPrefixAndCoords() {
        StubApi api = apiWithExisting();
        WaypointRegistrar.show(api, "Desert", new BlockPos(1, 64, 2), DIM, true, 0xF5DEB3, false);
        assertEquals("[Biome] Desert (1, 2)", api.added.get(0).getName());
    }
}
