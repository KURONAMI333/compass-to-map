package com.kuronami.compasstomap;

import com.mojang.logging.LogUtils;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Compass to Map: Fabric 1.20.1 Mod entry。
 *
 * 1.21.1 Fabric との差分：
 *  - FCAP の v3 API (1.20.1 用 v8.0.3-Fabric)
 *  - Network: Fabric 1.20.1 は CustomPacketPayload 未導入のため raw ByteBuf packet を使う必要あり
 *    → v2.0 Fabric 1.20.1 では JM 統合 disable, server side chat 通知のみ
 */
public class CompassToMapFabric implements ModInitializer {

    public static final String MODID = "compasstomap";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, Config.SPEC);
        ServerTickEvents.END_SERVER_TICK.register(com.kuronami.compasstomap.event.CompassWatcher::onServerTick);
        LOGGER.info("Compass to Map (Fabric 1.20.1) initialized - JM integration disabled in v2.0");
    }
}
