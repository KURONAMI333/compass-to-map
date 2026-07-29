package com.kuronami.compasstomap;

import com.kuronami.compasstomap.network.BiomeFoundPayload;
import com.kuronami.compasstomap.network.StructureFoundPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Fabric クライアント側エントリ。Server→Client payload 受信ハンドラを登録。
 *
 * NeoForge / Forge では PayloadRegistrar / SimpleChannel 内でクライアント側ハンドラも一括登録するが、
 * Fabric では明示的に ClientPlayNetworking.registerGlobalReceiver で受信側ハンドラを登録する。
 */
public class CompassToMapClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(StructureFoundPayload.TYPE,
                (payload, context) -> context.client().execute(
                        () -> com.kuronami.compasstomap.compat.jm.JourneyMapClientHook.onStructureFound(payload)));

        ClientPlayNetworking.registerGlobalReceiver(BiomeFoundPayload.TYPE,
                (payload, context) -> context.client().execute(
                        () -> com.kuronami.compasstomap.compat.jm.JourneyMapClientHook.onBiomeFound(payload)));
    }
}
