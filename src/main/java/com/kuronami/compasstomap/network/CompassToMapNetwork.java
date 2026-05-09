package com.kuronami.compasstomap.network;

import com.kuronami.compasstomap.CompassToMap;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * NeoForge 1.21 のカスタムパケット登録。
 */
@EventBusSubscriber(modid = CompassToMap.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class CompassToMapNetwork {

    private CompassToMapNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("2").optional();
        // Structure (Explorer's Compass)
        registrar.playToClient(
                StructureFoundPayload.TYPE,
                StructureFoundPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        com.kuronami.compasstomap.compat.jm.JourneyMapClientHook.onStructureFound(payload)
                )
        );
        // Biome (Nature's Compass) - v2.0 で追加
        registrar.playToClient(
                BiomeFoundPayload.TYPE,
                BiomeFoundPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        com.kuronami.compasstomap.compat.jm.JourneyMapClientHook.onBiomeFound(payload)
                )
        );
    }
}
