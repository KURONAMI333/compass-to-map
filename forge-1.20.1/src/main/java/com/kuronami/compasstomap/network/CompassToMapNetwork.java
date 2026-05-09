package com.kuronami.compasstomap.network;

import com.kuronami.compasstomap.CompassToMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Forge 1.20.1 SimpleChannel ベース。1.21.1 とは API が違う：
 *  - {@link NetworkRegistry#newSimpleChannel} でビルド
 *  - handler signature: {@code BiConsumer<MSG, Supplier<NetworkEvent.Context>>}
 *  - {@link SimpleChannel#send(PacketDistributor.PacketTarget, Object)} (target → msg の順)
 */
public final class CompassToMapNetwork {

    private static final String PROTOCOL_VERSION = "2";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CompassToMap.MODID, "main"),
            () -> PROTOCOL_VERSION,
            v -> true,   // accept any client version
            v -> true    // accept any server version
    );

    private CompassToMapNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(
                0,
                StructureFoundPayload.class,
                StructureFoundPayload::encode,
                StructureFoundPayload::decode,
                CompassToMapNetwork::handleStructure,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                1,
                BiomeFoundPayload.class,
                BiomeFoundPayload::encode,
                BiomeFoundPayload::decode,
                CompassToMapNetwork::handleBiome,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    private static void handleStructure(StructureFoundPayload payload, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.kuronami.compasstomap.compat.jm.JourneyMapClientHook.onStructureFound(payload)));
        ctx.setPacketHandled(true);
    }

    private static void handleBiome(BiomeFoundPayload payload, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.kuronami.compasstomap.compat.jm.JourneyMapClientHook.onBiomeFound(payload)));
        ctx.setPacketHandled(true);
    }

    public static void sendToPlayer(ServerPlayer player, StructureFoundPayload payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    public static void sendToPlayer(ServerPlayer player, BiomeFoundPayload payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }
}
