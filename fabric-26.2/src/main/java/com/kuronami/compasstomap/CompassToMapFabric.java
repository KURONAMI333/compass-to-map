package com.kuronami.compasstomap;

import com.mojang.logging.LogUtils;

import com.kuronami.compasstomap.network.BiomeFoundPayload;
import com.kuronami.compasstomap.network.StructureFoundPayload;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Compass to Map: Fabric 26.2 Mod entry。
 *
 * Fabric 版の差分 (vs NeoForge):
 *  - {@link ModInitializer}#onInitialize() が起動エントリ
 *  - Config 登録は {@link ForgeConfigRegistry} (FCAP 提供)
 *  - tick イベントは {@link ServerTickEvents#END_SERVER_TICK} で server 全体を毎 tick 監視、
 *    その中で各 player を iterate (NeoForge の per-player PlayerTickEvent 相当)
 *  - payload 登録は {@link PayloadTypeRegistry#clientboundPlay()} (vanilla CustomPacketPayload)。
 *    26.2 で fabric-api の {@code playS2C()/playC2S()} は {@code clientboundPlay()/serverboundPlay()}
 *    に改名された (26.2_MIGRATION_NOTES.md)
 */
public class CompassToMapFabric implements ModInitializer {

    public static final String MODID = "compasstomap";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        // ─── Config 登録 ────────────────────────────────────────
        ConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, Config.SPEC);

        // ─── Payload 型を Server→Client で登録 ──────────────────
        PayloadTypeRegistry.clientboundPlay().register(StructureFoundPayload.TYPE, StructureFoundPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BiomeFoundPayload.TYPE, BiomeFoundPayload.STREAM_CODEC);

        // ─── Tick listener (server 側) ─────────────────────────
        ServerTickEvents.END_SERVER_TICK.register(com.kuronami.compasstomap.event.CompassWatcher::onServerTick);

        LOGGER.info("Compass to Map (Fabric 26.2) initialized");
    }
}
