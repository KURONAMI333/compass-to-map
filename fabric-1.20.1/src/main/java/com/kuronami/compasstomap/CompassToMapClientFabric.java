package com.kuronami.compasstomap;

import com.kuronami.compasstomap.event.ClientDispatch;
import com.kuronami.compasstomap.compat.jm.JourneyMapClientHook;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class CompassToMapClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ClientDispatch.STRUCTURE, (client, handler, buf, sender) -> {
            String id = buf.readUtf();
            BlockPos pos = buf.readBlockPos();
            ResourceLocation dim = buf.readResourceLocation();
            client.execute(() -> JourneyMapClientHook.show(id, pos, dim, false));
        });
        ClientPlayNetworking.registerGlobalReceiver(ClientDispatch.BIOME, (client, handler, buf, sender) -> {
            String id = buf.readUtf();
            BlockPos pos = buf.readBlockPos();
            ResourceLocation dim = buf.readResourceLocation();
            client.execute(() -> JourneyMapClientHook.show(id, pos, dim, true));
        });
    }
}
