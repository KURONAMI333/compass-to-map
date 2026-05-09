package com.kuronami.compasstomap.network;

import com.kuronami.compasstomap.CompassToMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

/**
 * Forge 1.21.1 SimpleChannel ベースのカスタムパケット登録。
 *
 * 仕様：
 *  - Channel ID = "compasstomap:main", protocol version 2
 *  - パケット: 0=Structure, 1=Biome
 *  - 方向: PLAY_TO_CLIENT (server → client のみ)
 *  - クライアント側ハンドラは {@link DistExecutor#unsafeRunWhenOn} で
 *    CLIENT-side 限定実行 (専用サーバで NoClassDefFoundError を防ぐ)
 */
public final class CompassToMapNetwork {

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(CompassToMap.MODID, "main"))
            .networkProtocolVersion(2)
            .optional()
            .simpleChannel();

    private CompassToMapNetwork() {}

    /**
     * Mod entry の commonSetup 内 (FMLCommonSetupEvent.enqueueWork) から呼ばれる。
     */
    public static void register() {
        CHANNEL.messageBuilder(StructureFoundPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(StructureFoundPayload::encode)
                .decoder(StructureFoundPayload::decode)
                .consumerMainThread(CompassToMapNetwork::handleStructure)
                .add();

        CHANNEL.messageBuilder(BiomeFoundPayload.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(BiomeFoundPayload::encode)
                .decoder(BiomeFoundPayload::decode)
                .consumerMainThread(CompassToMapNetwork::handleBiome)
                .add();
    }

    private static void handleStructure(StructureFoundPayload payload, CustomPayloadEvent.Context ctx) {
        // CLIENT のみで JM hook 呼び出し (専用サーバで JM クラス参照を回避)
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.kuronami.compasstomap.compat.jm.JourneyMapClientHook.onStructureFound(payload));
        ctx.setPacketHandled(true);
    }

    private static void handleBiome(BiomeFoundPayload payload, CustomPayloadEvent.Context ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.kuronami.compasstomap.compat.jm.JourneyMapClientHook.onBiomeFound(payload));
        ctx.setPacketHandled(true);
    }

    // ─── サーバ → 特定クライアント送信ヘルパー ───────────────────────
    public static void sendToPlayer(ServerPlayer player, StructureFoundPayload payload) {
        CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToPlayer(ServerPlayer player, BiomeFoundPayload payload) {
        CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
    }
}
