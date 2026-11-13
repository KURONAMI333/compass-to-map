package com.kuronami.compasstomap.network;

import com.kuronami.compasstomap.CompassToMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

/**
 * 構造物発見時にサーバ→クライアントへ送信される waypoint 登録依頼 payload。
 *
 * @param structureId   構造物 ID (例: minecraft:village_plains)
 * @param pos           推定座標 (Y は Heightmap で補完済み)
 * @param dimension     対象ディメンション
 */
public record StructureFoundPayload(String structureId, BlockPos pos, ResourceKey<Level> dimension)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StructureFoundPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(CompassToMap.MODID, "structure_found"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StructureFoundPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, StructureFoundPayload::structureId,
                    BlockPos.STREAM_CODEC, StructureFoundPayload::pos,
                    ResourceKey.streamCodec(Registries.DIMENSION), StructureFoundPayload::dimension,
                    StructureFoundPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
