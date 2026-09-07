package com.kuronami.compasstomap.event;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.kuronami.compasstomap.CompassToMapFabric;
import com.kuronami.compasstomap.Config;
import com.kuronami.compasstomap.network.BiomeFoundPayload;
import com.kuronami.compasstomap.network.StructureFoundPayload;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Explorer's Compass / Nature's Compass の DataComponent を監視 (Fabric 1.21.1)。
 *
 * Fabric 版の差分 (vs NeoForge):
 *  - ServerTickEvents.END_SERVER_TICK は server 全体で 1 回。各 player を iterate する。
 *  - ログイン / ログアウトは ServerPlayConnectionEvents.JOIN / DISCONNECT で
 *    PlayerState を作り直す・破棄する。
 *  - ModList → FabricLoader.getInstance().isModLoaded()
 *  - PacketDistributor → ServerPlayNetworking.send(player, payload)
 */
public final class CompassWatcher {

    /** 各 player の観測状態 (dedupe / ログイン時持ち越しの判定用)。ログアウト時に破棄。 */
    private static final Map<UUID, PlayerState> STATES = new ConcurrentHashMap<>();

    private static volatile boolean ecApiBroken = false;
    private static volatile boolean ncApiBroken = false;

    private CompassWatcher() {}

    /** ログイン時に state を作り直す (priming 窓を毎セッション張り直すため)。 */
    public static void onPlayerJoin(ServerPlayer player) {
        STATES.put(player.getUUID(), new PlayerState());
    }

    /** ログアウト時に state を破棄する。 */
    public static void onPlayerDisconnect(ServerPlayer player) {
        STATES.remove(player.getUUID());
    }

    /**
     * Fabric の {@code ServerTickEvents.END_SERVER_TICK} ハンドラ。
     * server 全体で毎 tick 呼ばれるので、各 player を iterate して per-player check。
     */
    public static void onServerTick(MinecraftServer server) {
        if (!Config.ENABLED.get()) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(player.level() instanceof ServerLevel serverLevel)) continue;

            // ログイン event を取りこぼした場合の受け皿。新規に作った state は必ず priming から始まる。
            PlayerState state = STATES.computeIfAbsent(player.getUUID(), k -> new PlayerState());
            boolean priming = state.consumePrimingTick();

            if (!ecApiBroken && Config.ENABLE_STRUCTURE.get()
                    && FabricLoader.getInstance().isModLoaded("explorerscompass")) {
                try {
                    ECInner.tickCheck(player, serverLevel, state, priming);
                } catch (LinkageError | RuntimeException t) {
                    ecApiBroken = true;
                    CompassToMapFabric.LOGGER.warn(
                            "Explorer's Compass API mismatch or class missing. Structure detection disabled until restart.", t);
                }
            }

