package com.kuronami.compasstomap.network;

import com.kuronami.compasstomap.CompassToMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Nature's Compass によるバイオーム発見時にサーバ→クライアントへ送信される
 * waypoint 登録依頼 payload (v2.0 で追加)。
 *
 * @param biomeId       バイオーム ID (例: minecraft:desert)
 * @param pos           推定座標 (Y は Heightmap or fallback で補完済み)
 * @param dimension     対象ディメンション
 */
public record BiomeFoundPayload(String biomeId, BlockPos pos, ResourceKey<Level> dimension)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BiomeFoundPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CompassToMap.MODID, "biome_found"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BiomeFoundPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, BiomeFoundPayload::biomeId,
                    BlockPos.STREAM_CODEC, BiomeFoundPayload::pos,
                    ResourceKey.streamCodec(Registries.DIMENSION), BiomeFoundPayload::dimension,
                    BiomeFoundPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
