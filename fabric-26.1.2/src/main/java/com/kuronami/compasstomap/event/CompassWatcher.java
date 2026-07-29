package com.kuronami.compasstomap.event;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
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
 * Explorer's Compass / Nature's Compass の DataComponent を監視 (Fabric 26.1.2)。
 *
 * Fabric 版の差分 (vs NeoForge):
 *  - ServerTickEvents.END_SERVER_TICK は server 全体で 1 回。各 player を iterate する。
 *  - Player ログアウトクリーンアップは現状未対応 (将来 ServerPlayConnectionEvents.DISCONNECT で追加)。
 *  - ModList → FabricLoader.getInstance().isModLoaded()
 *  - PacketDistributor → ServerPlayNetworking.send(player, payload)
 */
public final class CompassWatcher {

    private static final Map<UUID, Set<String>> SEEN_KEYS = new ConcurrentHashMap<>();
    private static final int MAX_SEEN_PER_PLAYER = 512;

    private static volatile boolean ecApiBroken = false;
    private static volatile boolean ncApiBroken = false;

    private CompassWatcher() {}

    /**
     * Fabric の {@code ServerTickEvents.END_SERVER_TICK} ハンドラ。
     * server 全体で毎 tick 呼ばれるので、各 player を iterate して per-player check。
     */
    public static void onServerTick(MinecraftServer server) {
        if (!Config.ENABLED.get()) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(player.level() instanceof ServerLevel serverLevel)) continue;

            if (!ecApiBroken && Config.ENABLE_STRUCTURE.get()
                    && FabricLoader.getInstance().isModLoaded("explorerscompass")) {
                try {
                    ECInner.tickCheck(player, serverLevel);
                } catch (LinkageError | RuntimeException t) {
                    ecApiBroken = true;
                    CompassToMapFabric.LOGGER.warn(
                            "Explorer's Compass API mismatch or class missing. Structure detection disabled until restart.", t);
                }
            }

            if (!ncApiBroken && Config.ENABLE_BIOME.get()
                    && FabricLoader.getInstance().isModLoaded("naturescompass")) {
                try {
                    NCInner.tickCheck(player, serverLevel);
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
        static void tickCheck(ServerPlayer player, ServerLevel serverLevel) {
            Inventory inv = player.getInventory();
            ItemStack found = null;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (isFound(stack)) { found = stack; break; }
            }
            if (found == null) return;

            String structureId = found.get(com.chaosthedude.explorerscompass.ExplorersCompass.STRUCTURE_ID);
            Integer x = found.get(com.chaosthedude.explorerscompass.ExplorersCompass.FOUND_X);
            Integer z = found.get(com.chaosthedude.explorerscompass.ExplorersCompass.FOUND_Z);
            if (structureId == null || x == null || z == null) return;

            String dimKey = serverLevel.dimension().identifier().toString();
            String key = "s|" + dimKey + "|" + structureId + "|" + x + "|" + z;
            if (!recordSeen(player.getUUID(), key)) return;

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
            if (!(stack.getItem() instanceof com.chaosthedude.explorerscompass.item.ExplorersCompassItem)) return false;
            Integer state = stack.get(com.chaosthedude.explorerscompass.ExplorersCompass.COMPASS_STATE);
            return state != null && state == com.chaosthedude.explorerscompass.util.CompassState.FOUND.getID();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Nature's Compass (バイオーム検出)
    // ─────────────────────────────────────────────────────────────
    private static final class NCInner {
        static void tickCheck(ServerPlayer player, ServerLevel serverLevel) {
            Inventory inv = player.getInventory();
            ItemStack found = null;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (isFound(stack)) { found = stack; break; }
            }
            if (found == null) return;

            // 注: NC Fabric は NeoForge と命名規則が違う:
            //   NF: BIOME_ID, COMPASS_STATE, FOUND_X, FOUND_Z
            //   Fabric: BIOME_ID_COMPONENT, COMPASS_STATE_COMPONENT, FOUND_X_COMPONENT, FOUND_Z_COMPONENT
            String biomeId = found.get(com.chaosthedude.naturescompass.NaturesCompass.BIOME_ID);
            Integer x = found.get(com.chaosthedude.naturescompass.NaturesCompass.FOUND_X);
            Integer z = found.get(com.chaosthedude.naturescompass.NaturesCompass.FOUND_Z);
            if (biomeId == null || x == null || z == null) return;

            String dimKey = serverLevel.dimension().identifier().toString();
            String key = "b|" + dimKey + "|" + biomeId + "|" + x + "|" + z;
            if (!recordSeen(player.getUUID(), key)) return;

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
            if (!(stack.getItem() instanceof com.chaosthedude.naturescompass.item.NaturesCompassItem)) return false;
            Integer state = stack.get(com.chaosthedude.naturescompass.NaturesCompass.COMPASS_STATE);
            // NC Fabric は util ではなく utils パッケージ
            return state != null && state == com.chaosthedude.naturescompass.util.CompassState.FOUND.getID();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 共通ヘルパー
    // ─────────────────────────────────────────────────────────────
    private static boolean recordSeen(UUID uuid, String key) {
        Set<String> seen = SEEN_KEYS.computeIfAbsent(uuid,
                k -> Collections.synchronizedSet(new LinkedHashSet<>()));
        synchronized (seen) {
            if (!seen.add(key)) return false;
            if (seen.size() > MAX_SEEN_PER_PLAYER) {
                Iterator<String> it = seen.iterator();
                it.next(); it.remove();
            }
            return true;
        }
    }

    private static int estimateY(ServerLevel level, int x, int z, String resourceId, boolean isBiome) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y <= level.getMinY() + 1) {
            String dim = level.dimension().identifier().toString();
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
        final boolean isOp = ((net.minecraft.server.level.ServerLevel) player.level()).getServer().getPlayerList().isOp(new net.minecraft.server.players.NameAndId(player.getGameProfile()));
        final String tpCmd = "/tp @s " + x + " " + y + " " + z;
        Component coord = Component.literal(x + ", " + z)
                .withStyle(s -> {
                    s = s.withColor(ChatFormatting.LIGHT_PURPLE);
                    if (isOp) {
                        s = s.withUnderlined(true)
                                .withClickEvent(new ClickEvent.SuggestCommand(tpCmd))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.literal("Click to insert /tp command")));
                    }
                    return s;
                });
        // 26.1 で displayClientMessage(Component, boolean) は 2 分割された。
        // actionBar=false（チャット行）は Player#sendSystemMessage(Component) が等価。
        player.sendSystemMessage(
                Component.translatable(translationKey, prettyName, coord)
        );
    }
}