            if (!ncApiBroken && Config.ENABLE_BIOME.get()
                    && FabricLoader.getInstance().isModLoaded("naturescompass")) {
                try {
                    NCInner.tickCheck(player, serverLevel, state, priming);
                } catch (LinkageError | RuntimeException t) {
                    ncApiBroken = true;
                    CompassToMapFabric.LOGGER.warn(
                            "Nature's Compass API mismatch or class missing. Biome detection disabled until restart.", t);
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Explorer's Compass (構造物検出)
    // ─────────────────────────────────────────────────────────────
    private static final class ECInner {
        static void tickCheck(ServerPlayer player, ServerLevel serverLevel,
                              PlayerState state, boolean priming) {
            Inventory inv = player.getInventory();
            ItemStack found = null;
            for (int i = 0; i < inv.items.size(); i++) {
                ItemStack stack = inv.items.get(i);
                if (isFound(stack)) { found = stack; break; }
            }
            if (found == null) {
                ItemStack off = inv.offhand.get(0);
                if (isFound(off)) found = off;
            }
            if (found == null) return;

            String structureId = found.get(com.chaosthedude.explorerscompass.ExplorersCompass.STRUCTURE_ID_COMPONENT);
            Integer x = found.get(com.chaosthedude.explorerscompass.ExplorersCompass.FOUND_X_COMPONENT);
            Integer z = found.get(com.chaosthedude.explorerscompass.ExplorersCompass.FOUND_Z_COMPONENT);
            if (structureId == null || x == null || z == null) return;

            String key = DedupeKeys.structure(structureId, x, z);
            if (!state.shouldRegister(key, x, z, priming)) return;

            int y = estimateY(serverLevel, x, z, structureId, false);
            BlockPos pos = new BlockPos(x, y, z);

            ServerPlayNetworking.send(player,
                    new StructureFoundPayload(structureId, pos, serverLevel.dimension()));

            if (Config.NOTIFY_ON_FOUND.get()) {
                String prettyName = com.kuronami.compasstomap.compat.jm.JourneyMapClientHook
                        .prettifyResourceName(structureId);
                sendChatNotification(player, "message.compasstomap.structure_found", prettyName, x, y, z);
            }

            CompassToMapFabric.LOGGER.info("Structure found by {}: {} @ ({}, ~{}, {})",
                    player.getName().getString(), structureId, x, y, z);
        }

        private static boolean isFound(ItemStack stack) {
            if (stack.isEmpty()) return false;
            // Fabric では EXPLORERS_COMPASS_ITEM (NF/Forge では explorersCompass) が public field
            if (!(stack.getItem() instanceof com.chaosthedude.explorerscompass.items.ExplorersCompassItem)) return false;
            Integer state = stack.get(com.chaosthedude.explorerscompass.ExplorersCompass.COMPASS_STATE_COMPONENT);
            return state != null && state == com.chaosthedude.explorerscompass.util.CompassState.FOUND.getID();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Nature's Compass (バイオーム検出)
    // ─────────────────────────────────────────────────────────────
    private static final class NCInner {
        static void tickCheck(ServerPlayer player, ServerLevel serverLevel,
                              PlayerState state, boolean priming) {
            Inventory inv = player.getInventory();
            ItemStack found = null;
            for (int i = 0; i < inv.items.size(); i++) {
                ItemStack stack = inv.items.get(i);
                if (isFound(stack)) { found = stack; break; }
            }
            if (found == null) {
                ItemStack off = inv.offhand.get(0);
                if (isFound(off)) found = off;
            }
            if (found == null) return;

            // 注: NC Fabric は NeoForge と命名規則が違う:
            //   NF: BIOME_ID, COMPASS_STATE, FOUND_X, FOUND_Z
            //   Fabric: BIOME_ID_COMPONENT, COMPASS_STATE_COMPONENT, FOUND_X_COMPONENT, FOUND_Z_COMPONENT
            String biomeId = found.get(com.chaosthedude.naturescompass.NaturesCompass.BIOME_ID_COMPONENT);
            Integer x = found.get(com.chaosthedude.naturescompass.NaturesCompass.FOUND_X_COMPONENT);
            Integer z = found.get(com.chaosthedude.naturescompass.NaturesCompass.FOUND_Z_COMPONENT);
            if (biomeId == null || x == null || z == null) return;

            String key = DedupeKeys.biome(biomeId);
            if (!state.shouldRegister(key, x, z, priming)) return;

            int y = estimateY(serverLevel, x, z, biomeId, true);
            BlockPos pos = new BlockPos(x, y, z);

            ServerPlayNetworking.send(player,
                    new BiomeFoundPayload(biomeId, pos, serverLevel.dimension()));

            if (Config.NOTIFY_ON_FOUND.get()) {
                String prettyName = com.kuronami.compasstomap.compat.jm.JourneyMapClientHook
                        .prettifyResourceName(biomeId);
                sendChatNotification(player, "message.compasstomap.biome_found", prettyName, x, y, z);
            }

            CompassToMapFabric.LOGGER.info("Biome found by {}: {} @ ({}, ~{}, {})",
                    player.getName().getString(), biomeId, x, y, z);
        }

        private static boolean isFound(ItemStack stack) {
            if (stack.isEmpty()) return false;
            if (!(stack.getItem() instanceof com.chaosthedude.naturescompass.items.NaturesCompassItem)) return false;
            Integer state = stack.get(com.chaosthedude.naturescompass.NaturesCompass.COMPASS_STATE_COMPONENT);
            // NC Fabric は util ではなく utils パッケージ
            return state != null && state == com.chaosthedude.naturescompass.utils.CompassState.FOUND.getID();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 共通ヘルパー
    // ─────────────────────────────────────────────────────────────

    private static int estimateY(ServerLevel level, int x, int z, String resourceId, boolean isBiome) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y <= level.getMinY() + 1) {
            String dim = level.dimension().location().toString();
            if ("minecraft:the_end".equals(dim)) return 64;
            if ("minecraft:the_nether".equals(dim)) return 96;
            if (isBiome) return 96;
            String lower = resourceId.toLowerCase();
            if (lower.contains("mineshaft") || lower.contains("dungeon")
                    || lower.contains("stronghold") || lower.contains("ancient_city")
                    || lower.contains("trial_chambers")) return 40;
            if (lower.contains("ocean_monument") || lower.contains("shipwreck")
                    || lower.contains("buried_treasure")) return 80;
            return 96;
        }
        return y;
    }

    private static void sendChatNotification(ServerPlayer player, String translationKey,
                                              String prettyName, int x, int y, int z) {
        final boolean isOp = player.hasPermissions(2);
        final String tpCmd = "/tp @s " + x + " " + y + " " + z;
        Component coord = Component.literal(x + ", " + z)
                .withStyle(s -> {
                    s = s.withColor(ChatFormatting.LIGHT_PURPLE);
                    if (isOp) {
                        s = s.withUnderlined(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tpCmd))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.literal("Click to insert /tp command")));
                    }
                    return s;
                });
        player.displayClientMessage(
                Component.translatable(translationKey, prettyName, coord),
                false
        );
    }
}
