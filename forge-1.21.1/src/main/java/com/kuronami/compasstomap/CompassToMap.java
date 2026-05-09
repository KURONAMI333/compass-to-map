package com.kuronami.compasstomap;

import com.mojang.logging.LogUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;

/**
 * Compass to Map: Explorer's Compass × Nature's Compass × JourneyMap Addon (Forge 1.21.1).
 *
 * Forge 版は NeoForge 版と同一機能。差分は主に：
 *  - Mod entry: @Mod + FMLJavaModLoadingContext.getModEventBus()
 *  - EventBus: MinecraftForge.EVENT_BUS
 *  - Config: ForgeConfigSpec (ModConfigSpec の Forge 版エイリアス)
 *  - Network: SimpleChannel (NeoForge の PayloadRegistrar の代わり)
 */
@Mod(CompassToMap.MODID)
public class CompassToMap {

    public static final String MODID = "compasstomap";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CompassToMap() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Mod lifecycle イベント (network 登録など)
        modEventBus.addListener(this::commonSetup);

        // ゲーム実行時イベント (PlayerTickEvent 等)
        MinecraftForge.EVENT_BUS.register(com.kuronami.compasstomap.event.CompassWatcher.class);

        // Config 登録
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // SimpleChannel の初期化を enqueueWork 内で実行
        event.enqueueWork(com.kuronami.compasstomap.network.CompassToMapNetwork::register);
    }
}
