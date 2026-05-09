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
 * Compass to Map: Forge 1.20.1 (NBT-based EC/NC API).
 *
 * 1.21.1 版との差分：
 *  - DataComponents 未導入 → ItemStack.get(component) ではなく
 *    ExplorersCompass.explorersCompass.getStructureKey(stack) 等の instance method
 *  - Java 17 (vs 21)
 *  - ResourceLocation 生成は new ResourceLocation(ns, path) (parse() は無いが互換あり)
 */
@Mod(CompassToMap.MODID)
public class CompassToMap {

    public static final String MODID = "compasstomap";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CompassToMap() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(com.kuronami.compasstomap.event.CompassWatcher.class);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(com.kuronami.compasstomap.network.CompassToMapNetwork::register);
    }
}
