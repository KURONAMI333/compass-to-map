package com.kuronami.compasstomap;

import com.mojang.logging.LogUtils;

import com.kuronami.compasstomap.network.BiomeFoundPayload;
import com.kuronami.compasstomap.network.StructureFoundPayload;
import fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Compass to Map: Fabric 1.21.4 Mod entry。
 *
 * Fabric 版の差分 (vs NeoForge):
 *  - {@link ModInitializer}#onInitialize() が起動エントリ
 *  - Config 登録は {@link ForgeConfigRegistry} (FCAP 提供)
 *  - tick イベントは {@link ServerTickEvents#END_SERVER_TICK} で server 全体を毎 tick 監視、
 *    その中で各 player を iterate (NeoForge の per-player PlayerTickEvent 相当)
 *  - payload 登録は {@link PayloadTypeRegistry#playS2C()} (vanilla CustomPacketPayload)
 */
public class CompassToMapFabric implements ModInitializer {

    public static final String MODID = "compasstomap";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        // ─── Config 登録 ────────────────────────────────────────
        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, Config.SPEC);

        // ─── Payload 型を Server→Client で登録 ──────────────────
        PayloadTypeRegistry.playS2C().register(StructureFoundPayload.TYPE, StructureFoundPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(BiomeFoundPayload.TYPE, BiomeFoundPayload.STREAM_CODEC);

        // ─── Tick listener (server 側) ─────────────────────────
        ServerTickEvents.END_SERVER_TICK.register(com.kuronami.compasstomap.event.CompassWatcher::onServerTick);

        LOGGER.info("Compass to Map (Fabric 1.21.4) initialized");
    }
}
