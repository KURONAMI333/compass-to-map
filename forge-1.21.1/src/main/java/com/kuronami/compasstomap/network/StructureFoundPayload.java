package com.kuronami.compasstomap.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * 構造物発見時にサーバ→クライアントへ送信される waypoint 登録依頼。
 *
 * 注: Forge 1.21.1 の SimpleChannel ベースで、record + 手書き encode/decode。
 *      NeoForge 版の {@code CustomPacketPayload} + {@code StreamCodec} とは別実装。
 *
 * @param structureId   構造物 ID (例: minecraft:village_plains)
 * @param pos           推定座標 (Y は Heightmap で補完済み)
 * @param dimension     対象ディメンション
 */
public record StructureFoundPayload(String structureId, BlockPos pos, ResourceKey<Level> dimension) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(structureId);
        buf.writeBlockPos(pos);
        buf.writeResourceKey(dimension);
    }

    public static StructureFoundPayload decode(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        BlockPos p = buf.readBlockPos();
        ResourceKey<Level> dim = buf.readResourceKey(net.minecraft.core.registries.Registries.DIMENSION);
        return new StructureFoundPayload(id, p, dim);
    }
}
